package incominginfos

import org.json.JSONArray
import utils.Vertex
import WorldConfig
import data.MovementVector
import data.StepPoint
import utils.Compass
import utils.GameEngine
import utils.Logger
import kotlin.math.pow
import kotlin.math.sqrt

class MineInfo(stateJson: JSONArray, val globalConfig: WorldConfig, val mLogger: Logger) {
    val mFragmentsState: Array<MineFragmentInfo>

    val mMainFragmentIndex: Int
    val mSmallestFragmentIndex: Int

    val canSplit: Boolean
        get() = globalConfig.MaxFragsCnt > mFragmentsState.size && mFragmentsState.any { it.canSplit }

    val totalMass: Float
        get() = mFragmentsState.sumByDouble { it.mMass.toDouble() }.toFloat()

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

/*    val viruses = gameEngine.worldParseResult.worldObjectsInfo.mViruses

                if (me.mFragmentsState.size == 1 && me.getMainFragment().canSplit && gameEngine.currentTick < 1000) {
                    val nearestViruses = viruses.filter {
                        it.mVertex.distance(me.getCoordinates()) <= me.getMainFragment().mRadius * 2f
                    }.sortedBy { it.mVertex.distance(me.getMainFragment().mVertex) }
                    if (nearestViruses.isNotEmpty() && nearestViruses[0].mVertex.distance(me.getMainFragment().mVertex) < bestWay.target.distance(me.getMainFragment().mVertex)) {
                        return StrategyResult(2, nearestViruses[0].mVertex)
                    }
                }
*/

    fun getBestMovementPoint(knownVertex: StepPoint?, cachedState: MineInfo?): StepPoint {

        mLogger.writeLog("Looking for best sector. Known target = $knownVertex")
        knownVertex?.let { fp ->
            val knownFragment = mFragmentsState.firstOrNull { it.mId == fp.fragmentId }
            knownFragment?.let { targetFragment ->

                mLogger.writeLog("Known vertex is not null")
                if (cachedState != null) {
                    val cachedFragment = cachedState.mFragmentsState.firstOrNull { it.mId == fp.fragmentId }
                    if (cachedFragment != null && cachedFragment.mVertex.distance(fp.target) > knownFragment.mVertex.distance(fp.target) * 2f && knownFragment.mCompass.hasBlackAreas()) {
                        mLogger.writeLog("distance is less than x2")
                        return@let
                    }
                }

                if (fp.foodPoints.size == 1 && globalConfig.outOfMap(fp.foodPoints[0])) {
                    mLogger.writeLog("food point out of map")
                    return@let
                }

                if (mFragmentsState.none { it.mCompass.isVertexInBlackArea(fp.target) } && mFragmentsState.none { it.mVertex == fp.target }) {
                    if (mFragmentsState.size > 1 && mFragmentsState.all { it.mTTF < 2 } && mFragmentsState.any { it.mCompass.hasBlackAreas() })
                        return knownVertex

                    knownVertex.movementTarget = getMovementPointForTarget(knownFragment, knownVertex.target)
                    return knownVertex
                }
            }
        }

        var fragmentsList = mFragmentsState.sortedByDescending { fr -> fr.mCompass.getMaxSectorScore() }

        if (mFragmentsState.any { it -> it.mCompass.hasDarkAreas() }) {
            fragmentsList = mFragmentsState.sortedByDescending { fr -> fr.mCompass.getDangerSectorsCount() }

        }
        val alreadyProcessed: ArrayList<MineFragmentInfo> = ArrayList()

        fragmentsList.forEach { fr ->
            fr.mCompass.mRumbBorders.filter { rumb ->
                rumb.areaScore > Compass.DEFAULT_AREA_SCORE ||
                        rumb.lastEscapePoint ||
                        (rumb.areaScore == Compass.DEFAULT_AREA_SCORE && fr.mCompass.hasBlackAreas())
            }.sortedByDescending { it.areaScore }.forEach { rumb ->
                val fp = fr.mCompass.getSectorFoodPoint(rumb)
                if (fp != StepPoint.DEFAULT) {

                    mLogger.writeLog("BS: $fp")
                    fp.movementTarget = getMovementPointForTarget(fr, fp.target)

                    if (rumb.lastEscapePoint) {
                        if (rumb.lastEscapePoint && mFragmentsState.size == 1) {
                            if (fr.canSplit && fr.mCompass.getRumbIndexByVector(MovementVector(fr.mSX, fr.mSY)) == fr.mCompass.mRumbBorders.indexOf(rumb))
                                fp.useSplit = true
                            else
                                fp.useEjections = true
                        }
                        return fp
                    }

                    if (mFragmentsState.size > 1 && mFragmentsState.all { it.mTTF < 2 } && mFragmentsState.any { it.mCompass.hasBlackAreas() }) {
//                    val enemies: ArrayList<EnemyInfo> = ArrayList()
//                    mFragmentsState.forEach { it-> it.mCompass.mRumbBorders.forEach { r -> enemies.addAll(r.canBeEatenByEnemy) } }
//
                        var x = 0.0f
                        var y = 0.0f
                        mFragmentsState.forEach {
                            x += it.mVertex.X
                            y += it.mVertex.Y
                        }
                        x /= mFragmentsState.size
                        y /= mFragmentsState.size
                        val center = Vertex(x, y)
                        if (mFragmentsState.none { it !in alreadyProcessed && it.mCompass.isVertexInDangerArea(center) }) {
                            fp.target = center
                            fp.movementTarget = center
                            return fp
                        }
                    }

                    if (mFragmentsState.none { state -> state !in alreadyProcessed && state.mCompass.isVertexInDangerArea(fp.target) }) {

                        if (mFragmentsState.none { state -> state.mCompass.isVertexInAreaWithEnemy(fp.movementTarget) }) {
                            fp.useSplit = fr.maySplit && mFragmentsState.none { it.mCompass.hasBlackAreas() } && mFragmentsState.none { frag -> frag.mCompass.mRumbBorders.any { rumb -> rumb.enemies.any { en -> en.mVertex.distance(frag.mVertex) < frag.mRadius * WorldConfig.FOW_RADIUS_FACTOR / 2 } } } && !getMainFragment().mCompass.isVertexInAreaWithEnemy(getMainFragment().mVertex.plus(Vertex(getMainFragment().mSX, getMainFragment().mSY)))
                        }
                        return fp
                    }
                }
            }
            alreadyProcessed.add(fr)
        }

        fragmentsList.forEach { fr ->
            if (fr.mCompass.hasBlackAreas() && fr.mCompass.mRumbBorders.none { it.areaScore > 0 }) {
                val escape = fr.mCompass.mRumbBorders.filter { rumb ->
                    rumb.areaScore >= Compass.CORNER_SECTOR_SCORE
                }[0]
                val fp = fr.mCompass.getSectorFoodPoint(escape)
                mLogger.writeLog("BS: $fp")
                fp.movementTarget = getMovementPointForTarget(fr, fp.target)
                if (fr.canSplit && fr.mCompass.getRumbIndexByVector(MovementVector(fr.mSX, fr.mSY)) == fr.mCompass.mRumbBorders.indexOf(escape))
                    fp.useSplit = true
                else
                    fp.useEjections = true
            }
        }

        return StepPoint.DEFAULT
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

    fun getMovementPointForTarget(fragment: MineFragmentInfo, target: Vertex): Vertex {

        mLogger.writeLog("${GameEngine.DEBUG_TAG} $fragment")
        val sVector = MovementVector(fragment.mSX, fragment.mSY)

        //for one fragment

        mLogger.writeLog("${GameEngine.DEBUG_TAG} $sVector ${fragment.mVertex} $target")

        val inertionK = globalConfig.InertionFactor / fragment.mMass
        mLogger.writeLog("${GameEngine.DEBUG_TAG} inertion = $inertionK")
        val maxSpeed = globalConfig.SpeedFactor / sqrt(fragment.mMass)

        mLogger.writeLog("${GameEngine.DEBUG_TAG} maxSpeed = $maxSpeed")
        val vectorTarget = fragment.mVertex.getMovementVector(target).minus(sVector)
        mLogger.writeLog("${GameEngine.DEBUG_TAG} vector target = $vectorTarget")
        if (vectorTarget == MovementVector(0f, 0f)) {
            //no move
            return target
        }

        var NX = ((vectorTarget.SX - sVector.SX) / inertionK + sVector.SX) / maxSpeed
        var NY = ((vectorTarget.SY - sVector.SY) / inertionK + sVector.SY) / maxSpeed

        //FIX: border
        if (fragment.mRadius + 0.2f >= fragment.mVertex.Y && NY < 0
                || fragment.mRadius + 0.2f >= globalConfig.GameHeight - fragment.mVertex.Y && NY > 0)
            NY = 0f
        if (fragment.mRadius + 0.2f >= fragment.mVertex.X && NX < 0
                || fragment.mRadius + 0.2f >= globalConfig.GameWidth - fragment.mVertex.X && NX > 0)
            NX = 0f

        mLogger.writeLog("${GameEngine.DEBUG_TAG} NX=$NX NY=$NY")

        val factor = 1 / sqrt(NX.pow(2) + NY.pow(2)) * fragment.mVertex.distance(target)
        mLogger.writeLog("${GameEngine.DEBUG_TAG} factor $factor")
        mLogger.writeLog("${GameEngine.DEBUG_TAG} Ktarget = ${vectorTarget.K} KN = ${MovementVector(NX, NY).K}")

        NX *= factor //
        NY *= factor

        return GameEngine.vectorEdgeCrossPoint(fragment.mVertex, MovementVector(NX, NY), globalConfig.GameWidth.toFloat(), globalConfig.GameHeight.toFloat())
    }
}

//{\"Mine\":[{\"Id\":\"1\",\"M\":40,\"R\":12.649110640673518,\"SX\":0,\"SY\":0,\"TTF\":32,\"X\":474,\"Y\":178}]