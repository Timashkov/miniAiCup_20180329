package utils

import data.MovementVector
import incominginfos.EnemyInfo
import incominginfos.MineFragmentInfo
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2

// 1/32 of circle
class Compass {

    enum class COLOR {
        BLACK, // prevent move
        GREEN, // prefered for movement
        GRAY, // can go, but with lowest priority
        WHITE // normal sector
    }

    data class Rumb(val majorBorder: Float, var color: COLOR = COLOR.WHITE)

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


    fun getShiftedIndex(index: Int, shifting: Int): Int {
        return if (index >= 16) index - 16
        else index + 16
    }
    
    fun getRumbIndexByVectorNormalized(mv: MovementVector): Int = getShiftedIndex(getRumbIndexByVector(mv), 16)

    fun setColorsByEnemies(me: MineFragmentInfo, enemy: EnemyInfo) {
        setColorsByEnemiesInternal(me.mVertex, me.mMass, enemy.mVertex, enemy.mRadius, enemy.mMass)
    }

    fun setColorsByEnemiesInternal(myPosition: Vertex, myMass: Float, enemyPosition: Vertex, enemyR: Float, enemyMass: Float) {
        val vec = myPosition.getMovementVector(enemyPosition)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)
        val shiftedAngle = asin(enemyR / myPosition.distance(enemyPosition))
        val shiftedRumbIndex = getRumbIndexByAngle(shiftedAngle + directAngle)
        val indexShifting = shiftedRumbIndex - directMovementIndex

        if (enemyMass >= myMass * 1.2f) {
            mRumbBorders[getShiftedIndex(directMovementIndex, 16)].color = COLOR.BLACK
            markRumbsByDirectAndShifting(directMovementIndex, indexShifting, COLOR.BLACK)
        } else if (enemyMass * 1.25f <= myMass && mRumbBorders[directMovementIndex].color != COLOR.BLACK) {
            markRumbsByDirectAndShifting(directMovementIndex, indexShifting, COLOR.GREEN)
        }
    }

    private fun markRumbsByDirectAndShifting(directMovementIndex: Int, indexShifting: Int, black: COLOR) {

    }
}