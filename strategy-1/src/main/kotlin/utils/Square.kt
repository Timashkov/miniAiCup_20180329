package utils

import kotlin.math.abs

class Square(val center: Vertex, val diff: Float) {

    val corners: Array<Vertex>
    init {
        val P1 = Vertex(center.X - diff, center.Y - diff)
        val P2 = Vertex(center.X - diff, center.Y + diff)
        val P3 = Vertex(center.X + diff, center.Y + diff)
        val P4 = Vertex(center.X + diff, center.Y - diff)
        corners = arrayOf(P1, P2, P3, P4)
    }

    fun isInSquare(vertex: Vertex): Boolean = abs(vertex.X - center.X) <= diff && abs(vertex.Y - center.Y) <= diff


}