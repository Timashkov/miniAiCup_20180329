package utils

import data.MovementVector
import incominginfos.EjectionInfo
import incominginfos.EnemyInfo
import incominginfos.FoodInfo
import incominginfos.MineFragmentInfo
import kotlin.math.*

// 1/32 of circle
class Compass {

    data class Rumb(val majorBorder: Float, var availablepoints: Int = 0, var canEat: ArrayList<Vertex> = ArrayList(), var canEatEnemy: Vertex = Vertex(-1f,-1f))

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

    fun setColorsByFood(me: MineFragmentInfo, food: FoodInfo) {
        setColorsByFoodInternal(me.mVertex, food.mVertex)
    }

    fun setColorsByEjection(me: MineFragmentInfo, ejection: EjectionInfo) {
        setColorsByFoodInternal(me.mVertex, ejection.mVertex)
    }

    // can be private , but tests
    fun setColorsByEnemiesInternal(myPosition: Vertex, myMass: Float, enemyPosition: Vertex, enemyR: Float, enemyMass: Float) {
        if (myPosition.distance(enemyPosition) < enemyR) {
            if (enemyMass >= myMass * WorldConfig.EAT_MASS_FACTOR)
                setWholeCompassPoints(FAIL_SECTOR_POINTS)
            return
        }

        val vec = myPosition.getMovementVector(enemyPosition)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)


        if (enemyMass >= myMass * WorldConfig.EAT_MASS_FACTOR) {
            val shiftedAngle = (asin(enemyR / myPosition.distance(enemyPosition)) * 180f / PI).toFloat()
            val shiftedRumbIndex = getRumbIndexByAngle(shiftedAngle + directAngle)
            val indexDelta = shiftedRumbIndex - directMovementIndex
            mRumbBorders[getShiftedIndex(directMovementIndex, 16)].availablepoints = FAIL_SECTOR_POINTS
            markRumbsByDirectAndShifting(directMovementIndex, indexDelta, FAIL_SECTOR_POINTS)
        } else if (enemyMass * WorldConfig.EAT_MASS_FACTOR <= myMass && mRumbBorders[directMovementIndex].availablepoints != FAIL_SECTOR_POINTS) {
            markRumbsByDirectAndShifting(directMovementIndex, 1, PREFERRED_SECTOR)
        }
    }

    fun setColorsByFoodInternal(myPosition: Vertex, foodPosition: Vertex) {

        val vec = myPosition.getMovementVector(foodPosition)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)

        if (mRumbBorders[directMovementIndex].availablepoints != FAIL_SECTOR_POINTS) {
            mRumbBorders[directMovementIndex].availablepoints = mRumbBorders[directMovementIndex].availablepoints + 1
            mRumbBorders[directMovementIndex].canEat.add(foodPosition)
        }
    }

    private fun setWholeCompassPoints(points: Int) {
        mRumbBorders.forEach { it.availablepoints = points }
    }

    private fun markRumbsByDirectAndShifting(directMovementIndex: Int, indexDelta: Int, points: Int) {
        for (i in indexDelta * -1..indexDelta) {
            if (points == FAIL_SECTOR_POINTS) {
                mRumbBorders[getShiftedIndex(directMovementIndex, i)].availablepoints = points
                if (abs(i) == indexDelta)
                    mRumbBorders[getShiftedIndex(directMovementIndex, i + 16)].availablepoints = PREFERRED_SECTOR
            } else if (mRumbBorders[getShiftedIndex(directMovementIndex, i)].availablepoints != FAIL_SECTOR_POINTS) {
                mRumbBorders[getShiftedIndex(directMovementIndex, i)].availablepoints = points
            }
        }
    }

    fun isVertexInBlackArea(myPosition: Vertex, target: Vertex): Boolean {
        val vec = myPosition.getMovementVector(target)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)
        return mRumbBorders[directMovementIndex].availablepoints == FAIL_SECTOR_POINTS
    }

    companion object {
        val FAIL_SECTOR_POINTS = -100 // can be eaten here
        val PREFERRED_SECTOR = 5 //
    }

    fun hasBlackAreas(): Boolean {
        return mRumbBorders.any { it.availablepoints == FAIL_SECTOR_POINTS }
    }
}