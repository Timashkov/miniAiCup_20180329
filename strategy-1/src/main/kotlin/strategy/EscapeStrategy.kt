package strategy

import utils.Logger
import WorldConfig
import utils.GameEngine
import utils.Vertex

class EscapeStrategy(val mGlobalConfig: WorldConfig, val mLogger: Logger) : IStrategy {
    override fun apply(gameEngine: GameEngine): StrategyResult {

        val me = gameEngine.worldParseResult.mineInfo
/*
* 1) анализ массы и радиуса противника
* 2) если мы внутри его зоны видимости полностью -> убегаем по кратчайшей
* 3) смотрим на границы карты
*
*
*
* */



        //TODO:??
        if (gameEngine.worldParseResult.worldObjectsInfo.mEnemies.isNotEmpty() && me.mFragmentsState.size == 1) {
            val enemies = gameEngine.worldParseResult.worldObjectsInfo.mEnemies.filter { me.getMainFragment().canBeEatenByEnemy(it.mMass) }
            if (enemies.isNotEmpty()) {
                val deltaX = enemies[0].mVertex.X - me.getMainFragment().mVertex.X
                val k = (enemies[0].mVertex.Y - me.getMainFragment().mVertex.Y) / deltaX

                val targetX = if (deltaX > 0) me.getMainFragment().mVertex.X - deltaX else me.getMainFragment().mVertex.X + deltaX
                val targetY = targetX * (-k)
                return StrategyResult(100, Vertex(targetX, targetY), debugMessage = "ESCAPE!!!!")
            }
        }
        return StrategyResult(-1, Vertex(0.0f, 0.0f), debugMessage = "FindFood: Not applied")
    }

    override fun stopStrategy() {

    }
}