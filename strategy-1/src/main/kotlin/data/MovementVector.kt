package data

import utils.Vertex
import kotlin.math.abs

class MovementVector(val SX: Float, val SY: Float) {
    private val K: Float = SY / SX

    fun plus(v: MovementVector): MovementVector = MovementVector(SX + v.SX, SY + v.SY)
    fun minus(v: MovementVector): MovementVector = MovementVector(SX - v.SX, SY - v.SY)

    fun crossPointWithBorders(totalWidth: Float, totalHeight: Float, sourceVertex: Vertex): Vertex {

        val b = sourceVertex.Y - K * sourceVertex.X


        if (abs(SX) >= abs(SY)) {
            //crosspoint with vert border
            if (SX > 0) {
                return Vertex(totalWidth, totalWidth * K + b)

            } else if (SX < 0) {
                return Vertex(0f, b)

            } else {
                return sourceVertex

            }
        } else {
            //crosspoint with hor border
            if (SY > 0) {
                return Vertex((totalHeight - b) / K, totalHeight)
            } else {
                //SY < 0 , == 0 parsed in prev
                return Vertex(-b / K, 0f)
            }
        }
    }
}