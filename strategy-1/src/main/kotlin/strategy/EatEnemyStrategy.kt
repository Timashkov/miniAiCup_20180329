package strategy

import WorldConfig
import incominginfos.EnemyInfo
import incominginfos.MineInfo
import incominginfos.WorldObjectsInfo
import utils.Logger
import utils.Vertex

class EatEnemyStrategy(val mGlobalConfig: WorldConfig, val mLogger: Logger) : IStrategy {

    override fun apply(worldInfo: WorldObjectsInfo, mineInfo: MineInfo): StrategyResult {

        if (worldInfo.mEnemies.isNotEmpty()) {
            return searchForEnemies(worldInfo.mEnemies, mineInfo)
        }

        return StrategyResult(-1.0f, Vertex(0.0f, 0.0f), debugMessage = "Eat Enemy: Not applied")
    }

    override fun stopStrategy() {
    }

    private fun searchForEnemies(enemies: ArrayList<EnemyInfo>, mineInfo: MineInfo): StrategyResult {

        // Stage 1 - search for 1.25
        val nearEnemies = enemies.filter {
            it.mMass < mineInfo.getMainFragment().mMass / 1.25f &&
                    it.mMass > mineInfo.getMainFragment().mMass / 2.7f
                    && Vertex(it.mX, it.mY).distance(mineInfo.getCoordinates()) < mineInfo.getMainFragment().mRadius * 1.5f
        }.sortedBy { mineInfo.getCoordinates().distance(Vertex(it.mX, it.mY)) }
        if (nearEnemies.isNotEmpty()) {
            val res = StrategyResult(nearEnemies[0].mMass, Vertex(nearEnemies[0].mX, nearEnemies[0].mY), debugMessage = "Try to eat ${nearEnemies[0].mId}")
            mLogger.writeLog("Enemy strat: ${res}")
            return res
        }


        // Stage 2 search for small fragments < 2.5
        if (mineInfo.getMainFragment().mMass > 120) {
            val nearLowerEnemies = enemies.filter {
                it.mMass < mineInfo.getMainFragment().mMass / 2.7f
            }.sortedBy { mineInfo.getCoordinates().distance(Vertex(it.mX, it.mY)) }
            if (nearLowerEnemies.isNotEmpty()) {
                val res =  StrategyResult(nearLowerEnemies[0].mMass, Vertex(nearLowerEnemies[0].mX, nearLowerEnemies[0].mY), split = true, debugMessage = "Try to eat ${nearLowerEnemies[0].mId}")
                mLogger.writeLog("Enemy 2 strat: ${res}")
                return res
            }
        }

        ////


        return StrategyResult(-1.0f, Vertex(0.0f, 0.0f), debugMessage = "Eat Enemy: Not applied")
    }
}