package data

class MovementVector(val SX: Float, val SY: Float) {
    val K: Float = SY / SX

    fun plus(v: MovementVector): MovementVector = MovementVector(SX + v.SX, SY + v.SY)
    fun minus(v: MovementVector): MovementVector = MovementVector(SX - v.SX, SY - v.SY)

    override fun toString(): String {
        return "($SX; $SY)"
    }
}