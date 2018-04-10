package utils

import data.MovementVector
import kotlin.math.pow
import kotlin.math.sqrt

data class Vertex(val X: Float, val Y: Float) {

    fun isOneSign(): Boolean = ((X > 0 && Y > 0) || (X < 0 && Y < 0))
    fun getMovementVector(vertexTarget: Vertex, scaleFactor: Float = 1.0f): MovementVector {
        val deltaX = (vertexTarget.X - X) * scaleFactor
        val deltaY = (vertexTarget.Y - Y) * scaleFactor
        return MovementVector(deltaX, deltaY)
    }

    fun distance(target: Vertex): Float {
        return sqrt((X - target.X).pow(2) + (Y - target.Y).pow(2))
    }

    override fun toString(): String {
        return "Vertex: $X : $Y"
    }

    fun equals(other: Vertex?): Boolean {
        if (other == null)
            return false
        return other.X.toInt() == this.X.toInt() && other.Y.toInt() == this.Y.toInt()
    }

    fun plus(v: Vertex): Vertex = Vertex(X + v.X, Y + v.Y)
    fun minus(v: Vertex): Vertex = Vertex(X - v.X, Y - v.Y)
}