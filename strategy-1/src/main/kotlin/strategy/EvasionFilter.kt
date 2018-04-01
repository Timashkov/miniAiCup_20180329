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
                val xBorder = (2f * enemy.mX - myMinor.mX) / 2f
                if (xBorder > 0) {
                    if (xDirection == -2)
                        xDirection = -1
                    if (xDirection == 1)
                        xDirection = 0

                    parseResult.worldObjectsInfo.mFood = ArrayList(parseResult.worldObjectsInfo.mFood.filter { it.mVertex.X <= xBorder })
                } else {
                    if (xDirection == -2)
                        xDirection = 1
                    if (xDirection == -1)
                        xDirection = 0

                    parseResult.worldObjectsInfo.mFood = ArrayList(parseResult.worldObjectsInfo.mFood.filter { it.mVertex.X >= xBorder })
                }

                val yBorder = (2f * enemy.mY - myMinor.mY) / 2f
                if (yBorder > 0) {
                    if (yDirection == -2)
                        yDirection = -1
                    if (yDirection == 1)
                        yDirection = 0

                    parseResult.worldObjectsInfo.mFood = ArrayList(parseResult.worldObjectsInfo.mFood.filter { it.mVertex.Y <= yBorder })
                } else {
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