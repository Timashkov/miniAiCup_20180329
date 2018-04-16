package strategy

import utils.Logger
import WorldConfig
import data.ParseResult
import incominginfos.EnemyInfo
import utils.Compass
import utils.Vertex
import java.util.*
import kotlin.math.sqrt

class WorldObjectsFilter(private val mGlobalConfig: WorldConfig, val mLogger: Logger) {

    // отфильтровать информацию о мире так , чтоб не давать персонажу давать идти в сторону
    // потенциальной опасности

    //TODO: проверить сумму всех фрагментов одного игрока рядом, если она приводит к поеданию - валим!!
    val historicalEnemies: ArrayList<EnemyInfo> = ArrayList()

    fun onFilter(parseResult: ParseResult, tickNum: Int): ParseResult {

        val pr = removeUnreachableFood(parseResult)

        val enemies = pr.worldObjectsInfo.mEnemies
        if (enemies.isNotEmpty()) {
            mLogger.writeLog("Enemies total : ${enemies.size}")
            pr.mineInfo.mFragmentsState.forEach { fragment ->
                enemies.forEach { enemy ->
                    mLogger.writeLog("Processed enemy: $enemy")
                    fragment.mCompass.setColorsByEnemies(fragment, enemy)
                }
            }
        }

        if (historicalEnemies.isNotEmpty()) {
            enemies.forEach { ei ->
                historicalEnemies.forEach { he ->
                    if (he.mId == ei.mId) {
                        val info = EnemyInfo(ei.mVertex.plus(ei.mVertex.minus(he.mVertex)), ei.mId, ei.mMass, ei.mRadius)

                        pr.mineInfo.mFragmentsState.forEach { fragment ->
                            mLogger.writeLog("Processed fantom enemy: $info")
                            fragment.mCompass.setColorsByEnemies(fragment, info)
                        }
                    }
                }
            }

            enemies.forEach { ei ->
                historicalEnemies.removeIf { it.mId == ei.mId || (!ei.mId.contains(".") && it.mId.startsWith(ei.mId)) }
            }

            historicalEnemies.removeIf { it.lastSeenTick > 50 }
            historicalEnemies.forEach { he ->
                pr.mineInfo.mFragmentsState.forEach { fragment ->
                    if (he.mVertex.distance(fragment.mVertex) - mGlobalConfig.SpeedFactor / sqrt(fragment.mMass) < (he.mRadius * 5f + fragment.mRadius)) {
                        mLogger.writeLog("Processed historical enemy: $he")
                        fragment.mCompass.setColorsByEnemies(fragment, he)
                    } else {
                        he.lastSeenTick = 50
                    }
                    he.lastSeenTick++
                }
            }
        }
        historicalEnemies.addAll(enemies)

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
                pr.mineInfo.mFragmentsState.none { fragment -> !fragment.mCompass.isVertexInBlackArea(f.mVertex) }
            }
        }

        val ejections = pr.worldObjectsInfo.mEjection.filter { it.pId != 1 }
        if (ejections.isNotEmpty()) {
            mLogger.writeLog("Ejections total : ${ejections.size}")
            pr.worldObjectsInfo.mEjection.filter { ejection ->
                pr.mineInfo.mFragmentsState.none { fragment -> !fragment.mCompass.isVertexInBlackArea(ejection.mVertex) }
            }
        }

        val foodPoints: ArrayList<Vertex> = ArrayList()
        if (food.isNotEmpty())
            foodPoints.addAll(food.map { it.mVertex })
        if (ejections.isNotEmpty())
            foodPoints.addAll(ejections.map { it.mVertex })

        val cornerFactor =
                if (pr.worldObjectsInfo.mEnemies.isNotEmpty()) 5f else 1.5f

        pr.mineInfo.mFragmentsState.forEach { fragment ->
            val cornerDistance = fragment.mRadius * cornerFactor



            if (fragment.mVertex.distance(mGlobalConfig.ltCorner) < cornerDistance) {
                fragment.mCompass.setColorByCorner(mGlobalConfig.ltCorner)
            }
            if (fragment.mVertex.distance(mGlobalConfig.lbCorner) < cornerDistance) {
                fragment.mCompass.setColorByCorner(mGlobalConfig.lbCorner)
            }
            if (fragment.mVertex.distance(mGlobalConfig.rtCorner) < cornerDistance) {
                fragment.mCompass.setColorByCorner(mGlobalConfig.rtCorner)
            }
            if (fragment.mVertex.distance(mGlobalConfig.rbCorner) < cornerDistance) {
                fragment.mCompass.setColorByCorner(mGlobalConfig.rbCorner)
            }
        }
        if (pr.mineInfo.mFragmentsState.any { it.mCompass.hasBlackAreas() }) {
            pr.mineInfo.mFragmentsState.forEach { frag ->
                val points: Array<Vertex> = arrayOf(Vertex(0f, frag.mVertex.Y), Vertex(frag.mVertex.X, 0f), Vertex(mGlobalConfig.GameWidth - frag.mVertex.X, frag.mVertex.Y), Vertex(frag.mVertex.X, mGlobalConfig.GameHeight - frag.mVertex.Y))
                val nearest = points.sortedBy { it.distance(frag.mVertex) }[0]
                frag.mCompass.setColorByVertex(nearest, Compass.CORNER_SECTOR_SCORE)
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