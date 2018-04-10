package strategy

import WorldConfig
import data.ParseResult
import incominginfos.MineInfo
import utils.GameEngine
import utils.Logger
import utils.Vertex

class FindFoodStrategyV2(val mGlobalConfig: WorldConfig, val mLogger: Logger) : IStrategy {

    private var mKnownWay: Vertex = Vertex(35.0f , 111.0f)//Vertex.DEFAULT
    private var mGamerStateCache: MineInfo? = null

    override fun apply(gameEngine: GameEngine, cache: ParseResult?): StrategyResult {
        mLogger.writeLog("Try to apply food search\n")
        val food = gameEngine.worldParseResult.worldObjectsInfo.mFood

        if (food.isEmpty()) {
            mKnownWay = Vertex.DEFAULT
            return StrategyResult(-1, Vertex(0.0f, 0.0f), debugMessage = "FindFood: Not applied")
        } else {
            analyzePlate(gameEngine)

            try {
                if (mKnownWay != Vertex.DEFAULT) {
                    val me = gameEngine.worldParseResult.mineInfo
                    mGamerStateCache = me
                    val viruses = gameEngine.worldParseResult.worldObjectsInfo.mViruses

                    if (me.mFragmentsState.size == 1 && me.getMainFragment().canSplit && gameEngine.currentTick < 1000) {
                        val nearestViruses = viruses.filter {
                            it.mVertex.distance(me.getCoordinates()) <= me.getMainFragment().mRadius * 2f
                        }.sortedBy { it.mVertex.distance(me.getMainFragment().mVertex) }
                        if (nearestViruses.isNotEmpty() && nearestViruses[0].mVertex.distance(me.getMainFragment().mVertex) < mKnownWay.distance(me.getMainFragment().mVertex)) {
                            return StrategyResult(2, nearestViruses[0].mVertex)
                        }
                    }

                    if (shouldSplit(me, mKnownWay)) {
                        mLogger.writeLog("${FindFoodStrategy.DEBUG_TAG} movementTarget $mKnownWay and split for FOOD: $mKnownWay")
                        return StrategyResult(1, mKnownWay, split = true, debugMessage = "Debug : get food with split")
                    } else {
                        val movementTarget = gameEngine.getMovementPointForTarget(me.getNearestFragment(mKnownWay).mId, me.getCoordinates(), mKnownWay)
                        mLogger.writeLog("${FindFoodStrategy.DEBUG_TAG} movementTarget $movementTarget  for FOOD: $mKnownWay")
                        return StrategyResult(1, movementTarget)
                    }
                }
            } catch (e: Exception) {
                mLogger.writeLog("Fault on apply food $e")
            }

            mLogger.writeLog("${FindFoodStrategy.DEBUG_TAG} Find food is not applied")
            return StrategyResult(-1, Vertex(0.0f, 0.0f), debugMessage = "FindFood: Not applied")
        }
    }

    override fun stopStrategy() {
        mKnownWay = Vertex.DEFAULT
        mGamerStateCache = null
    }

    private fun shouldSplit(me: MineInfo, target: Vertex): Boolean {
        var shouldSplit = me.getMainFragment().mMass > WorldConfig.MIN_SPLITABLE_MASS * 1.2f
        shouldSplit = shouldSplit && me.mFragmentsState.none { fragment -> fragment.mCompass.isVertexInDangerArea(target) }
        return shouldSplit
    }

    private fun analyzePlate(gameEngine: GameEngine) {

        try {
            if (isGamerStateChanged(gameEngine.worldParseResult.mineInfo)) {
                mKnownWay = Vertex.DEFAULT
            }

            if (mKnownWay != Vertex.DEFAULT && gameEngine.worldParseResult.mineInfo.mFragmentsState.any { it.mCompass.isVertexInBlackArea(mKnownWay) }) {
                mLogger.writeLog("${FindFoodStrategy.DEBUG_TAG} fragment in black area")
                mKnownWay = Vertex.DEFAULT
            }

            if (mKnownWay != Vertex.DEFAULT && gameEngine.worldParseResult.mineInfo.mFragmentsState.any { it.mVertex == mKnownWay }) {
                mLogger.writeLog("${FindFoodStrategy.DEBUG_TAG} one fragment on the point now")
                mKnownWay = Vertex.DEFAULT
            }

            if (mKnownWay == Vertex.DEFAULT) {
                mKnownWay = gameEngine.worldParseResult.mineInfo.getBestMovementPoint()
            }
        } catch (e: Exception) {
            mLogger.writeLog("Fault on analyze plate $e")
        }
    }

    private fun isGamerStateChanged(gamerInfo: MineInfo): Boolean {
        mGamerStateCache?.let { cached ->
            if (cached.mFragmentsState.size != gamerInfo.mFragmentsState.size) {
                mLogger.writeLog("${FindFoodStrategy.DEBUG_TAG} state changed - fragments count")
                return true
            }
        }
        return false
    }
}