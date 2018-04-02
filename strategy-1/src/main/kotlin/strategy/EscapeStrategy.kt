package strategy

import utils.Logger
import WorldConfig
import incominginfos.MineInfo
import incominginfos.WorldObjectsInfo
import utils.Vertex

class EscapeStrategy(val mGlobalConfig: WorldConfig, val mLogger: Logger) : IStrategy {
    override fun apply(worldInfo: WorldObjectsInfo, mineInfo: MineInfo, currentTickCount: Int): StrategyResult {

        if (worldInfo.mEnemies.isNotEmpty() && mineInfo.mFragmentsState.size == 1) {
            val enemies = worldInfo.mEnemies.filter { it.mMass > mineInfo.getMainFragment().mMass * 4f }
            if (enemies.isNotEmpty()) {
                val deltaX = enemies[0].mVertex.X - mineInfo.getMainFragment().mVertex.X
                val k = (enemies[0].mVertex.Y - mineInfo.getMainFragment().mVertex.Y) / deltaX

                val targetX = if (deltaX > 0) mineInfo.getMainFragment().mVertex.X - deltaX else mineInfo.getMainFragment().mVertex.X + deltaX
                val targetY = targetX * (-k)
                return StrategyResult(1.0f, Vertex(targetX, targetY), debugMessage = "ESCAPE!!!!")
            }
        }
        return StrategyResult(-1.0f, Vertex(0.0f, 0.0f), debugMessage = "FindFood: Not applied")
    }

    override fun stopStrategy() {

    }
}