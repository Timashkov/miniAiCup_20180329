package utils

import data.ParseResult
import WorldConfig
import data.MovementVector
import kotlin.math.pow
import kotlin.math.sqrt

class GameEngine(private val globalConfig: WorldConfig, val worldParseResult: ParseResult, val currentTick: Int, val mLogger: Logger) {

    fun getMovementPointForTarget(fragmentId: String, currentPosition: Vertex, target: Vertex): Vertex {
        val fragment = worldParseResult.mineInfo.getFragmentById(fragmentId)

        mLogger.writeLog("$DEBUG_TAG $fragment")
        return getMovementPointForTargetInner(fragment.mMass, MovementVector(fragment.mSX, fragment.mSY), currentPosition, target)
    }

    fun getMovementPointForTargetInner(mass: Float, sVector: MovementVector, source: Vertex, target: Vertex): Vertex {
        //for one fragment

        mLogger.writeLog("$DEBUG_TAG $sVector $source $target")

        val inertionK = globalConfig.InertionFactor / mass
        mLogger.writeLog("$DEBUG_TAG inertion = $inertionK")
        val maxSpeed = globalConfig.SpeedFactor / sqrt(mass)

        mLogger.writeLog("$DEBUG_TAG maxSpeed = $maxSpeed")
        val vectorTarget = source.getMovementVector(target).minus(sVector)
        mLogger.writeLog("$DEBUG_TAG vector target = $vectorTarget")
        if (vectorTarget == MovementVector(0f, 0f)) {
            //no move
            return target
        }

        var NX = ((vectorTarget.SX - sVector.SX) / inertionK + sVector.SX) / maxSpeed
        var NY = ((vectorTarget.SY - sVector.SY) / inertionK + sVector.SY) / maxSpeed

        mLogger.writeLog("$DEBUG_TAG NX=$NX NY=$NY")

        val factor = 1 / sqrt(NX.pow(2) + NY.pow(2)) * source.distance(target)
        mLogger.writeLog("$DEBUG_TAG factor $factor")
        mLogger.writeLog("$DEBUG_TAG Ktarget = ${vectorTarget.K} KN = ${MovementVector(NX, NY).K}")

        NX *= factor //
        NY *= factor

        var dest = source.plus(Vertex(NX, NY))
        if (dest.X > globalConfig.GameWidth || dest.X < 0 || dest.Y > globalConfig.GameHeight || dest.Y < 0)
            dest = fixByBorders(source, dest, globalConfig.GameWidth.toFloat(), globalConfig.GameHeight.toFloat())

        return dest
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