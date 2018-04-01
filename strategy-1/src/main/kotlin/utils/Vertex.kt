package utils

import kotlin.math.pow
import kotlin.math.sqrt

data class Vertex(val X: Float, val Y: Float) {

    fun isOneSign(): Boolean = ((X > 0 && Y > 0) || (X < 0 && Y < 0))
    fun delta(vertexTarget: Vertex, scaleFactor: Float = 1.0f): Vertex {
        val deltaX = (X - vertexTarget.X) * scaleFactor
        val deltaY = (X - vertexTarget.X) * scaleFactor
        return Vertex(deltaX, deltaY)
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
        return other.X == this.X && other.Y == this.Y
    }
}