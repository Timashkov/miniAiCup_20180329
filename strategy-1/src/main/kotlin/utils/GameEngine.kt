package utils

import data.ParseResult
import WorldConfig
import data.MovementVector
import kotlin.math.pow
import kotlin.math.sqrt

class GameEngine(private val globalConfig: WorldConfig, val worldParseResult: ParseResult, val currentTick: Int, val mLogger: Logger) {

    fun getMovementPointForTarget(fragmentId: String, target: Vertex): Vertex {
        val fragment = worldParseResult.mineInfo.getFragmentById(fragmentId)

        mLogger.writeLog("$DEBUG_TAG $fragment")
        val sVector = MovementVector(fragment.mSX, fragment.mSY)

        //for one fragment

        mLogger.writeLog("$DEBUG_TAG $sVector ${fragment.mVertex} $target")

        val inertionK = globalConfig.InertionFactor / fragment.mMass
        mLogger.writeLog("$DEBUG_TAG inertion = $inertionK")
        val maxSpeed = globalConfig.SpeedFactor / sqrt(fragment.mMass)

        mLogger.writeLog("$DEBUG_TAG maxSpeed = $maxSpeed")
        val distance = fragment.mVertex.distance(target)

        val vectorTarget = fragment.mVertex.getMovementVector(target, 8f/distance).minus(sVector)

        mLogger.writeLog("$DEBUG_TAG vector target = $vectorTarget")
        if (vectorTarget == MovementVector(0f, 0f)) {
            //no move
            return target
        }

        var NX = ((vectorTarget.SX - sVector.SX) / inertionK + sVector.SX) / maxSpeed
        var NY = ((vectorTarget.SY - sVector.SY) / inertionK + sVector.SY) / maxSpeed

        //FIX: border
        if (fragment.mRadius + 0.2f >= fragment.mVertex.Y && NY < 0
                || fragment.mRadius + 0.2f >= globalConfig.GameHeight - fragment.mVertex.Y && NY > 0)
            NY = 0f
        if (fragment.mRadius + 0.2f >= fragment.mVertex.X && NX < 0
                || fragment.mRadius + 0.2f >= globalConfig.GameWidth - fragment.mVertex.X && NX > 0)
            NX = 0f

        mLogger.writeLog("$DEBUG_TAG NX=$NX NY=$NY")

        val factor = 1 / sqrt(NX.pow(2) + NY.pow(2)) * fragment.mVertex.distance(target)
        mLogger.writeLog("$DEBUG_TAG factor $factor")
        mLogger.writeLog("$DEBUG_TAG Ktarget = ${vectorTarget.K} KN = ${MovementVector(NX, NY).K}")

        NX *= factor //
        NY *= factor

        return vectorEdgeCrossPoint(fragment.mVertex, MovementVector(NX, NY), globalConfig.GameWidth.toFloat(), globalConfig.GameHeight.toFloat())
    }

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