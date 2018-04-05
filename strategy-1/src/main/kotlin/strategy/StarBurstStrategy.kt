package strategy

import utils.Logger
import WorldConfig
import utils.GameEngine
import utils.Vertex

class StarBurstStrategy(val mGlobalConfig: WorldConfig, val mLogger: Logger) : IStrategy {

    // Check inertion and viscosity to check ability of burst
    // compass and gray sectors
    override fun apply(gameEngine: GameEngine): StrategyResult {
        if (gameEngine.worldParseResult.worldObjectsInfo.mEnemies.isNotEmpty() || gameEngine.currentTick >= mGlobalConfig.GameTicks * 0.3f)
            return StrategyResult(-1, Vertex(0.0f, 0.0f), debugMessage = "Star burst: Not applied")

        if (gameEngine.worldParseResult.mineInfo.mFragmentsState.size == 1 && gameEngine.worldParseResult.mineInfo.getMainFragment().mMass > 120) {
            val nearestViruses = gameEngine.worldParseResult.worldObjectsInfo.mViruses.filter {
                it.mVertex.distance(gameEngine.worldParseResult.mineInfo.getCoordinates()) <= mGlobalConfig.GameHeight / 2f && gameEngine.worldParseResult.mineInfo.getMainFragment().mRadius > mGlobalConfig.VirusRadius * 1.2
            }.sortedBy { it.mVertex.distance(gameEngine.worldParseResult.mineInfo.getMainFragment().mVertex) }
            if (nearestViruses.isNotEmpty()) {
                return StrategyResult(2, nearestViruses[0].mVertex)
            }
        }
        return StrategyResult(-1, Vertex(0.0f, 0.0f), debugMessage = "Star burst: Not applied")
    }

    override fun stopStrategy() {
    }
}