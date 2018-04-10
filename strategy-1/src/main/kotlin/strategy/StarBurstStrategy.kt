package strategy

import utils.Logger
import WorldConfig
import WorldConfig.Companion.STAR_BURST_DISABLE_TICK
import data.ParseResult
import utils.GameEngine
import utils.Vertex

class StarBurstStrategy(val mGlobalConfig: WorldConfig, val mLogger: Logger) : IStrategy {

    // Check inertion and viscosity to check ability of burst
    // compass and gray sectors
    override fun apply(gameEngine: GameEngine, cachedParseResult: ParseResult?): StrategyResult {

        mLogger.writeLog("Try to apply star burst")
        val tooLateGameStage = gameEngine.currentTick >= STAR_BURST_DISABLE_TICK
        val fragmentCount = gameEngine.worldParseResult.mineInfo.mFragmentsState.size == 1
        val isNearEnemies = gameEngine.worldParseResult.worldObjectsInfo.mEnemies.isNotEmpty()
        val myFragmentCanNotBurst = gameEngine.worldParseResult.mineInfo.getMainFragment().mRadius < mGlobalConfig.VirusRadius * 1.2

        if (isNearEnemies || tooLateGameStage || myFragmentCanNotBurst ) {
            mLogger.writeLog("Star burst not applied: $isNearEnemies $tooLateGameStage $myFragmentCanNotBurst\n")
            return StrategyResult(-1, Vertex(0.0f, 0.0f), debugMessage = "Star burst: Not applied")
        }
        val mineInfo = gameEngine.worldParseResult.mineInfo

        if ( fragmentCount && mineInfo.getMainFragment().canSplit) {
            val nearestViruses = gameEngine.worldParseResult.worldObjectsInfo.mViruses.filter {
                it.mVertex.distance(mineInfo.getCoordinates()) <= mGlobalConfig.GameHeight / 2f
            }.sortedBy { it.mVertex.distance(mineInfo.getMainFragment().mVertex) }
            if (nearestViruses.isNotEmpty()) {
                mLogger.writeLog("Go to star $nearestViruses[0].mVertex")
                return StrategyResult(2, gameEngine.getMovementPointForTarget(mineInfo.getMainFragment().mId, nearestViruses[0].mVertex))
            }
        }

        mLogger.writeLog("Can not burst\n")
        return StrategyResult(-1, Vertex(0.0f, 0.0f), debugMessage = "Star burst: Not applied")
    }

    override fun stopStrategy() {
    }
}