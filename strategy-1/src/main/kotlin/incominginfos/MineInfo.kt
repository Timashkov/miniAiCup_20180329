package incominginfos

import org.json.JSONArray
import utils.Vertex
import WorldConfig
import data.MovementVector
import data.StepPoint
import utils.Compass
import utils.GameEngine
import utils.Logger
import kotlin.math.*

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

        var existStepPoint: StepPoint? = null
        mLogger.writeLog("Looking for best sector. Known target = $knownVertex")
        knownVertex?.let { fp ->
            val knownFragment = mFragmentsState.firstOrNull { it.mId == fp.fragmentId }
            knownFragment?.let { targetFragment ->

                mLogger.writeLog("Known vertex is not null")

                if (mFragmentsState.any { it.mVertex.X <= it.mRadius + 1f || it.mVertex.Y <= it.mRadius + 1f || it.mVertex.X >= globalConfig.GameWidth - it.mRadius - 1f || it.mVertex.Y >= globalConfig.GameHeight - it.mRadius - 1f }) {
                    return@let
                }

                if (fp.foodPoints.size == 1 && globalConfig.outOfMap(fp.foodPoints[0])) {
                    mLogger.writeLog("food point out of map")
                    return@let
                }

                if (mFragmentsState.size == 1) {
                    val score = getMainFragment().mCompass.getAreaScore(fp.target)
                    if (getMainFragment().mCompass.mRumbBorders.any { rumb -> rumb.areaScore / 2f > score })
                        return@let
                }

                if (mFragmentsState.none { it.mCompass.isVertexInBlackArea(fp.target) } && mFragmentsState.none { it.mVertex == fp.target }) {
                    if (mFragmentsState.size > 1 && mFragmentsState.all { it.mTTF < 2 } && mFragmentsState.any { it.mCompass.hasBlackAreas() }) {
                        existStepPoint = knownVertex
                        return@let
                    }


                    knownVertex.movementTarget = getMovementPointForTarget(targetFragment, knownVertex.target)
                    existStepPoint = knownVertex
                }
            }
        }

        // 1st stage - one fragment
        return if (mFragmentsState.size == 1) {
            movementPointForOneFragment(existStepPoint)
        } else {
            movementPointForMultiFragment(existStepPoint)
        }
    }

    private fun movementPointForMultiFragment(existStepPoint: StepPoint?): StepPoint {

        var xmax = -1f
        var xmin = 1000f
        var ymax = -1f
        var ymin = 1000f
        var rsum = 0f
        mFragmentsState.forEach { frag ->
            if (frag.mVertex.X > xmax)
                xmax = frag.mVertex.X
            if (frag.mVertex.X < xmin)
                xmin = frag.mVertex.X
            if (frag.mVertex.Y > ymax)
                ymax = frag.mVertex.Y
            if (frag.mVertex.Y < ymin)
                ymin = frag.mVertex.Y
            rsum += frag.mRadius
        }
        val tooBigRadius = Vertex(xmin, ymin).distance(Vertex(xmax, ymax)) > rsum * 4
        val mergeRequired = mFragmentsState.all { it.mTTF < 2 } && mFragmentsState.any { it.mCompass.hasBlackAreas() }

        if (tooBigRadius || mergeRequired) {

            var x = 0.0f
            var y = 0.0f
            mFragmentsState.forEach {
                x += it.mVertex.X
                y += it.mVertex.Y
            }
            x /= mFragmentsState.size
            y /= mFragmentsState.size
            val center = Vertex(x, y)
            if (mFragmentsState.none { it.mCompass.isVertexInDangerArea(center) }) {
                return StepPoint(center, center, listOf(center), getMainFragment().mId, 100f)
            }
        }

        val borderVerts: ArrayList<Vertex> = ArrayList()
        for (i in 0..98) {
            if (mFragmentsState.none { it.mVertex.Y < it.mRadius * 2 })
                borderVerts.add(Vertex((i * 10 + 5).toFloat(), 0f))
            if (mFragmentsState.none { it.mVertex.X < it.mRadius * 2 })
                borderVerts.add(Vertex(0f, (i * 10 + 5).toFloat()))
            if (mFragmentsState.none { it.mVertex.Y > globalConfig.GameHeight.toFloat() - it.mRadius * 2 })
                borderVerts.add(Vertex((i * 10 + 5).toFloat(), globalConfig.GameHeight.toFloat()))
            if (mFragmentsState.none { it.mVertex.X > globalConfig.GameWidth.toFloat() - it.mRadius * 2 })
                borderVerts.add(Vertex(globalConfig.GameWidth.toFloat(), (i * 10 + 5).toFloat()))
        }

        var maxScore = 0
        var target = Vertex.DEFAULT

        val borderVertexes: HashMap<Vertex, Int> = HashMap()

        borderVerts.forEach { vertex ->
            var score = 0
            mFragmentsState.forEach { frag ->
                val areaScoreFactor = if (mFragmentsState.indexOf(frag) == mMainFragmentIndex) 2 else 1
                score += frag.mCompass.getAreaScore(vertex) * areaScoreFactor
            }

            if (existStepPoint != null && existStepPoint!!.target == vertex)
                existStepPoint!!.mScore = score.toFloat()

            if (score > maxScore) {
                maxScore = score
                borderVertexes.clear()
                borderVertexes[vertex] = maxScore
            } else if (score == maxScore) {
                borderVertexes[vertex] = maxScore
            }
        }


        if (maxScore > (mFragmentsState.size + 1) * Compass.DEFAULT_AREA_SCORE && existStepPoint == null) {
            return StepPoint.DEFAULT
        }

        if (borderVertexes.size > 1) {
            var minAngle = 360f
            var indexVertes = Vertex.DEFAULT
            val mv = getMainFragment().flippedVectorByEdge(GameEngine.vectorEdgeCrossPoint(getMainFragment().mVertex, MovementVector(getMainFragment().mSX, getMainFragment().mSY), globalConfig.GameWidth.toFloat(), globalConfig.GameHeight.toFloat()))
            val currentAngle = (atan2(mv.SY, mv.SX) * 180f / PI).toFloat()
            borderVertexes.forEach {
                val vectToPoint = getMainFragment().mVertex.getMovementVector(it.key)
                val vectorAngle = (atan2(vectToPoint.SY, vectToPoint.SX) * 180f / PI).toFloat()
                if (abs(vectorAngle - currentAngle) < minAngle) {
                    minAngle = abs(vectorAngle - currentAngle)
                    indexVertes = it.key
                }
            }
            borderVertexes[indexVertes] = borderVertexes[indexVertes]!! + 10
            target = indexVertes
        } else {
            target = borderVertexes.keys.first()
        }

        if (target != Vertex.DEFAULT) {

            val canEatVertexes: ArrayList<StepPoint> = ArrayList()
            mFragmentsState.forEach { frag -> canEatVertexes.addAll(frag.mCompass.mRumbBorders[frag.mCompass.getRumbIndexByVector(frag.mVertex.getMovementVector(target))].canEat) }
            val foods: ArrayList<Vertex> = ArrayList()
            foods.addAll(canEatVertexes.flatMap { it -> it.foodPoints })
            foods.add(target)

            val fp = if (existStepPoint != null && borderVertexes[target]!!.toFloat() * 0.75f < existStepPoint!!.mScore) existStepPoint!! else StepPoint(target, target, foods, getMainFragment().mId, borderVertexes[target]!!.toFloat())
            if (mFragmentsState.none { state -> state.mCompass.isVertexInDangerArea(target) }) {

                if (mFragmentsState.none { state -> state.mCompass.isVertexInAreaWithEnemy(fp.movementTarget) }) {
                    fp.useSplit = getMainFragment().maySplit && mFragmentsState.none { it.mCompass.hasBlackAreas() } && mFragmentsState.none { frag -> frag.mCompass.mRumbBorders.any { rumb -> rumb.enemies.any { en -> en.mVertex.distance(frag.mVertex) < frag.mRadius * WorldConfig.FOW_RADIUS_FACTOR / 2 } } } && !getMainFragment().mCompass.isVertexInAreaWithEnemy(getMainFragment().mVertex.plus(Vertex(getMainFragment().mSX, getMainFragment().mSY)))
                }
                mLogger.writeLog("Going to border vertex : $fp")
                return fp
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

                    mLogger.writeLog("BS21: $fp")
                    fp.movementTarget = getMovementPointForTarget(fr, fp.target)

                    if (mFragmentsState.all { it.mTTF < 2 } && mFragmentsState.any { it.mCompass.hasBlackAreas() }) {

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
                mLogger.writeLog("BS22: $fp")
                fp.movementTarget = getMovementPointForTarget(fr, fp.target)
                if (fr.canSplit && fr.mCompass.getRumbIndexByVector(MovementVector(fr.mSX, fr.mSY)) == fr.mCompass.mRumbBorders.indexOf(escape))
                    fp.useSplit = true
                else
                    fp.useEjections = true
            }
        }
        return StepPoint.DEFAULT
    }

    private fun movementPointForOneFragment(existStepPoint: StepPoint?): StepPoint {
        val fr = mFragmentsState[0]
        var areas = fr.mCompass.mRumbBorders.filter { rumb ->
            rumb.areaScore > Compass.DEFAULT_AREA_SCORE ||
                    rumb.lastEscapePoint ||
                    (rumb.areaScore == Compass.DEFAULT_AREA_SCORE && fr.mCompass.hasBlackAreas())
        }.sortedByDescending { it.areaScore }
        if (areas.isEmpty())
            return StepPoint.DEFAULT

        val maxscore = areas[0].areaScore
        areas = areas.filter { it.areaScore == maxscore }

        if (areas.size > 1) {
            var minAngle = 360f
            var index = -1
            val mv = getMainFragment().flippedVectorByEdge(GameEngine.vectorEdgeCrossPoint(getMainFragment().mVertex, MovementVector(getMainFragment().mSX, getMainFragment().mSY), globalConfig.GameWidth.toFloat(), globalConfig.GameHeight.toFloat()))
            val currentAngle = (atan2(mv.SY, mv.SX) * 180f / PI).toFloat()
            areas.forEach { it ->

                val vectorAngle = it.majorBorder
                if (abs(vectorAngle - currentAngle) < minAngle) {
                    minAngle = abs(vectorAngle - currentAngle)
                    index = areas.indexOf(it)
                }
            }
            areas[index].areaScore = maxscore + 10
            areas = areas.sortedByDescending { it.areaScore }
        }


        areas.forEach { rumb ->
            var fp = fr.mCompass.getSectorFoodPoint(rumb)

            if (existStepPoint != null && fp.mScore * 0.9f < existStepPoint.mScore)
                fp = existStepPoint

            if (fp != StepPoint.DEFAULT) {

                mLogger.writeLog("BS11: $fp")
                fp.movementTarget = getMovementPointForTarget(fr, fp.target)

                if (rumb.lastEscapePoint) {
                    if (rumb.lastEscapePoint) {
                        if (fr.canSplit && fr.mCompass.getRumbIndexByVector(MovementVector(fr.mSX, fr.mSY)) == fr.mCompass.mRumbBorders.indexOf(rumb))
                            fp.useSplit = true
                        else
                            fp.useEjections = true
                    }
                    return fp
                }

                if (!fr.mCompass.isVertexInDangerArea(fp.target)) {

                    if (!fr.mCompass.isVertexInAreaWithEnemy(fp.movementTarget)) {
                        fp.useSplit = fr.maySplit && !fr.mCompass.hasBlackAreas() && !fr.mCompass.mRumbBorders.any { rumb -> rumb.enemies.any { en -> en.mVertex.distance(fr.mVertex) < fr.mRadius * WorldConfig.FOW_RADIUS_FACTOR / 2 } } && !fr.mCompass.isVertexInAreaWithEnemy(getMainFragment().mVertex.plus(Vertex(getMainFragment().mSX, getMainFragment().mSY)))
                    }
                    return fp
                }
            }
        }

        if (fr.mCompass.hasBlackAreas() && fr.mCompass.mRumbBorders.none { it.areaScore > 0 }) {
            val escape = fr.mCompass.mRumbBorders.filter { rumb ->
                rumb.areaScore >= Compass.CORNER_SECTOR_SCORE
            }[0]
            val fp = fr.mCompass.getSectorFoodPoint(escape)
            mLogger.writeLog("BS12: $fp")
            fp.movementTarget = getMovementPointForTarget(fr, fp.target)
            if (fr.canSplit && fr.mCompass.getRumbIndexByVector(MovementVector(fr.mSX, fr.mSY)) == fr.mCompass.mRumbBorders.indexOf(escape))
                fp.useSplit = true
            else
                fp.useEjections = true
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
        val distance = fragment.mVertex.distance(target)

        val vectorTarget = fragment.mVertex.getMovementVector(target, 8f / distance).minus(sVector)
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
