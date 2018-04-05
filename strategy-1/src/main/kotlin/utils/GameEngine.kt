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
        if (vectorTarget == MovementVector(0f,0f)){
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
            dest = fixByBorders(source, dest)

        return dest
    }

    private fun fixByBorders(source: Vertex, dest: Vertex): Vertex {
        val k = (dest.X - source.X) / (dest.Y - source.Y)
        val b = dest.Y - k * dest.X

        /////////

        return dest
    }

    companion object {
        val DEBUG_TAG = "GameEngine"
    }
    // Return point between fragments ??
    // turn for other fragments ?
}