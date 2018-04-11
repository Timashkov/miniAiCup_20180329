package strategy

import utils.Logger
import WorldConfig
import data.ParseResult
import utils.Vertex

class WorldObjectsFilter(private val mGlobalConfig: WorldConfig, val mLogger: Logger) {

    // отфильтровать информацию о мире так , чтоб не давать персонажу давать идти в сторону
    // потенциальной опасности

    //TODO: проверить сумму всех фрагментов одного игрока рядом, если она приводит к поеданию - валим!!

    fun onFilter(parseResult: ParseResult): ParseResult {

        val pr = removeUnreachableFood(parseResult)

        val enemies = pr.worldObjectsInfo.mEnemies
        if (enemies.isNotEmpty()) {
            mLogger.writeLog("Enemies total : ${enemies.size}")
            pr.mineInfo.mFragmentsState.forEach { fragment ->
                enemies.filter {
                    fragment.canBeEatenByEnemy(it.mMass) &&
                            it.mVertex.distance(fragment.mVertex) < (it.mRadius * 5f + fragment.mRadius)
                }.forEach { enemy ->
                    mLogger.writeLog("Processed enemy: $enemy")
                    fragment.mCompass.setColorsByEnemies(fragment, enemy)
                }
            }
        }

        val viruses = pr.worldObjectsInfo.mViruses
        if (viruses.isNotEmpty() && pr.mineInfo.mFragmentsState.size < mGlobalConfig.MaxFragsCnt) {
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

        val foodPoints: ArrayList<Vertex> = ArrayList()
        if (food.isNotEmpty())
            foodPoints.addAll(food.map { it.mVertex })
        if (ejections.isNotEmpty())
            foodPoints.addAll(ejections.map { it.mVertex })

        if (pr.worldObjectsInfo.mEnemies.isNotEmpty())
            pr.mineInfo.mFragmentsState.forEach { fragment ->
                val borderDistance = fragment.mRadius * 6f
                if (fragment.mVertex.X < borderDistance) {
                    fragment.mCompass.setColorByEdge(Vertex(0f, fragment.mVertex.Y))
                }
                if (fragment.mVertex.Y < borderDistance) {
                    fragment.mCompass.setColorByEdge(Vertex(fragment.mVertex.X, 0f))
                }
                if (fragment.mVertex.X > mGlobalConfig.GameWidth - borderDistance) {
                    fragment.mCompass.setColorByEdge(Vertex(mGlobalConfig.GameWidth.toFloat(), fragment.mVertex.Y))
                }
                if (fragment.mVertex.Y > mGlobalConfig.GameHeight - borderDistance) {
                    fragment.mCompass.setColorByEdge(Vertex(fragment.mVertex.X, mGlobalConfig.GameWidth.toFloat()))
                }
            }

        pr.mineInfo.reconfigureCompass(foodPoints)
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