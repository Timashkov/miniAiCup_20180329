package utils

data class Point(val X: Float, val Y: Float) {
    fun isOneSign(): Boolean = ((X > 0 && Y > 0) || (X < 0 && Y < 0))
    fun delta(pointTarget: Point, scaleFactor: Float = 1.0f): Point{
        val deltaX = (X - pointTarget.X) * scaleFactor
        val deltaY = (X - pointTarget.X) * scaleFactor
        return Point(deltaX, deltaY)
    }
}