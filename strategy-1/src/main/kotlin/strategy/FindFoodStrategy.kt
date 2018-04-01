package strategy

import incominginfos.MineInfo
import incominginfos.WorldObjectsInfo
import WorldConfig
import utils.Logger
import utils.Vertex
import kotlin.math.abs

class FindFoodStrategy(globalConfig: WorldConfig, val mLogger: Logger) : IStrategy {
    private val mFoodMass = globalConfig.FoodMass

    private var mTargetVertex: Vertex? = null
    private var mTargetMass: Float = 0f

    override fun apply(worldInfo: WorldObjectsInfo, mineInfo: MineInfo): StrategyResult {
//если съели целевую точку, а ее соседки остались - то надо доесть
        if (worldInfo.mFood.isNotEmpty()) {
            return analyzePlate(worldInfo, mineInfo)
        }

        mTargetVertex = null
        mTargetMass = 0f
        return StrategyResult(-1.0f, Vertex(0.0f, 0.0f), "FindFood: Not applied")
    }

    private fun analyzePlate(worldInfo: WorldObjectsInfo, mineInfo: MineInfo): StrategyResult {
        if (worldInfo.mFood.map { it.mVertex }.none { it.equals(mTargetVertex) }) {
            mTargetVertex = null
            mTargetMass = 0f
        }
        if (mTargetVertex == null) {
            val result = findBestWay(worldInfo.mFood.map { it.mVertex }, mineInfo.getCoordinates(), mineInfo.getFragmentConfig(mineInfo.getMainfragmentIndex()).mRadius)
            mTargetVertex = result.target
            mTargetMass = mFoodMass * result.points
        }
        return StrategyResult(mTargetMass, mTargetVertex!!, "")
    }

    data class BestWayResult(val target: Vertex, val points: Int)

    fun findBestWay(foodPoints: List<Vertex>, gamerPosition: Vertex, gamerRadius: Float): BestWayResult {


        val sortedByX = foodPoints.sortedBy { it.X }
        val sortedByR = foodPoints.sortedBy { it.distance(gamerPosition) }
        val weights: HashMap<Vertex, Int> = HashMap()
        var points = 0

        sortedByR.forEach { it ->
            points = 0
            val xIndex = sortedByX.indexOf(it)

            if (xIndex > 0) {
                for (i in xIndex - 1 downTo 0) {
                    if (abs(sortedByX[i].X - it.X) < gamerRadius) {
                        if (abs(sortedByX[i].Y - it.Y) < gamerRadius)
                            points++
                    } else
                        break
                }
            }
            if (xIndex < foodPoints.size - 1) {
                for (i in xIndex + 1 until foodPoints.size)
                    if (abs(sortedByX[i].X - it.X) < gamerRadius) {
                        if (abs(sortedByX[i].Y - it.Y) < gamerRadius)
                            points++
                    } else
                        break
            }

            weights[it] = points + 1
        }

        points = 0
        val vertex: ArrayList<Vertex> = ArrayList()
        weights.forEach { it ->
            if (it.value > points) {
                points = it.value
                vertex.clear()
                vertex.add(it.key)
            }
            if (it.value == points)
                vertex.add(it.key)
        }

        if (vertex.size == 0)
            return BestWayResult(vertex[0], points)

        return BestWayResult(vertex[vertex.size / 2], points)
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