package strategy

import utils.Logger
import WorldConfig
import data.ParseResult
import utils.GameEngine
import utils.Vertex

class EscapeStrategy(val mGlobalConfig: WorldConfig, val mLogger: Logger) : IStrategy {
    override fun apply(gameEngine: GameEngine, cachedParseResult: ParseResult?): StrategyResult {

        mLogger.writeLog("Try to escape")
        val me = gameEngine.worldParseResult.mineInfo
/*
* 1) анализ массы и радиуса противника
* 2) если мы внутри его зоны видимости полностью -> убегаем по кратчайшей
* 3) смотрим на границы карты
*
*
*
* */

        //SIMPLE:


//        val squareIndex = currentSquareIndex
//        val cornerIndex = currentCornerIndex
//
//        while (me.mFragmentsState.any { fragment -> fragment.mCompass.getAreaFactor(corner) < 1 } && (squareIndex != currentSquareIndex || cornerIndex != currentCornerIndex)) {
//            corner = getNextCorner()
//            mLogger.writeLog("DEFAULT_TURN $corner \n")
//        }
//
//
//
//        if (squareIndex == currentSquareIndex && cornerIndex == currentCornerIndex && me.mFragmentsState.any { fragment -> fragment.mCompass.getAreaFactor(corner) < 1 }) {
//            // border
//            var found = false
//            arrayOf(mGlobalConfig.ltCorner, mGlobalConfig.rtCorner, mGlobalConfig.rbCorner, mGlobalConfig.lbCorner).forEach { it ->
//                if (me.mFragmentsState.none { fragment -> fragment.mCompass.getAreaFactor(corner) < 1 }) {
//                    corner = it
//                    found = true
//                }
//            }
//
//            if (found){
//                mLogger.writeLog("DEFAULT_TURN to map corner $corner \n")
//                val fixedVertex = gameEngine.getMovementPointForTarget(me.getMainFragment().mId, me.getCoordinates(), corner)
//                return StrategyResult(0, fixedVertex, debugMessage = "DEFAULT: Go TO $corner")
//            }else{
//
//            }
//
//        } else {


//        //TODO:??
        if (gameEngine.worldParseResult.worldObjectsInfo.mEnemies.isNotEmpty()) {
            val enemies = gameEngine.worldParseResult.worldObjectsInfo.mEnemies.filter { enemy -> me.mFragmentsState.any { fragment -> fragment.canBeEatenByEnemy(enemy.mMass) } }
            if (enemies.isNotEmpty()) {
                val fat = enemies.maxBy { it.mMass }
                val target = me.getBestEscapePoint(fat!!.mVertex)


//                val deltaX = enemies[0].mVertex.X - me.getMainFragment().mVertex.X
//                val k = (enemies[0].mVertex.Y - me.getMainFragment().mVertex.Y) / deltaX
//
//                val targetX = if (deltaX > 0) me.getMainFragment().mVertex.X - deltaX else me.getMainFragment().mVertex.X + deltaX
//                val targetY = targetX * (-k)
                return StrategyResult(100, target, debugMessage = "ESCAPE!!!!")
            }
        }

        return StrategyResult(-1, Vertex(0.0f, 0.0f), debugMessage = "Escape: Not applied")
    }

    override fun stopStrategy() {

    }
}