package utils

import kotlin.math.abs

class Polygon(val center: Vertex, val diff: Float, val mapCenter: Vertex) {
    val corners: Array<Vertex>

    init {

        if (center.X > mapCenter.X) {
            if (center.Y > mapCenter.Y) {
                // 3
                val p1 = mapCenter.plus(Vertex(diff/2f, diff/2f))
                val p2 = center.plus(Vertex(diff, -diff))
                val p3 = center.plus(Vertex(diff, diff))
                val p4 = center.plus(Vertex(-diff, diff))
                corners = arrayOf(p1, p2, p3, p4)
            } else {
                // 2
                val p1 = mapCenter.plus(Vertex(diff/2f, -diff/2f))
                val p2 = center.plus(Vertex(-diff, -diff))
                val p3 = center.plus(Vertex(diff, -diff))
                val p4 = center.plus(Vertex(diff, diff))
                corners = arrayOf(p1, p2, p3, p4)
            }
        } else {
            if (center.Y > mapCenter.Y) {
                //4
                val p1 = mapCenter.plus(Vertex(-diff/2f, diff/2f))
                val p2 = center.plus(Vertex(diff, diff))
                val p3 = center.plus(Vertex(-diff, diff))
                val p4 = center.plus(Vertex(-diff, -diff))
                corners = arrayOf(p1, p2, p3, p4)
            } else {
                //1
                val p1 = mapCenter.plus(Vertex(-diff/2f, -diff/2f))
                val p2 = center.plus(Vertex(-diff, diff))
                val p3 = center.plus(Vertex(-diff, -diff))
                val p4 = center.plus(Vertex(diff, -diff))
                corners = arrayOf(p1, p2, p3, p4)
            }
        }
    }

    fun isInSquare(vertex: Vertex): Boolean = abs(vertex.X - center.X) <= diff && abs(vertex.Y - center.Y) <= diff
}