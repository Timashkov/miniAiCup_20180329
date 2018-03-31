package utils

import kotlin.math.abs

class Square(val center: Point, val diff: Float) {

    val corners: ArrayList<Point>
    init {
        val P1 = Point(center.X - diff, center.Y - diff)
        val P2 = Point(center.X - diff, center.Y + diff)
        val P3 = Point(center.X + diff, center.Y + diff)
        val P4 = Point(center.X + diff, center.Y - diff)
        corners = arrayListOf(P1, P2, P3, P4)
    }

    fun isInSquare(point: Point): Boolean = abs(point.X - center.X) <= diff && abs(point.Y - center.Y) <= diff


}