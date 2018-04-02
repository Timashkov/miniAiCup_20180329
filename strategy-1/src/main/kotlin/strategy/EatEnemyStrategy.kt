package strategy

import WorldConfig
import incominginfos.EnemyInfo
import incominginfos.MineInfo
import incominginfos.WorldObjectsInfo
import utils.GameEngine
import utils.Logger
import utils.Vertex

class EatEnemyStrategy(val mGlobalConfig: WorldConfig, val mLogger: Logger) : IStrategy {

    override fun apply(gameEngine: GameEngine): StrategyResult {

        if (gameEngine.worldParseResult.worldObjectsInfo.mEnemies.isNotEmpty()) {
            return searchForEnemies(gameEngine)
        }

        return StrategyResult(-1, Vertex(0.0f, 0.0f), debugMessage = "Eat Enemy: Not applied")
    }

    override fun stopStrategy() {
    }

    private fun searchForEnemies(gameEngine: GameEngine): StrategyResult {

        val enemies = gameEngine.worldParseResult.worldObjectsInfo.mEnemies
        val me = gameEngine.worldParseResult.mineInfo

        // Stage 0
        if (me.mFragmentsState.size > 2)
            return StrategyResult(-1, Vertex(0.0f, 0.0f), debugMessage = "Eat Enemy: Not applied")

        // Stage 1 - search for 1.25
        val nearEnemies = enemies.filter {
            it.mMass < me.getMainFragment().mMass / 1.25f &&
                    it.mMass > me.getMainFragment().mMass / 2.7f
                    && it.mVertex.distance(me.getCoordinates()) < me.getMainFragment().mRadius * 1.5f
        }.sortedBy { me.getCoordinates().distance(it.mVertex) }
        if (nearEnemies.isNotEmpty()) {
            val res = StrategyResult(10, gameEngine.getMovementPointForTarget(me.getCoordinates(), nearEnemies[0].mVertex), debugMessage = "Try to eat ${nearEnemies[0].mId}")
            mLogger.writeLog("Enemy strat: ${res}")
            return res
        }


        // Stage 2 search for small fragments < 2.5
        if (me.getMainFragment().mMass > 120) {
            val nearLowerEnemies = enemies.filter {
                it.mMass < me.getMainFragment().mMass / 2.7f
            }.sortedBy { me.getCoordinates().distance(it.mVertex) }
            if (nearLowerEnemies.isNotEmpty()) {
                val res = StrategyResult(10, nearLowerEnemies[0].mVertex, split = true, debugMessage = "Try to eat ${nearLowerEnemies[0].mId}")
                mLogger.writeLog("Enemy 2 strat: ${res}")
                return res
            }
        }

        ////


        return StrategyResult(-1, Vertex(0.0f, 0.0f), debugMessage = "Eat Enemy: Not applied")
    }
}