package strategy

import incominginfos.MineInfo
import incominginfos.WorldObjectsInfo
import WorldConfig
import utils.Point

class DefaultTurnStrategy : IStrategy{

    var mDefaultTarget: Point = Point(0.0f, 0.0f)
    var mDeltas = Point(0.0f, 0.0f)

    override fun apply(globalConfig: WorldConfig, worldInfo: WorldObjectsInfo, mineInfo: MineInfo): StrategyResult {
        if (mDefaultTarget == Point(0.0f, 0.0f)) {
            mDeltas = globalConfig.getCenter().delta(mineInfo.getCoordinates())
            mDefaultTarget = getDefaultTarget(globalConfig.getCenter(), mDeltas)
        }

        if (mineInfo.getCoordinates() == mDefaultTarget) {
            var deltaX = mDeltas.X
            var deltaY = mDeltas.Y
            if (mDeltas.isOneSign())
                deltaX *= -1.0f
            else
                deltaY *= -1.0f
            mDeltas = Point(deltaX, deltaY)

            mDefaultTarget = getDefaultTarget(globalConfig.getCenter(), mDeltas)
        }
        return StrategyResult(0.0f, mDefaultTarget, "DEFAULT: Go TO Default")
    }
    private fun getDefaultTarget(center: Point, deltas: Point): Point {
        return Point(center.X + deltas.X, center.Y - deltas.Y)
    }
}