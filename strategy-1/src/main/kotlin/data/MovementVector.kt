package data

class MovementVector(var SX: Float, var SY: Float) {
    val K: Float = SY / SX

    fun plus(v: MovementVector): MovementVector = MovementVector(SX + v.SX, SY + v.SY)
    fun minus(v: MovementVector): MovementVector = MovementVector(SX - v.SX, SY - v.SY)

    override fun toString(): String {
        return "($SX; $SY)"
    }
}