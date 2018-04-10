package incominginfos

import org.json.JSONArray
import utils.Vertex
import WorldConfig
import data.FoodPoint
import utils.Compass
import utils.Logger
import utils.Square

class MineInfo(stateJson: JSONArray, val globalConfig: WorldConfig, val mLogger: Logger) {
    val mFragmentsState: Array<MineFragmentInfo>

    val mMainFragmentIndex: Int
    val mSmallestFragmentIndex: Int

    val canSplit: Boolean
        get() = globalConfig.MaxFragsCnt > mFragmentsState.size && mFragmentsState.any { it.canSplit }

    enum class Direction {
        TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT, STAYS
    }

    init {
        mLogger.writeLog("Start parse mine info")
        mFragmentsState = Array(stateJson.length(), { it -> MineFragmentInfo(stateJson.getJSONObject(it), globalConfig) })
        mMainFragmentIndex = getMainFragmentIndex()
        mSmallestFragmentIndex = getSmallestFragmentIndex()
        mLogger.writeLog("Mine info parsed")
    }

    fun isNotEmpty(): Boolean {
        return mFragmentsState.isNotEmpty()
    }

    fun getFragmentConfig(index: Int): MineFragmentInfo = mFragmentsState[index]

    fun getCoordinates(): Vertex {
        return getMainFragment().mVertex
    }

    fun getDirection(): Direction {
        var Sx = 0f
        var Sy = 0f
        mFragmentsState.forEach {
            Sx += it.mSX
            Sy += it.mSY
        }
        if (Sx + Sy == 0f)
            return Direction.STAYS
        if (Sx > 0) {
            if (Sy > 0)
                return Direction.BOTTOM_RIGHT
            return Direction.TOP_RIGHT
        }
        if (Sy > 0)
            return Direction.BOTTOM_LEFT
        return Direction.TOP_LEFT
    }

    fun getMainFragment(): MineFragmentInfo = mFragmentsState[mMainFragmentIndex]
    fun getMinorFragment(): MineFragmentInfo = mFragmentsState[mSmallestFragmentIndex]

    private fun getMainFragmentIndex(): Int {
        if (mFragmentsState.size == 1)
            return 0
        var maxWeight = 0f
        var fatCount = 0
        var fattestIndex = 0
        mFragmentsState.forEach {
            if (it.mMass > maxWeight) {
                fattestIndex = mFragmentsState.indexOf(it)
                fatCount = 1
            } else if (it.mMass == maxWeight)
                fatCount++
        }
        if (fatCount == 1)
            return fattestIndex

        val fats = mFragmentsState.filter { it.mMass == maxWeight }
        return fats.size / 2
    }

    private fun getSmallestFragmentIndex(): Int {
        if (mFragmentsState.size == 1)
            return 0
        var minWeight = 20000f
        var thinCount = 0
        var thinnestIndex = 0
        mFragmentsState.forEach {
            if (it.mMass < minWeight) {
                thinnestIndex = mFragmentsState.indexOf(it)
                thinCount = 1
            } else if (it.mMass == minWeight)
                thinCount++
        }
        if (thinCount == 1)
            return thinnestIndex

        val fats = mFragmentsState.filter { it.mMass == minWeight }
        return fats.size / 2
    }

    fun shortestDistanceTo(target: Vertex): Float {
        return mFragmentsState.map { it.mVertex.distance(target) }.min()!!
    }

    fun getFragmentById(fragmentId: String): MineFragmentInfo {
        val f = mFragmentsState.firstOrNull { it.mId == fragmentId } ?: return getMainFragment()
        return f
    }

    fun getNearestFragment(target: Vertex): MineFragmentInfo {
        return mFragmentsState.sortedBy { it.mVertex.distance(target) }[0]
    }

//    fun getBestMovementPoint(): FoodPoint {
//
//        mLogger.writeLog("Looking for best escape sector")
//        val currentCompass = Compass(getMinorFragment(), globalConfig, true)
//
//        mFragmentsState.forEach { fr ->
//            currentCompass.mergeCompass(fr.mCompass, 1f)
//        }
//
//        val sector = currentCompass.mRumbBorders.maxBy { it.areaScore } ?: return FoodPoint.DEFAULT
//        mLogger.writeLog("Sector $sector")
//
//        if (sector.areaScore == Compass.DEFAULT_AREA_SCORE * mFragmentsState.size)
//            return FoodPoint.DEFAULT
//        return currentCompass.getSectorFoodPoint(sector)
//    }

    fun getBestMovementPoint(): FoodPoint {
        mLogger.writeLog("Looking for best sector")

        mFragmentsState.sortedBy { fr -> fr.mCompass.mRumbBorders.maxBy { rumb-> rumb.areaScore } !! }.forEach { fr ->
            fr.mCompass.mRumbBorders.filter { rumb -> rumb.areaScore > Compass.DEFAULT_AREA_SCORE }.forEach { rumb ->
                val fp = fr.mCompass.getSectorFoodPoint(rumb)
                mLogger.writeLog("BS: $fp")
                if (mFragmentsState.none { state -> state.mCompass.isVertexInDangerArea(fp.target) }){
                    return fp
                }
            }
        }

        return FoodPoint.DEFAULT
    }


    fun reconfigureCompass(foodPoints: ArrayList<Vertex>) {
        mFragmentsState.forEach { it.reconfigureCompass(foodPoints) }
    }

    private fun getQuart(vertex: Vertex, center: Vertex): Int {
        if (vertex.X <= center.X) {
            if (vertex.Y <= center.Y)
                return 1
            return 4
        }
        if (vertex.Y <= center.Y) {
            return 2
        }
        return 3
    }

}

//{\"Mine\":[{\"Id\":\"1\",\"M\":40,\"R\":12.649110640673518,\"SX\":0,\"SY\":0,\"TTF\":32,\"X\":474,\"Y\":178}]