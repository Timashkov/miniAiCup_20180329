package data

import utils.Vertex
import kotlin.math.abs

class MovementVector(val SX: Float, val SY: Float) {
    val K: Float = SY / SX

    fun plus(v: MovementVector): MovementVector = MovementVector(SX + v.SX, SY + v.SY)
    fun minus(v: MovementVector): MovementVector = MovementVector(SX - v.SX, SY - v.SY)

//  TODO:required review
//    fun crossPointWithBorders(totalWidth: Float, totalHeight: Float, sourceVertex: Vertex): Vertex {
//
//        val b = sourceVertex.Y - K * sourceVertex.X
//
//
//        /*
//        * NX -30.688663
//        * NY = 56.919945
//        * X = 191.636
//        * Y = 187.953
//        *
//        * Res = -41.094, 660
//        * */
//        if (abs(SX) >= abs(SY)) {
//            //crosspoint with vert border
//            if (SX > 0) {
//                return Vertex(totalWidth, totalWidth * K + b)
//
//            } else if (SX < 0) {
//                return Vertex(0f, b)
//
//            } else {
//                return sourceVertex
//
//            }
//        } else {
//            //crosspoint with hor border
//            if (SY > 0) {
//                return Vertex((totalHeight - b) / K, totalHeight)
//            } else {
//                //SY < 0 , == 0 parsed in prev
//                return Vertex(-b / K, 0f)
//            }
//        }
//    }

    override fun toString(): String {
        return "($SX; $SY)"
    }
}