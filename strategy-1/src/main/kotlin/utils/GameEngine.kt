package utils

import data.ParseResult
import WorldConfig
import data.MovementVector

class GameEngine(private val globalConfig: WorldConfig, val worldParseResult: ParseResult, val currentTick: Int, val mLogger: Logger) {

    companion object {
        val DEBUG_TAG = "GameEngine"

        fun vectorEdgeCrossPoint(source: Vertex, vector: MovementVector, maxX: Float, maxY: Float): Vertex {
            if (vector.SX == 0f) {
                if (vector.SY == 0f) {
                    return source
                }
                if (vector.SY > 0f) {
                    return Vertex(source.X, maxY)
                }
                if (vector.SY < 0f) {
                    return Vertex(source.X, 0f)
                }
            }
            if (vector.SY == 0f) {
                if (vector.SX > 0) {
                    return Vertex(maxX, vector.SY)
                }
                if (vector.SX < 0) {
                    return Vertex(0f, vector.SY)
                }
            }

            return vectorEdgeCrossPointInternal(source, vector.SX, vector.SY, maxX, maxY)
        }


        fun fixByBorders(source: Vertex, dest: Vertex, maxX: Float, maxY: Float): Vertex {

            if (dest.X == source.X) {
                if (dest.Y == source.Y) {
                    return source
                }
                if (dest.Y > source.Y) {
                    return Vertex(source.X, maxY)
                }
                if (dest.Y < source.Y) {
                    return Vertex(source.X, 0f)
                }
            }
            if (dest.Y == source.Y) {
                if (dest.X > source.X) {
                    return Vertex(maxX, source.Y)
                }
                if (dest.X < source.X) {
                    return Vertex(0f, source.Y)
                }
            }

            val deltaX = dest.X - source.X
            val deltaY = dest.Y - source.Y

            return vectorEdgeCrossPointInternal(source, deltaX, deltaY, maxX, maxY)
        }

        fun vectorEdgeCrossPointInternal(source: Vertex, deltaX: Float, deltaY: Float, maxX: Float, maxY: Float): Vertex {
            val k = deltaY / deltaX
            val b = source.Y - k * source.X


            val minBK = -b / k
            val maxBK = (maxY - b) / k

            val vert1 = Vertex(0f, b)
            val vert2 = Vertex(minBK, 0f)
            val vert3 = Vertex(maxBK, maxY)
            val vert4 = Vertex(maxX, maxX * k + b)


            if (deltaX > 0) {
                if (deltaY < 0 && minBK <= maxX && minBK >= 0) {
                    //vert2 || vert4
                    return vert2
                }
                if (deltaY > 0 && maxBK <= maxX && maxBK >= 0) {
                    //vert 3 || vert 4
                    return vert3
                }
                return vert4
            } else {

                if (deltaY < 0 && minBK >= 0 && minBK <= maxY) {
                    //vert2 || vert1
                    return vert2
                }
                if (deltaY > 0 && maxBK >= 0 && minBK <= maxY) {
                    //vert 3 || vert 1
                    return vert3
                }
                return vert1
            }
        }
    }
    // Return point between fragments ??
    // turn for other fragments ?
}