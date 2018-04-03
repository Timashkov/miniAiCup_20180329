package data

import utils.Vertex
import kotlin.math.abs

class MovementVector(val SX: Float, val SY: Float) {
    fun plus(v: MovementVector): MovementVector = MovementVector(SX + v.SX, SY + v.SY)
    fun minus(v: MovementVector): MovementVector = MovementVector(SX - v.SX, SY - v.SY)

    fun crossPointWithBorders(totalWidth: Float, totalHeight: Float): Vertex {

        if (abs(SX) >= abs(SY)) {
            //crosspoint with vert border
            if (SX > 0) {
                return Vertex(totalWidth, totalHeight / 2 + SY * (totalWidth / 2) / SX)
            } else if (SX < 0) {
                return Vertex(0f, totalHeight / 2 + SY * (totalWidth / 2) / SX)
            } else {
                return Vertex(totalWidth / 2, totalHeight / 2)
            }
        } else {
            //crosspoint with hor border
            if (SY > 0){
                return Vertex(totalWidth + (totalHeight/2 * SX) / SY, totalHeight)
            } else {
                //SY < 0 , == 0 parsed in prev
                return Vertex(totalWidth + (totalHeight/2 * SX) / SY, 0f)
            }
        }
    }
}