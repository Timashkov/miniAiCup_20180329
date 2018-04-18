package strategy

import WorldConfig
import data.StepPoint
import data.ParseResult
import incominginfos.MineInfo
import incominginfos.WorldObjectsInfo
import utils.GameEngine
import utils.Logger
import utils.Vertex
import kotlin.math.abs

class FindFoodStrategyV2(val mGlobalConfig: WorldConfig, val mLogger: Logger) : IStrategy {

    private var mKnownWay: StepPoint? = null
    private var mGamerStateCache: MineInfo? = null

    override fun apply(gameEngine: GameEngine, cache: ParseResult?): StrategyResult {
        mLogger.writeLog("\nTry to apply food search, known $mKnownWay")

        try {

            if (isDestinationAchieved(gameEngine.worldParseResult.worldObjectsInfo, gameEngine.worldParseResult.mineInfo) || isGamerStateChanged(gameEngine.worldParseResult.mineInfo)) {
                mKnownWay = null
            }

            mKnownWay = gameEngine.worldParseResult.mineInfo.getBestMovementPoint(mKnownWay, mGamerStateCache)

            mKnownWay?.let { bestWay ->

                if (bestWay.target == Vertex.DEFAULT) {
                    return StrategyResult(-1, Vertex(0.0f, 0.0f), debugMessage = "FindFood2: Not applied")
                }

                val me = gameEngine.worldParseResult.mineInfo
                mGamerStateCache = me

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

    private fun isDestinationAchieved(worldInfo: WorldObjectsInfo, me: MineInfo): Boolean {

        mKnownWay?.let { targetWay ->
            val fr = me.getFragmentById(targetWay.fragmentId)
            if (targetWay.target.distance(fr.mVertex) < fr.mRadius*1.5)
                return true
            return false
        }
        return true
    }

    private fun isGamerStateChanged(gamerInfo: MineInfo): Boolean {
        mGamerStateCache?.let { cached ->
            if (cached.mFragmentsState.size != gamerInfo.mFragmentsState.size || abs(cached.totalMass - gamerInfo.totalMass) > gamerInfo.totalMass * 0.2f) {
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