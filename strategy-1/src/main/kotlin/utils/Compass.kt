package utils

import data.MovementVector
import incominginfos.EnemyInfo
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2


// 1/32 of circle
class Compass {

    data class Rumb(val majorBorder: Float,var color: Int)

    val mRumbBorders: Array<Rumb>

    init{
        val rs = ArrayList<Rumb>()
        for (i in -15 .. 16){
            rs.add(Rumb(11.25f*i, 0))
        }
        mRumbBorders = rs.toTypedArray()
    }

    fun getRumbIndexByVector(mv: MovementVector): Int {
        val angle = atan2(mv.SY, mv.SX) * 180f / PI
        return mRumbBorders.indexOfFirst { angle < it.majorBorder } - 16
    }

    fun setColors(myLocation: Vertex, enemy : EnemyInfo){
        val vec = myLocation.getMovementVector(enemy.mVertex)
        val directAngle = asin(vec.SY/vec.SX) * 180f/PI // asin -PI/2 ~ PI/2

    }
}