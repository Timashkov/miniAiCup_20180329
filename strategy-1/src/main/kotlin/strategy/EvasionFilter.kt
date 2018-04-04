package strategy

import utils.Logger
import WorldConfig
import data.ParseResult

class EvasionFilter(val mGlobalConfig: WorldConfig, val mLogger: Logger) {

    // отфильтровать информацию о мире так , чтоб не давать персонажу давать идти в сторону
    // потенциальной опасности
    fun onFilter(parseResult: ParseResult): ParseResult {

        var pr = removeUnreachableFood(parseResult)

        val enemies = parseResult.worldObjectsInfo.mEnemies
        if (enemies.isNotEmpty()) {
            pr.mineInfo.mFragmentsState.forEach {
                fragment ->
                enemies.filter { fragment.canBeEatenByEnemy(it.mMass) }.forEach { enemy ->
                    mLogger.writeLog("Processed enemy: $enemy")
                    fragment.mCompass.setColorsByEnemies(fragment, enemy)
                }
            }
        }

        val food = parseResult.worldObjectsInfo.mFood
        if (food.isNotEmpty()){
            pr.mineInfo.mFragmentsState.forEach {
                fragment ->
                food.forEach { f ->
                    mLogger.writeLog("Processed f: $f")
                    fragment.mCompass.setColorsByFood(fragment, f)
                }
            }
        }

        val ejections = parseResult.worldObjectsInfo.mEjection
        if (ejections.isNotEmpty()){
            pr.mineInfo.mFragmentsState.forEach {
                fragment ->
                ejections.forEach{ e->
                    mLogger.writeLog("Processed e: $e")
                    fragment.mCompass.setColorsByEjection(fragment, e)
                }
            }
        }

        return pr
    }


    fun removeUnreachableFood(parseResult: ParseResult): ParseResult {
        mLogger.writeLog("removeUnreachableFood")
        val cornerDistance = parseResult.mineInfo.getMainFragment().mRadius * 1.5f
        parseResult.worldObjectsInfo.mFood = ArrayList(parseResult.worldObjectsInfo.mFood.filter {
            it.mVertex.distance(mGlobalConfig.lbCorner) > cornerDistance && it.mVertex.distance(mGlobalConfig.ltCorner) > cornerDistance &&
                    it.mVertex.distance(mGlobalConfig.rbCorner) > cornerDistance && it.mVertex.distance(mGlobalConfig.rtCorner) > cornerDistance
        })
        parseResult.worldObjectsInfo.mEjection = ArrayList(parseResult.worldObjectsInfo.mEjection.filter {
            it.mVertex.distance(mGlobalConfig.lbCorner) > cornerDistance && it.mVertex.distance(mGlobalConfig.ltCorner) > cornerDistance &&
                    it.mVertex.distance(mGlobalConfig.rbCorner) > cornerDistance && it.mVertex.distance(mGlobalConfig.rtCorner) > cornerDistance
        })
        mLogger.writeLog("food removed")
        return parseResult
    }
}