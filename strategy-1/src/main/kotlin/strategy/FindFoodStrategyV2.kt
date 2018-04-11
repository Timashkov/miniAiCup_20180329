package strategy

import WorldConfig
import data.FoodPoint
import data.ParseResult
import incominginfos.MineInfo
import incominginfos.WorldObjectsInfo
import utils.GameEngine
import utils.Logger
import utils.Vertex

class FindFoodStrategyV2(val mGlobalConfig: WorldConfig, val mLogger: Logger) : IStrategy {

    private var mKnownWay: FoodPoint? = null
    private var mGamerStateCache: MineInfo? = null

    override fun apply(gameEngine: GameEngine, cache: ParseResult?): StrategyResult {
        mLogger.writeLog("Try to apply food search\n")

        analyzePlate(gameEngine)

        try {
            mKnownWay?.let { bestWay ->

                if (bestWay.target == Vertex.DEFAULT) {
                    return StrategyResult(-1, Vertex(0.0f, 0.0f), debugMessage = "FindFood2: Not applied")
                }

                val me = gameEngine.worldParseResult.mineInfo
                mGamerStateCache = me
                val viruses = gameEngine.worldParseResult.worldObjectsInfo.mViruses

                if (me.mFragmentsState.size == 1 && me.getMainFragment().canSplit && gameEngine.currentTick < 1000) {
                    val nearestViruses = viruses.filter {
                        it.mVertex.distance(me.getCoordinates()) <= me.getMainFragment().mRadius * 2f
                    }.sortedBy { it.mVertex.distance(me.getMainFragment().mVertex) }
                    if (nearestViruses.isNotEmpty() && nearestViruses[0].mVertex.distance(me.getMainFragment().mVertex) < bestWay.target.distance(me.getMainFragment().mVertex)) {
                        return StrategyResult(2, nearestViruses[0].mVertex)
                    }
                }

                if (shouldSplit(me, bestWay)) {
                    mLogger.writeLog("$DEBUG_TAG movementTarget $mKnownWay and split for FOOD: $mKnownWay")
                    return StrategyResult(1, bestWay.target, eject = bestWay.useEjections, split = true, debugMessage = "Debug : get food with split")
                } else {
                    val movementTarget = gameEngine.getMovementPointForTarget(bestWay.fragmentId, bestWay.target)
                    mLogger.writeLog("$DEBUG_TAG movementTarget $movementTarget  for FOOD: $mKnownWay")
                    return StrategyResult(1, movementTarget, eject = bestWay.useEjections, debugMessage = "Eat food on $movementTarget")
                }
            }
        } catch (e: Exception) {
            mLogger.writeLog("Fault on apply food $e")
        }

        mLogger.writeLog("$DEBUG_TAG Find food is not applied")
        return StrategyResult(-1, Vertex(0.0f, 0.0f), debugMessage = "FindFood: Not applied")

    }

    override fun stopStrategy() {
        mKnownWay = null
        mGamerStateCache = null
    }

    private fun shouldSplit(me: MineInfo, way: FoodPoint): Boolean {
        var shouldSplit = me.getMainFragment().mMass > WorldConfig.MIN_SPLITABLE_MASS * 1.2f
        shouldSplit = shouldSplit && me.mFragmentsState.none { fragment -> fragment.mCompass.isVertexInDangerArea(way.target) }
        return shouldSplit
    }

    private fun analyzePlate(gameEngine: GameEngine) {

        try {
            if (mKnownWay != null && isDestinationAchieved(gameEngine.worldParseResult.worldObjectsInfo) || isGamerStateChanged(gameEngine.worldParseResult.mineInfo)) {
                mKnownWay = null
            }

            mKnownWay = gameEngine.worldParseResult.mineInfo.getBestMovementPoint(mKnownWay)
        } catch (e: Exception) {
            mLogger.writeLog("Fault on analyze plate $e")
        }
    }

    private fun isDestinationAchieved(worldInfo: WorldObjectsInfo): Boolean {
        //если съели целевую точку, а ее соседки остались - то надо доесть

        mKnownWay?.let { targetWay ->
            var c = 0
            targetWay.foodPoints.forEach { fp ->
                if (worldInfo.mFood.map { it.mVertex }.any { it.equals(fp) }) {
                    mLogger.writeLog("FP: $fp")
                    c++
                }
            }
            if (c > 0)
                return false
        }
        return true
    }

    private fun isGamerStateChanged(gamerInfo: MineInfo): Boolean {
        mGamerStateCache?.let { cached ->
            if (cached.mFragmentsState.size != gamerInfo.mFragmentsState.size) {
                mLogger.writeLog("$DEBUG_TAG state changed - fragments count")
                return true
            }
        }
        return false
    }

    companion object {
        val DEBUG_TAG = "FIND_FOOD_v2"
    }
}