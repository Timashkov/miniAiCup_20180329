package utils

import WorldConfig.Companion.MAGIC_COMPASS_BLACK_DELTA
import data.MovementVector
import incominginfos.*
import kotlin.math.*

// 1/32 of circle
class Compass(val mCenterVertex: Vertex) {

    data class Rumb(val majorBorder: Float, var availablepoints: Int = 0, var canEat: ArrayList<Vertex> = ArrayList(), var canEatEnemy: Vertex = Vertex(-1f, -1f))

    val mRumbBorders: Array<Rumb>

    init {
        val rs = ArrayList<Rumb>()
        for (i in -15..16) {
            rs.add(Rumb(11.25f * i))
        }
        mRumbBorders = rs.toTypedArray()
    }

    fun getRumbIndexByVector(mv: MovementVector): Int {
        val angle = atan2(mv.SY, mv.SX) * 180f / PI
        return getRumbIndexByAngle(angle.toFloat())
    }

    private fun getRumbIndexByAngle(angle: Float): Int {
        return mRumbBorders.indexOfFirst { angle < it.majorBorder }
    }

    fun getRumbPointsByVector(mv: MovementVector): Int = mRumbBorders[getRumbIndexByVector(mv)].availablepoints

    fun getShiftedIndex(index: Int, shifting: Int): Int {
        val res = index + shifting
        if (res > 0 && res >= mRumbBorders.size)
            return res % mRumbBorders.size

        if (res < 0)
            return mRumbBorders.size + res % mRumbBorders.size
        return res
    }

    fun getRumbIndexByVectorNormalized(mv: MovementVector): Int = getShiftedIndex(getRumbIndexByVector(mv), 16)

    fun setColorsByEnemies(me: MineFragmentInfo, enemy: EnemyInfo) {
        setColorsByEnemiesInternal(me.mVertex, me.mMass, enemy.mVertex, enemy.mRadius, enemy.mMass)
    }

    fun setColorsByFood(food: FoodInfo): Boolean {
        return setColorsByFoodInternal(mCenterVertex, food.mVertex)
    }

    fun setColorsByEjection(ejection: EjectionInfo): Boolean {
        return setColorsByFoodInternal(mCenterVertex, ejection.mVertex)
    }

    // can be private , but tests
    fun setColorsByEnemiesInternal(myPosition: Vertex, myMass: Float, enemyPosition: Vertex, enemyR: Float, enemyMass: Float) {
        if (myPosition.distance(enemyPosition) < enemyR) {
            if (enemyMass >= myMass * WorldConfig.EAT_MASS_FACTOR)
                setWholeCompassPoints(BLACK_SECTOR_POINTS)
            return
        }

        val vec = myPosition.getMovementVector(enemyPosition)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)


        if (enemyMass >= myMass * WorldConfig.EAT_MASS_FACTOR) {
            val shiftedAngle = (asin(enemyR / myPosition.distance(enemyPosition)) * 180f / PI).toFloat()
            val shiftedRumbIndex = getRumbIndexByAngle(shiftedAngle + directAngle)
            val indexDelta = shiftedRumbIndex - directMovementIndex + MAGIC_COMPASS_BLACK_DELTA

            mRumbBorders[getShiftedIndex(directMovementIndex, 16)].availablepoints = BLACK_SECTOR_POINTS
            markRumbsByDirectAndShifting(directMovementIndex, indexDelta, BLACK_SECTOR_POINTS)
        } else if (enemyMass * WorldConfig.EAT_MASS_FACTOR <= myMass && mRumbBorders[directMovementIndex].availablepoints != BLACK_SECTOR_POINTS) {
            markRumbsByDirectAndShifting(directMovementIndex, 1, PREFERRED_SECTOR)
        }
    }

    fun setColorsByFoodInternal(myPosition: Vertex, foodPosition: Vertex): Boolean {

        val vec = myPosition.getMovementVector(foodPosition)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)

        if (mRumbBorders[directMovementIndex].availablepoints != BLACK_SECTOR_POINTS) {
            mRumbBorders[directMovementIndex].availablepoints = mRumbBorders[directMovementIndex].availablepoints + 1
            mRumbBorders[directMovementIndex].canEat.add(foodPosition)
            return true
        }
        return false
    }

    private fun setWholeCompassPoints(points: Int) {
        mRumbBorders.forEach { it.availablepoints = points }
    }

    private fun markRumbsByDirectAndShifting(directMovementIndex: Int, indexDelta: Int, points: Int) {
        for (i in indexDelta * -1..indexDelta) {
            if (points == BLACK_SECTOR_POINTS) {
                mRumbBorders[getShiftedIndex(directMovementIndex, i)].availablepoints = points
                if (abs(i) == indexDelta)
                    mRumbBorders[getShiftedIndex(directMovementIndex, i + 16)].availablepoints = PREFERRED_SECTOR
            } else if (mRumbBorders[getShiftedIndex(directMovementIndex, i)].availablepoints != BLACK_SECTOR_POINTS) {
                mRumbBorders[getShiftedIndex(directMovementIndex, i)].availablepoints = points
            }
        }
    }

    fun isVertexInBlackArea(target: Vertex): Boolean {
        val vec = mCenterVertex.getMovementVector(target)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)
        return mRumbBorders[directMovementIndex].availablepoints == BLACK_SECTOR_POINTS
    }

    fun isVertexInBurstArea(target: Vertex): Boolean {
        val vec = mCenterVertex.getMovementVector(target)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)
        return mRumbBorders[directMovementIndex].availablepoints == BURST_SECTOR_POINTS
    }

    fun isVertexInDangerArea(target: Vertex): Boolean {
        val vec = mCenterVertex.getMovementVector(target)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)
        return mRumbBorders[directMovementIndex].availablepoints in arrayOf(BURST_SECTOR_POINTS, BLACK_SECTOR_POINTS)
    }


    fun hasBlackAreas(): Boolean {
        return mRumbBorders.any { it.availablepoints == BLACK_SECTOR_POINTS }
    }

    fun setColorsByVirus(virus: VirusInfo): Boolean {
        val vec = mCenterVertex.getMovementVector(virus.mVertex)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)

        if (mRumbBorders[directMovementIndex].availablepoints != BLACK_SECTOR_POINTS) {
            mRumbBorders[directMovementIndex].availablepoints = BURST_SECTOR_POINTS

            return true
        }
        return false
    }

    companion object {
        val BLACK_SECTOR_POINTS = -100 // can be eaten here
        val BURST_SECTOR_POINTS = -50
        val PREFERRED_SECTOR = 5 //
    }


}