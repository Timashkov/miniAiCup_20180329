package strategy

import incominginfos.MineInfo
import incominginfos.WorldObjectsInfo
import WorldConfig
import data.ParseResult
import utils.GameEngine
import utils.Logger
import utils.Vertex
import kotlin.math.abs

class FindFoodStrategy(val mGlobalConfig: WorldConfig, val mLogger: Logger) : IStrategy {
    data class BestWayResult(val target: Vertex, val foodPoints: List<Vertex>, val fragmentId: String)

    private var mBestKnownWay: BestWayResult? = null
    private var mGamerStateCache: MineInfo? = null

    override fun apply(gameEngine: GameEngine, cachedParseResult: ParseResult?): StrategyResult {

        val food = gameEngine.worldParseResult.worldObjectsInfo.mFood

        if (food.isEmpty()) {
            mBestKnownWay = null
            return StrategyResult(-1, Vertex(0.0f, 0.0f), debugMessage = "FindFood: Not applied")
        } else {
            analyzePlate(gameEngine)

            try {
                mBestKnownWay?.let { bestWay ->
                    val me = gameEngine.worldParseResult.mineInfo
                    mGamerStateCache = me
                    val viruses = gameEngine.worldParseResult.worldObjectsInfo.mViruses

                    if (me.mFragmentsState.size == 1 && me.getMainFragment().canSplit && gameEngine.currentTick < 1000) {
                        val nearestViruses = viruses.filter {
                            it.mVertex.distance(me.getCoordinates()) <= me.getMainFragment().mRadius * 2f
                        }.sortedBy { it.mVertex.distance(me.getMainFragment().mVertex) }
                        if (nearestViruses.isNotEmpty() && nearestViruses[0].mVertex.distance(me.getMainFragment().mVertex) < bestWay.target.distance(me.getMainFragment().mVertex)) {
                            return StrategyResult(2, nearestViruses[0].mVertex)
                        }
                    }

                    if (me.getMainFragment().mMass > WorldConfig.MIN_SPLITABLE_MASS && me.mFragmentsState.none { fragment -> fragment.mCompass.isVertexInDangerArea(bestWay.target) }) {
                        mLogger.writeLog("$DEBUG_TAG movementTarget ${bestWay.target} and split for FOOD: ${bestWay.target}\n")
                        return StrategyResult(bestWay.foodPoints.size, bestWay.target, split = true, debugMessage = "Debug : get food with split")
                    } else {
                        val movementTarget = gameEngine.getMovementPointForTarget(bestWay.fragmentId, me.getCoordinates(), bestWay.target)
                        mLogger.writeLog("$DEBUG_TAG movementTarget $movementTarget  for FOOD: ${bestWay.target}\n")
                        return StrategyResult(bestWay.foodPoints.size, movementTarget)
                    }
                }
            } catch (e: Exception) {
                mLogger.writeLog("Fault on apply food $e")
            }

            mLogger.writeLog("$DEBUG_TAG Find food is not applied \n")
            return StrategyResult(-1, Vertex(0.0f, 0.0f), debugMessage = "FindFood: Not applied")
        }
    }

    override fun stopStrategy() {
        mBestKnownWay = null
        mGamerStateCache = null
    }

    private fun analyzePlate(gameEngine: GameEngine) {

        try {
            if (mBestKnownWay != null && isDestinationAchieved(gameEngine.worldParseResult.worldObjectsInfo) || isGamerStateChanged(gameEngine.worldParseResult.mineInfo)) {
                mBestKnownWay = null
            }

            if (mBestKnownWay != null && gameEngine.worldParseResult.mineInfo.mFragmentsState.any { it.mCompass.isVertexInBlackArea(mBestKnownWay!!.target) }) {
                mLogger.writeLog("$DEBUG_TAG fragment in black area")
                mBestKnownWay = null
            }

            if (mBestKnownWay != null && gameEngine.worldParseResult.mineInfo.mFragmentsState.any { it.mVertex == mBestKnownWay!!.target }) {
                mLogger.writeLog("$DEBUG_TAG one fragment on the point now")
                mBestKnownWay = null
            }

            if (mBestKnownWay == null) {
                mBestKnownWay = findBestWay(gameEngine.worldParseResult.worldObjectsInfo.mFood.map { it.mVertex }, gameEngine.worldParseResult.mineInfo, gameEngine.worldParseResult.mineInfo.getMainFragment().mRadius)
            }
        } catch (e: Exception) {
            mLogger.writeLog("Fault on analyze plate $e")
        }
    }

    private fun isGamerStateChanged(gamerInfo: MineInfo): Boolean {
        mGamerStateCache?.let { cached ->
            if (cached.mFragmentsState.size != gamerInfo.mFragmentsState.size) {
                mLogger.writeLog("$DEBUG_TAG state changed - fragments count")
                return true
            }
        }
        return false
    }

    private fun isDestinationAchieved(worldInfo: WorldObjectsInfo): Boolean {
        //если съели целевую точку, а ее соседки остались - то надо доесть

        mBestKnownWay?.let { targetWay ->
            var c = 0
            targetWay.foodPoints.forEach { fp ->
                if (worldInfo.mFood.map { it.mVertex }.any { it.equals(fp) }) {
                    mLogger.writeLog("FP: $fp")
                    c++
                }
            }
            if (c > 0)
                return false
        }
        return true
    }

    fun findBestWay(foodPoints: List<Vertex>, gamerInfo: MineInfo, gamerRadius: Float): BestWayResult? {

        // add ejects
        val sortedByX = foodPoints.sortedBy { it.X }
        val sortedByR = foodPoints.sortedBy { gamerInfo.shortestDistanceTo(it) }
        val weights: HashMap<Vertex, ArrayList<Vertex>> = HashMap()

        sortedByR.forEach { it ->
            val verts: ArrayList<Vertex> = ArrayList()
            verts.add(it)
            val xIndex = sortedByX.indexOf(it)

            if (xIndex > 0) {
                for (i in xIndex - 1 downTo 0) {
                    if (abs(sortedByX[i].X - it.X) < gamerRadius) {
                        if (it.distance(sortedByX[i]) < gamerRadius * 0.9f)
                            verts.add(sortedByX[i])
                    } else
                        break
                }
            }
            if (xIndex < foodPoints.size - 1) {
                for (i in xIndex + 1 until foodPoints.size)
                    if (abs(sortedByX[i].X - it.X) < gamerRadius) {
                        if (it.distance(sortedByX[i]) < gamerRadius * 0.9f)
                            verts.add(sortedByX[i])
                    } else
                        break
            }

            weights[it] = verts
        }

        var maxPoints = 0
        val vertex: ArrayList<Vertex> = ArrayList()
        weights.forEach { it ->

            var factor = 0
            gamerInfo.mFragmentsState.forEach { fragment -> factor += fragment.mCompass.getAreaFactor(it.key) }
            if (factor > 0) {
                if (it.value.size > maxPoints) {
                    maxPoints = it.value.size * factor
                    vertex.clear()
                    vertex.add(it.key)
                }
                if (it.value.size * factor == maxPoints)
                    vertex.add(it.key)
            }
        }

        if (maxPoints < 1) {
            return null
        }

        if (vertex.size == 1)
            return BestWayResult(vertex[0], weights[vertex[0]]!!, gamerInfo.getNearestFragment(vertex[0]).mId)

        var nearest = sortedByR.last()
        vertex.forEach { vert ->
            if (sortedByR.indexOf(vert) < sortedByR.indexOf(nearest))
                nearest = vert
        }

        return BestWayResult(nearest, weights[nearest]!!, gamerInfo.getNearestFragment(vertex[0]).mId)

    }

    companion object {
        val DEBUG_TAG = "FIND_FOOD"
    }
}
/*
* 0 = {HashMap$Node@895} "Vertex(X=7.0, Y=10.0)" -> "26.791246"
1 = {HashMap$Node@896} "Vertex(X=1.0, Y=8.0)" -> "32.882454"
2 = {HashMap$Node@897} "Vertex(X=10.0, Y=5.0)" -> "18.731998"
3 = {HashMap$Node@898} "Vertex(X=3.0, Y=2.0)" -> "29.395622"
4 = {HashMap$Node@899} "Vertex(X=1.0, Y=2.0)" -> "16.625317"
5 = {HashMap$Node@900} "Vertex(X=2.0, Y=1.0)" -> "18.929482"
6 = {HashMap$Node@901} "Vertex(X=3.0, Y=0.0)" -> "13.074637"
7 = {HashMap$Node@902} "Vertex(X=3.0, Y=14.0)" -> "29.638264"
8 = {HashMap$Node@903} "Vertex(X=10.0, Y=3.0)" -> "18.970562"
9 = {HashMap$Node@904} "Vertex(X=9.0, Y=4.0)" -> "16.224049"*/


/*
* val first = parseResult.mineInfo.getFragmentConfig(0)
            val food = findFood(parseResult.worldObjectsInfo)
                    ?: return JSONObject(mapOf("X" to 0, "Y" to 0, "Debug" to "No Food"))
            return JSONObject(mapOf<String, Any>("X" to food.mX, "Y" to food.mY))
* */

/*
* 1) еда одна ? - идем
* 2) выбор группы или одной ( критерии - кол-во и удаленность )
* 3) могу ли я достигнуть выгодной группы за один шаг? - идем
* 4) есть ли в напр группы еще еда , достижимая за один шаг? - идем к еде
* 5) идем к группе
*
* fix bug: ширина рожи не позволяет залезть в угол за едой
*
* */