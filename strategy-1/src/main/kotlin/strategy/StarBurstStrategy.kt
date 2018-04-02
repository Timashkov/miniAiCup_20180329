package strategy

import utils.Logger
import WorldConfig
import incominginfos.MineInfo
import incominginfos.WorldObjectsInfo
import utils.Vertex

class StarBurstStrategy(val mGlobalConfig: WorldConfig, val mLogger: Logger) : IStrategy {
    override fun apply(worldInfo: WorldObjectsInfo, mineInfo: MineInfo, currentTickCount: Int): StrategyResult {
        if (worldInfo.mEnemies.isNotEmpty())
            return StrategyResult(-1, Vertex(0.0f, 0.0f), debugMessage = "Star burst: Not applied")

        if (mineInfo.mFragmentsState.size == 1 && mineInfo.getMainFragment().mMass > 120) {
            val nearestViruses = worldInfo.mViruses.filter {
                it.mVertex.distance(mineInfo.getCoordinates()) <= mGlobalConfig.GameHeight / 2f
            }.sortedBy { it.mVertex.distance(mineInfo.getMainFragment().mVertex) }
            if (nearestViruses.isNotEmpty()) {
                return StrategyResult(2, nearestViruses[0].mVertex)
            }
        }
        return StrategyResult(-1, Vertex(0.0f, 0.0f), debugMessage = "Star burst: Not applied")
    }

    override fun stopStrategy() {
    }
}