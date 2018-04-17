package strategy

import utils.Logger
import WorldConfig
import WorldConfig.Companion.FOW_RADIUS_FACTOR
import data.ParseResult
import incominginfos.EnemyInfo
import utils.Compass
import utils.Square
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
                viruses.filter { fragment.canBurst(it) && fragment.mVertex.distance(it.mVertex) < fragment.mRadius * FOW_RADIUS_FACTOR }.forEach { virus ->
                    mLogger.writeLog("Processed virus: $virus")
                    fragment.mCompass.setColorsByVirus(virus)
                }
            }
        }


        val food = pr.worldObjectsInfo.mFood
        if (food.isNotEmpty()) {
            mLogger.writeLog("Food total : ${food.size}")

            if (pr.worldObjectsInfo.mFood.size > 2) {
                // memorize in case of area contains at least 3 food objects
                pr.worldObjectsInfo.mFood.forEach {
                    val tempV = it.mVertex.minus(mGlobalConfig.getCenter())
                    pr.phantomFood.add(Vertex(tempV.X + mGlobalConfig.getCenter().X, -tempV.Y + mGlobalConfig.getCenter().Y))
                    pr.phantomFood.add(Vertex(-tempV.X + mGlobalConfig.getCenter().X, -tempV.Y + mGlobalConfig.getCenter().Y))
                    pr.phantomFood.add(Vertex(-tempV.X + mGlobalConfig.getCenter().X, tempV.Y + mGlobalConfig.getCenter().Y))
                }
            }

            pr.worldObjectsInfo.mFood = pr.worldObjectsInfo.mFood.filter { f ->
                pr.mineInfo.mFragmentsState.none { fragment -> !fragment.mCompass.isVertexInBlackArea(f.mVertex) }
                        && pr.worldObjectsInfo.mEnemies.none { en -> en.mVertex.distance(f.mVertex) < pr.mineInfo.getNearestFragment(f.mVertex).mVertex.distance((f.mVertex)) * 2f / 3f }
            }
        }

        while (pr.phantomFood.size > 50) {
            pr.phantomFood.removeAt(0)
        }

        val ejections = pr.worldObjectsInfo.mEjection.filter { it.pId != 1 }
        if (ejections.isNotEmpty()) {
            mLogger.writeLog("Ejections total : ${ejections.size}")
            pr.worldObjectsInfo.mEjection = pr.worldObjectsInfo.mEjection.filter { e ->
                pr.mineInfo.mFragmentsState.none { fragment -> !fragment.mCompass.isVertexInBlackArea(e.mVertex) }
                        && pr.worldObjectsInfo.mEnemies.none { en -> en.mVertex.distance(e.mVertex) < pr.mineInfo.getNearestFragment(e.mVertex).mVertex.distance((e.mVertex)) * 2f / 3f }
            }
        }

        val foodPoints: ArrayList<Vertex> = ArrayList()
        if (food.isNotEmpty())
            foodPoints.addAll(food.map { it.mVertex })
        if (ejections.isNotEmpty())
            foodPoints.addAll(ejections.map { it.mVertex })

        val cornerFactor =
                if (pr.worldObjectsInfo.mEnemies.isNotEmpty()) mGlobalConfig.GameHeight / 4f * 1.44f else 1.5f

        pr.mineInfo.mFragmentsState.forEach { fragment ->
            val cornerDistance = if (cornerFactor > 1.5f) cornerFactor else fragment.mRadius * cornerFactor

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
                val points: Array<Vertex> = arrayOf(Vertex(0f, frag.mVertex.Y), Vertex(mGlobalConfig.GameWidth.toFloat(), frag.mVertex.Y), Vertex(frag.mVertex.X, 0f), Vertex(frag.mVertex.X, mGlobalConfig.GameHeight.toFloat()))
                val nearest = points.sortedBy { it.distance(frag.mVertex) }[0]
                val square = Square(mGlobalConfig.getCenter(), mGlobalConfig.GameWidth / 4f)
                if (!square.isInSquare(frag.mVertex))
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