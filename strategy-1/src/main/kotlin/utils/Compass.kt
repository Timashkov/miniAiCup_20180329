package utils

import data.MovementVector
import incominginfos.EnemyInfo
import incominginfos.MineFragmentInfo
import kotlin.math.*

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
        val res = index + shifting
        if (res > 0 && res >= mRumbBorders.size)
            return res%mRumbBorders.size

        if (res < 0 )
            return mRumbBorders.size + res%mRumbBorders.size
        return res
    }

    fun getRumbIndexByVectorNormalized(mv: MovementVector): Int = getShiftedIndex(getRumbIndexByVector(mv), 16)

    fun setColorsByEnemies(me: MineFragmentInfo, enemy: EnemyInfo) {
        setColorsByEnemiesInternal(me.mVertex, me.mMass, enemy.mVertex, enemy.mRadius, enemy.mMass)
    }

    fun setColorsByEnemiesInternal(myPosition: Vertex, myMass: Float, enemyPosition: Vertex, enemyR: Float, enemyMass: Float) {
        if (myPosition.distance(enemyPosition) < enemyR) {
            if (enemyMass >= myMass * 1.2f)
                markWholeCompass(COLOR.BLACK)
            return
        }

        val vec = myPosition.getMovementVector(enemyPosition)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)


        if (enemyMass >= myMass * 1.2f) {
            val shiftedAngle = (asin(enemyR / myPosition.distance(enemyPosition)) * 180f / PI).toFloat()
            val shiftedRumbIndex = getRumbIndexByAngle(shiftedAngle + directAngle)
            val indexDelta = shiftedRumbIndex - directMovementIndex
            mRumbBorders[getShiftedIndex(directMovementIndex, 16)].color = COLOR.BLACK
            markRumbsByDirectAndShifting(directMovementIndex, indexDelta, COLOR.BLACK)
        } else if (enemyMass * 1.25f <= myMass && mRumbBorders[directMovementIndex].color != COLOR.BLACK) {
            markRumbsByDirectAndShifting(directMovementIndex, 1, COLOR.GREEN)
        }

    }

    private fun markWholeCompass(color: COLOR){
        mRumbBorders.forEach { it.color = color }
    }

    private fun markRumbsByDirectAndShifting(directMovementIndex: Int, indexDelta: Int, color: COLOR) {
        for (i in indexDelta * -1..indexDelta) {
            if (color == COLOR.BLACK) {
                mRumbBorders[getShiftedIndex(directMovementIndex, i)].color = color
                if (abs(i) != indexDelta)
                    mRumbBorders[getShiftedIndex(directMovementIndex, i + 16)].color = color
                else
                    mRumbBorders[getShiftedIndex(directMovementIndex, i + 16)].color = COLOR.GREEN
            } else if (mRumbBorders[getShiftedIndex(directMovementIndex, i)].color != COLOR.BLACK) {
                mRumbBorders[getShiftedIndex(directMovementIndex, i)].color = color
            }
        }
    }
}