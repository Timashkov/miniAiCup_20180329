package utils

import WorldConfig.Companion.FOW_RADIUS_FACTOR
import data.MovementVector
import incominginfos.*
import kotlin.math.*
import WorldConfig
import data.FoodPoint

// 1/32 of circle
class Compass(private val mFragment: MineFragmentInfo, private val mGlobalConfig: WorldConfig, private val plain: Boolean = false) {

    data class Rumb(val majorBorder: Float, var areaScore: Int = DEFAULT_AREA_SCORE, var canEat: ArrayList<FoodPoint> = ArrayList(), var canEatEnemy: ArrayList<Vertex> = ArrayList(), var canBeEatenByEnemy: ArrayList<Vertex> = ArrayList())

    val mRumbBorders: Array<Rumb>
    val mCenterVertex = mFragment.mVertex

    init {
        val rs = ArrayList<Rumb>()
        for (i in -15..16) {
            rs.add(Rumb(11.25f * i, if (plain) 0 else DEFAULT_AREA_SCORE))
        }
        mRumbBorders = rs.toTypedArray()


    }

    fun getRumbIndexByVector(mv: MovementVector): Int {
        val angle = atan2(mv.SY, mv.SX) * 180f / PI
        return getRumbIndexByAngle(angle.toFloat())
    }

    private fun getRumbIndexByAngle(angle: Float): Int {
        var target = angle
        while (angle > 180f) {
            target -= 360f
        }
        while (angle < -180f) {
            target += 360f
        }
        return mRumbBorders.indexOfFirst { angle <= it.majorBorder }
    }

    fun getSectorScoreByVector(mv: MovementVector): Int = mRumbBorders[getRumbIndexByVector(mv)].areaScore

    fun getShiftedIndex(index: Int, shifting: Int): Int {
        val res = index + shifting
        if (res > 0 && res >= mRumbBorders.size)
            return res % mRumbBorders.size

        if (res < 0)
            return mRumbBorders.size + res % mRumbBorders.size
        return res
    }

    fun setColorsByEnemies(me: MineFragmentInfo, enemy: EnemyInfo) {
        val distance = me.mVertex.distance(enemy.mVertex)
        if (distance < enemy.mRadius) {
            if (enemy.mMass >= me.mMass * WorldConfig.EAT_MASS_FACTOR)
                setWholeCompassPoints(BLACK_SECTOR_SCORE, enemy.mVertex.getMovementVector(me.mVertex))
            return
        }

        if (distance > 16 * me.mRadius) {
            // too far
            return
        }

        val vec = me.mVertex.getMovementVector(enemy.mVertex)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)

        if (enemy.mMass >= me.mMass * WorldConfig.EAT_MASS_FACTOR) {

            val shiftedAngle = (asin(enemy.mRadius / me.mVertex.distance(enemy.mVertex)) * 180f / PI).toFloat()

            var searchingAngle = shiftedAngle + directAngle
            var aign = 1
            if (searchingAngle > 180f) {
                aign = -1
                searchingAngle = directAngle - shiftedAngle
            }

            val shiftedRumbIndex = getRumbIndexByAngle(searchingAngle)
            val indexDelta = aign * (shiftedRumbIndex - directMovementIndex)

            markRumbsByDirectAndShifting(directMovementIndex, indexDelta, BLACK_SECTOR_SCORE)
        } else if (enemy.mMass * WorldConfig.EAT_MASS_FACTOR <= me.mMass) {
            markRumbsByDirectAndShifting(directMovementIndex, 1, PREFERRED_SECTOR_SCORE)
        }
    }

    fun setColorsByFood(food: FoodInfo): Boolean {
        return setColorsByFoodInternal(mCenterVertex, food.mVertex)
    }

    fun setColorsByEjection(ejection: EjectionInfo): Boolean {
        return setColorsByFoodInternal(mCenterVertex, ejection.mVertex)
    }

    fun mergeCompass(compass: Compass, factor: Float) {
        compass.mRumbBorders.forEachIndexed { index, it ->
            mRumbBorders[index].areaScore += (it.areaScore * factor).toInt()
            it.canEatEnemy.forEach { enemy ->
                if (!mRumbBorders[index].canEatEnemy.contains(enemy))
                    mRumbBorders[index].canEatEnemy.add(enemy)
            }
            it.canEat.forEach { food ->
                if (!mRumbBorders[index].canEat.contains(food))
                    mRumbBorders[index].canEat.add(food)
            }
        }
    }

    private fun setWholeCompassPoints(points: Int, escapeVector: MovementVector?) {
        mRumbBorders.forEach { it.areaScore = points }
        escapeVector?.let { vec->
            val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
            val directMovementIndex = getRumbIndexByAngle(directAngle)
            mRumbBorders[directMovementIndex].areaScore = ESCAPE_SECTOR_SCORE
        }
    }

    private fun markRumbsByDirectAndShifting(directMovementIndex: Int, indexDelta: Int, points: Int) {
        for (i in indexDelta * -1..indexDelta) {
            if (points == BLACK_SECTOR_SCORE) {
                mRumbBorders[getShiftedIndex(directMovementIndex, i)].areaScore = points
            } else if (mRumbBorders[getShiftedIndex(directMovementIndex, i)].areaScore != BLACK_SECTOR_SCORE) {
                mRumbBorders[getShiftedIndex(directMovementIndex, i)].areaScore = points
            }
        }
    }

    fun isVertexInBlackArea(target: Vertex): Boolean {
        val vec = mCenterVertex.getMovementVector(target)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)
        return mRumbBorders[directMovementIndex].areaScore == BLACK_SECTOR_SCORE
    }

    fun isVertexInBurstArea(target: Vertex): Boolean {
        val vec = mCenterVertex.getMovementVector(target)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)
        return mRumbBorders[directMovementIndex].areaScore == BURST_SECTOR_SCORE
    }

    fun isVertexInDangerArea(target: Vertex): Boolean {
        val vec = mCenterVertex.getMovementVector(target)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)
        return mRumbBorders[directMovementIndex].areaScore in arrayOf(BURST_SECTOR_SCORE, BLACK_SECTOR_SCORE)
    }

    fun getAreaScore(target: Vertex): Int {
        val vec = mCenterVertex.getMovementVector(target)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)
        return mRumbBorders[directMovementIndex].areaScore
    }

    fun setColorsByVirus(virus: VirusInfo): Boolean {
        if (mCenterVertex.distance(virus.mVertex) - virus.mRadius > mFragment.mRadius * FOW_RADIUS_FACTOR) {
            return false
        }
        val vec = mCenterVertex.getMovementVector(virus.mVertex)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)

        if (mRumbBorders[directMovementIndex].areaScore != BLACK_SECTOR_SCORE) {
            mRumbBorders[directMovementIndex].areaScore = BURST_SECTOR_SCORE

            return true
        }

        val shiftedAngle = (asin(virus.mRadius / mCenterVertex.distance(virus.mVertex)) * 180f / PI).toFloat()
        val shiftedRumbIndex = getRumbIndexByAngle(shiftedAngle + directAngle)
        val indexDelta = shiftedRumbIndex - directMovementIndex
        markRumbsByDirectAndShifting(directMovementIndex, indexDelta, BURST_SECTOR_SCORE)
        return false
    }

    private fun setColorsByFoodInternal(myPosition: Vertex, foodPosition: Vertex): Boolean {

        val vec = myPosition.getMovementVector(foodPosition)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)

        if (mRumbBorders[directMovementIndex].areaScore != BLACK_SECTOR_SCORE) {
//            mRumbBorders[directMovementIndex].canEat.add(FoodPoint(foodPosition, arrayListOf(foodPosition), mFragment.mId))
//            mRumbBorders[directMovementIndex].areaScore++
            return true
        }
        return false
    }

    fun getWhiteSectorsIndexesArray(): List<Rumb> {
        return mRumbBorders.filter { it.areaScore >= BURST_SECTOR_SCORE }
    }

    fun hasBlackAreas(): Boolean {
        return mRumbBorders.any { it.areaScore == BLACK_SECTOR_SCORE }
    }

    fun hasDarkAreas(): Boolean {
        return mRumbBorders.any { it.areaScore < 1 }
    }

    fun getMapEdgeBySector(rumbBorder: Float): Vertex {
        // val vec = myPosition.getMovementVector(enemyPosition)
        //val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()

        val K = tan(rumbBorder * PI / 180f).toFloat()
        val b = mCenterVertex.Y - mCenterVertex.X * K

        if (rumbBorder == 90f) {
            return Vertex(mCenterVertex.X, mGlobalConfig.GameHeight.toFloat())
        }
        if (rumbBorder == -90f) {
            return Vertex(mCenterVertex.X, 0f)
        }
        if (rumbBorder < -90 || rumbBorder > 90) {
            return GameEngine.fixByBorders(mCenterVertex, Vertex(0f, b), mGlobalConfig.GameWidth.toFloat(), mGlobalConfig.GameHeight.toFloat())
        } else {
            return GameEngine.fixByBorders(mCenterVertex, Vertex(mGlobalConfig.GameWidth.toFloat(), mGlobalConfig.GameWidth.toFloat() * K + b), mGlobalConfig.GameWidth.toFloat(), mGlobalConfig.GameHeight.toFloat())
        }
    }

    fun setColorByEdge(edgeVertex: Vertex) {
        val vec = mCenterVertex.getMovementVector(edgeVertex)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)
        val shiftingDelta = 4

        markRumbsByDirectAndShifting(directMovementIndex, shiftingDelta, EDGE_SECTOR_SCORE)
    }

    fun reconfigure(foodPoints: ArrayList<Vertex>) {
        setFoodToSectors(foodPoints)
        updateScore()
    }

    private fun setFoodToSectors(foodPoints: ArrayList<Vertex>) {
//        mRumbBorders.forEach { it.canEat.clear() }
        val filtered = foodPoints.filter { !isVertexInDangerArea(it) }

        val sortedByX = filtered.sortedBy { it.X }
        val sortedByR = filtered.sortedBy { mCenterVertex.distance(it) }


        sortedByR.forEach { it ->
            val verts: ArrayList<Vertex> = ArrayList()
            verts.add(it)
            val xIndex = sortedByX.indexOf(it)

            if (xIndex > 0) {
                for (i in xIndex - 1 downTo 0) {
                    if (abs(sortedByX[i].X - it.X) < mFragment.mRadius) {
                        if (it.distance(sortedByX[i]) < mFragment.mRadius * 0.95f)
                            verts.add(sortedByX[i])
                    } else
                        break
                }
            }
            if (xIndex < filtered.size - 1) {
                for (i in xIndex + 1 until filtered.size)
                    if (abs(sortedByX[i].X - it.X) < mFragment.mRadius) {
                        if (it.distance(sortedByX[i]) < mFragment.mRadius * 0.95f)
                            verts.add(sortedByX[i])
                    } else
                        break
            }

            val vec = mCenterVertex.getMovementVector(it)
            val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
            val directMovementIndex = getRumbIndexByAngle(directAngle)
            mRumbBorders[directMovementIndex].canEat.add(FoodPoint(it, verts, mFragment.mId))
            mRumbBorders[directMovementIndex].areaScore += verts.size
        }
    }


    private fun updateScore() {
        var first = -16
        var count = 0
        val sectorsSet: HashMap<Int, Int> = HashMap()

        mRumbBorders.forEach { sector ->

            if (sector.areaScore > -1) {
                if (first > -1) {
                    count += 1
                } else {
                    first = mRumbBorders.indexOf(sector)
                    count += 1
                }
            } else {
                if (first > -1) {
                    sectorsSet[first] = count
                    first = -1
                    count = 0
                }
            }
        }

        if (first > -1) {
            // last sector still +
            if (sectorsSet.containsKey(0)) {
                sectorsSet[first] = sectorsSet[0]!! + count
                sectorsSet.remove(0)
            } else
                sectorsSet[first] = count
        }
        if (sectorsSet.any { it -> it.value == 32 }) {
            return
        }

        sectorsSet.forEach { startIndex, count ->
            val factor = count / 2

            val directMovementIndex = getShiftedIndex(startIndex, factor)
            if (count % 2 == 1) {
                mRumbBorders[directMovementIndex].areaScore *= 2f.pow(factor).toInt()
            }

            for (i in 1..factor) {
                mRumbBorders[getShiftedIndex(directMovementIndex, i - 1)].areaScore *= 2f.pow(factor - i).toInt()
                mRumbBorders[getShiftedIndex(directMovementIndex, -i)].areaScore *= 2f.pow(factor - i).toInt()
            }
        }
        // best koeff = 2^(sectorscount/2)
    }

    fun getSectorFoodPoint(sector: Rumb): FoodPoint {
        val canEat = sector.canEat
        if (canEat.isNotEmpty())
            return canEat.maxBy { it.target.distance(mCenterVertex) }!!

        val target = getVertexBySector(sector.majorBorder)
        return FoodPoint(target, listOf(target), mFragment.mId, sector.areaScore >= ESCAPE_SECTOR_SCORE ) // there is no food
    }

    fun getVertexBySector(rumbBorder: Float): Vertex {
        // val vec = myPosition.getMovementVector(enemyPosition)
        //val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()

        val K = tan(rumbBorder * PI / 180f).toFloat()
        val b = mCenterVertex.Y - mCenterVertex.X * K

        if (rumbBorder == 90f) {
            return Vertex(mCenterVertex.X, mCenterVertex.Y + mFragment.mRadius * 4)
        }
        if (rumbBorder == -90f) {
            return Vertex(mCenterVertex.X, mCenterVertex.Y - mFragment.mRadius * 4)
        }
        if (rumbBorder < -90 || rumbBorder > 90) {
            return Vertex(mCenterVertex.X - mFragment.mRadius * 4, (mCenterVertex.X - mFragment.mRadius * 4) * K + b)
        } else {
            return Vertex(mCenterVertex.X + mFragment.mRadius * 4, (mCenterVertex.X + mFragment.mRadius * 4) * K + b)
        }
    }

    fun getMaxSectorScore(): Int {
        var score = 0
        mRumbBorders.forEach { if (it.areaScore > score) score = it.areaScore }
        return score
    }

    fun getDangerSectorsCount(): Int = mRumbBorders.filter { it.areaScore <= BURST_SECTOR_SCORE }.size

    //TODO: set area factor by enemies with calc of distance

    companion object {
        val BLACK_SECTOR_SCORE = -100 // can be eaten here
        val BURST_SECTOR_SCORE = -50
        val PREFERRED_SECTOR_SCORE = 5 //
        val EDGE_SECTOR_SCORE = -10 //
        val DEFAULT_AREA_SCORE = 1
        val ESCAPE_SECTOR_SCORE = Int.MAX_VALUE
    }

}