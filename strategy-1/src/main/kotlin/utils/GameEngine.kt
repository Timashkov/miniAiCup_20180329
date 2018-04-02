package utils

import data.ParseResult
import WorldConfig
import data.MovementVector
import kotlin.math.pow
import kotlin.math.sqrt

class GameEngine(val globalConfig: WorldConfig, val worldParseResult: ParseResult, val currentTick : Int) {


    fun getMovementPointForTarget(currentPosition: Vertex, target: Vertex): Vertex {
        //for one fragment
        val fragment = worldParseResult.mineInfo.getMainFragment()
        val inertionK = globalConfig.InertionFactor / fragment.mMass
        val maxSpeed = globalConfig.SpeedFactor / sqrt(fragment.mMass)

        val vectorTarget = currentPosition.getMovementVector(target).minus(MovementVector(fragment.mSX, fragment.mSY))

        var NX = ((vectorTarget.SX - fragment.mSX) / inertionK + fragment.mSX) / maxSpeed
        var NY = ((vectorTarget.SY - fragment.mSY) / inertionK + fragment.mSY) / maxSpeed

        val factor = sqrt(NX.pow(2) + NY.pow(2))
        NX = NX / factor // 1/factor * NX
        NY = NY / factor

        return Vertex(currentPosition.X + NX, currentPosition.Y + NY)
    }
}