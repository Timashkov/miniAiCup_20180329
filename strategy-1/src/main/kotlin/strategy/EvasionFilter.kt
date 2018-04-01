package strategy

import utils.Logger
import WorldConfig
import data.ParseResult

class EvasionFilter(val mGlobalConfig: WorldConfig, val mLogger: Logger) {

    // отфильтровать информацию о мире так , чтоб не давать персонажу давать идти в сторону
    // потенциальной опасности
    fun onFilter(parseResult: ParseResult): ParseResult {
        val myMinor = parseResult.mineInfo.getMinorFragment()
        val enemies = parseResult.worldObjectsInfo.mEnemies
        var xDirection = -2
        var yDirection = -2
        if (enemies.isNotEmpty()) {
            enemies.forEach { enemy ->
                mLogger.writeLog("Found enemy: $enemy")
                if (enemy.mMass <= myMinor.mMass * 1.2)
                    return@forEach

                mLogger.writeLog("Processed enemy: $enemy")
                if (enemy.mVertex.X > myMinor.mVertex.X){
                    val xBorder = enemy.mVertex.X - enemy.mRadius
                    if (xDirection == -2)
                        xDirection = -1
                    if (xDirection == 1)
                        xDirection = 0

                    parseResult.worldObjectsInfo.mFood = ArrayList(parseResult.worldObjectsInfo.mFood.filter { it.mVertex.X <= xBorder })
                } else {
                    val xBorder = enemy.mVertex.X + enemy.mRadius
                    if (xDirection == -2)
                        xDirection = 1
                    if (xDirection == -1)
                        xDirection = 0

                    parseResult.worldObjectsInfo.mFood = ArrayList(parseResult.worldObjectsInfo.mFood.filter { it.mVertex.X >= xBorder })
                }

                if (enemy.mVertex.Y > myMinor.mVertex.Y){
                    val yBorder = enemy.mVertex.Y - enemy.mRadius
                    if (yDirection == -2)
                        yDirection = -1
                    if (yDirection == 1)
                        yDirection = 0

                    parseResult.worldObjectsInfo.mFood = ArrayList(parseResult.worldObjectsInfo.mFood.filter { it.mVertex.Y <= yBorder })
                }else{
                    val yBorder = enemy.mVertex.Y + enemy.mRadius
                    if (yDirection == -2)
                        yDirection = 1
                    if (yDirection == -1)
                        yDirection = 0

                    parseResult.worldObjectsInfo.mFood = ArrayList(parseResult.worldObjectsInfo.mFood.filter { it.mVertex.Y >= yBorder })
                }
            }
        }

        return parseResult
    }
}