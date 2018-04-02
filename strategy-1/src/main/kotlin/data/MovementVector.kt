package data

import utils.Vertex

class MovementVector(val SX: Float, val SY: Float){
    fun plus(v: MovementVector): MovementVector = MovementVector(SX + v.SX, SY + v.SY)
    fun minus(v: MovementVector): MovementVector = MovementVector(SX - v.SX, SY - v.SY)
}