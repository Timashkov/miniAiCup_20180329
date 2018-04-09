package incominginfos

import org.json.JSONArray
import utils.Vertex
import WorldConfig
import utils.Compass
import utils.Logger
import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.math.tan

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

    data class sectorsSet(var sectorNum: Int, var sectorsCount: Int)

    // пытаемся покинуть зону в сторону границы карты
    fun getBestEscapePoint(): Vertex {

        mLogger.writeLog("Looking for best escape sector")
        val rumbHash = HashMap<Float, Int>()
        mFragmentsState.forEach { fr ->
            fr.mCompass.mRumbBorders.forEach { rumb ->
                if (rumbHash.containsKey(rumb.majorBorder))
                    rumbHash[rumb.majorBorder] = rumbHash[rumb.majorBorder]!! + rumb.areaFactor
                else
                    rumbHash[rumb.majorBorder] = rumb.areaFactor
            }
        }
        val sector = rumbHash.maxBy { it.value }
        mLogger.writeLog("Sector $sector")

//        var first = -16
//        var count = 0
//        val rr = ArrayList<sectorsSet>()
//        for (i in -15..16) {
//            val border = 11.25f * i
//            if (rumbHash.containsKey(border) && rumbHash[border]!! > 0) {
//                if (first > -16) {
//                    count++
//                } else {
//                    first = i
//                    count++
//                }
//            } else {
//                if (first > -16) {
//                    rr.add(sectorsSet(first, count))
//                    first = -16
//                    count = 0
//                }
//            }
//        }
//        if (first > -16) {
//            // last sector still +
//            if (rr[0].sectorNum == -15) {
//                rr[0].sectorNum = first
//                rr[0].sectorsCount += count
//            } else
//                rr.add(sectorsSet(first, count))
//        }
//
//        val secIndex = rr.maxBy { it -> it.sectorsCount }
//        val targetIndex = secIndex!!.sectorNum + secIndex.sectorsCount / 2

        val targetVertex = getMinorFragment().mCompass.getMapEdgeBySector(sector!!.key)
//        val targetVertex = getMinorFragment().mCompass.getMapEdgeBySector(targetIndex * 11.25f)


        return targetVertex
//        val modX = distance / sqrt(K * K + 1)

//        if (rightPart){
//            val y = K * globalConfig.GameWidth
//            if (y)
//        }
//        val Y = K * X
//
//        mLogger.writeLog("K=$K X=$X Y=$Y")
//        return Vertex(source.X + X, source.Y + Y)
    }

}

//{\"Mine\":[{\"Id\":\"1\",\"M\":40,\"R\":12.649110640673518,\"SX\":0,\"SY\":0,\"TTF\":32,\"X\":474,\"Y\":178}]