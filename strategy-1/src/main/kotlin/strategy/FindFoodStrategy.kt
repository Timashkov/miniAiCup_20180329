package strategy

import incominginfos.MineInfo
import incominginfos.WorldObjectsInfo
import WorldConfig
import utils.Logger
import utils.Vertex
import kotlin.math.abs

class FindFoodStrategy(val mGlobalConfig: WorldConfig, val mLogger: Logger) : IStrategy {
    data class BestWayResult(val target: Vertex, val foodPoints: List<Vertex>)

    private val mFoodMass = mGlobalConfig.FoodMass
    private var mTargetWay: BestWayResult? = null
    private var mGamerStateCache: MineInfo? = null

    override fun apply(worldInfo: WorldObjectsInfo, mineInfo: MineInfo): StrategyResult {

        if (worldInfo.mFood.isNotEmpty()) {
            analyzePlate(worldInfo, mineInfo)
            mGamerStateCache = mineInfo

            if (mineInfo.mFragmentsState.size == 1 && mineInfo.getMainFragment().mMass > 120) {
                var nearestViruses = worldInfo.mViruses.filter {
                    it.mVertex.distance(mineInfo.getCoordinates()) <= mineInfo.getMainFragment().mRadius * 2f
                }.sortedBy { it.mVertex.distance(mineInfo.getMainFragment().mVertex) }
                if (nearestViruses.isNotEmpty() && nearestViruses[0].mVertex.distance(mineInfo.getMainFragment().mVertex) < mTargetWay!!.target.distance(mineInfo.getMainFragment().mVertex)) {
                    return StrategyResult(2f, nearestViruses[0].mVertex)
                }
            }

            var split = mineInfo.getMainFragment().mMass > 120 * 3
            return StrategyResult(mTargetWay!!.foodPoints.size * mFoodMass, mTargetWay!!.target, split = split)
        }

        mTargetWay = null
        return StrategyResult(-1.0f, Vertex(0.0f, 0.0f), debugMessage = "FindFood: Not applied")
    }

    override fun stopStrategy() {
        mTargetWay = null
        mGamerStateCache = null
    }

    private fun analyzePlate(worldInfo: WorldObjectsInfo, mineInfo: MineInfo) {

        if (mTargetWay != null && (isDestinationAchieved(worldInfo) || isGamerStateChanged(mineInfo))) {
            mTargetWay = null
        }

        if (mTargetWay == null) {
            mTargetWay = findBestWay(worldInfo.mFood.map { it.mVertex }, mineInfo.getCoordinates(), mineInfo.getMainFragment().mRadius)
        }
    }

    private fun isGamerStateChanged(gamerInfo: MineInfo): Boolean {
        mGamerStateCache?.let { cached ->
            if (cached.mFragmentsState.size != gamerInfo.mFragmentsState.size)
                return true
            if (cached.getMainFragment().mRadius > gamerInfo.getMainFragment().mRadius * 0.95f)
                return true
        }
        return false
    }

    private fun isDestinationAchieved(worldInfo: WorldObjectsInfo): Boolean {
        //если съели целевую точку, а ее соседки остались - то надо доесть

        mTargetWay?.let { targetWay ->
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

    fun findBestWay(foodPoints: List<Vertex>, gamerPosition: Vertex, gamerRadius: Float): BestWayResult {


        val sortedByX = foodPoints.sortedBy { it.X }
        val sortedByR = foodPoints.sortedBy { it.distance(gamerPosition) }
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

        var points = 0
        val vertex: ArrayList<Vertex> = ArrayList()
        weights.forEach { it ->
            if (it.value.size > points) {
                points = it.value.size
                vertex.clear()
                vertex.add(it.key)
            }
            if (it.value.size == points)
                vertex.add(it.key)
        }

        if (vertex.size == 1)
            return BestWayResult(vertex[0], weights[vertex[0]]!!)

        var nearest = sortedByR.last()
        vertex.forEach { vert ->
            if (sortedByR.indexOf(vert) < sortedByR.indexOf(nearest))
                nearest = vert
        }

        return BestWayResult(nearest, weights[nearest]!!)
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