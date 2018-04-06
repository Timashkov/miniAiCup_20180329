package strategy

import utils.Logger
import WorldConfig
import data.ParseResult

class EvasionFilter(val mGlobalConfig: WorldConfig, val mLogger: Logger) {

    // отфильтровать информацию о мире так , чтоб не давать персонажу давать идти в сторону
    // потенциальной опасности
    fun onFilter(parseResult: ParseResult): ParseResult {

        var pr = removeUnreachableFood(parseResult)

        val enemies = pr.worldObjectsInfo.mEnemies
        if (enemies.isNotEmpty()) {
            mLogger.writeLog("Enemies total : ${enemies.size}")
            pr.mineInfo.mFragmentsState.forEach { fragment ->
                enemies.filter { fragment.canBeEatenByEnemy(it.mMass) }.forEach { enemy ->
                    mLogger.writeLog("Processed enemy: $enemy")
                    fragment.mCompass.setColorsByEnemies(fragment, enemy)
                }
            }
        }

        val viruses = pr.worldObjectsInfo.mViruses
        if (viruses.isNotEmpty()) {
            mLogger.writeLog("Viruses total : ${viruses.size}")
            pr.mineInfo.mFragmentsState.forEach { fragment ->
                viruses.filter { fragment.canBurst(it) }.forEach { virus ->
                    mLogger.writeLog("Processed virus: $virus")
                    fragment.mCompass.setColorsByVirus(virus)
                }
            }
        }

        val food = pr.worldObjectsInfo.mFood
        if (food.isNotEmpty()) {
            mLogger.writeLog("Food total : ${food.size}")
            pr.worldObjectsInfo.mFood.filter { f ->
                pr.mineInfo.mFragmentsState.none { fragment -> !fragment.mCompass.setColorsByFood(f) }
            }
        }

        val ejections = pr.worldObjectsInfo.mEjection
        if (ejections.isNotEmpty()) {
            mLogger.writeLog("Ejections total : ${ejections.size}")
            pr.worldObjectsInfo.mEjection.filter { ejection ->
                pr.mineInfo.mFragmentsState.none { fragment -> !fragment.mCompass.setColorsByEjection(ejection) }
            }
        }

        return pr
    }


    private fun removeUnreachableFood(parseResult: ParseResult): ParseResult {
        mLogger.writeLog("removeUnreachableFood")

        val cornerDistance = parseResult.mineInfo.getMainFragment().mRadius * 1.1f

        parseResult.worldObjectsInfo.mFood = parseResult.worldObjectsInfo.mFood.filter {
            !(it.mVertex.X < cornerDistance && it.mVertex.Y < cornerDistance) &&
                    !(it.mVertex.X > mGlobalConfig.GameWidth - cornerDistance && it.mVertex.Y < cornerDistance) &&
                    !(it.mVertex.X > mGlobalConfig.GameWidth - cornerDistance && it.mVertex.Y > mGlobalConfig.GameHeight - cornerDistance) &&
                    !(it.mVertex.X < cornerDistance && it.mVertex.Y > mGlobalConfig.GameHeight - cornerDistance)
        }
        parseResult.worldObjectsInfo.mEjection = ArrayList(parseResult.worldObjectsInfo.mEjection.filter {
            !(it.mVertex.X < cornerDistance && it.mVertex.Y < cornerDistance) &&
                    !(it.mVertex.X > mGlobalConfig.GameWidth - cornerDistance && it.mVertex.Y < cornerDistance) &&
                    !(it.mVertex.X > mGlobalConfig.GameWidth - cornerDistance && it.mVertex.Y > mGlobalConfig.GameHeight - cornerDistance) &&
                    !(it.mVertex.X < cornerDistance && it.mVertex.Y > mGlobalConfig.GameHeight - cornerDistance)
        })

        mLogger.writeLog("food removed")
        return parseResult
    }
}