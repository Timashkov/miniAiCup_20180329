package strategy

import WorldConfig
import data.StepPoint
import data.ParseResult
import incominginfos.MineInfo
import incominginfos.WorldObjectsInfo
import utils.GameEngine
import utils.Logger
import utils.Vertex

class FindFoodStrategyV2(val mGlobalConfig: WorldConfig, val mLogger: Logger) : IStrategy {

    private var mKnownWay: StepPoint? = null
    private var mGamerStateCache: MineInfo? = null

    override fun apply(gameEngine: GameEngine, cache: ParseResult?): StrategyResult {
        mLogger.writeLog("Try to apply food search\n")

        try {
            if (mKnownWay != null && isDestinationAchieved(gameEngine.worldParseResult.worldObjectsInfo) || isGamerStateChanged(gameEngine.worldParseResult.mineInfo)) {
                mKnownWay = null
            }

            mKnownWay = gameEngine.worldParseResult.mineInfo.getBestMovementPoint(mKnownWay)

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

                mLogger.writeLog("$DEBUG_TAG movementTarget $bestWay.target  for FOOD: $mKnownWay")
                return StrategyResult(1, bestWay.movementTarget, eject = bestWay.useEjections, split = bestWay.useSplit, debugMessage = "FindFoodV2 goes to ${bestWay.movementTarget} for point ${bestWay.target} ")
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