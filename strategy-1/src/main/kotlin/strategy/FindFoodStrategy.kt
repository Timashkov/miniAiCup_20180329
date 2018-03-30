package strategy

import incominginfos.MineInfo
import incominginfos.WorldObjectsInfo
import WorldConfig
import utils.Point

class FindFoodStrategy : IStrategy {

    var mDefaultTarget: Point = Point(0.0f, 0.0f)
    var mDeltas = Point(0.0f, 0.0f)

    override fun apply(globalConfig: WorldConfig, worldInfo: WorldObjectsInfo, mineInfo: MineInfo): StrategyResult {
        if (mDefaultTarget == Point(0.0f, 0.0f)) {
            mDeltas = globalConfig.getCenter().delta(mineInfo.getCoordinates())
            mDefaultTarget = getDefaultTarget(globalConfig.getCenter(), mDeltas)
        }

        if (worldInfo.mFood.isNotEmpty()) {
            return moveToFood(globalConfig, worldInfo)
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
        return StrategyResult(0.0f, mDefaultTarget, "Go TO Default")

    }

    private fun moveToFood(globalConfig: WorldConfig, worldInfo: WorldObjectsInfo): StrategyResult {
        return StrategyResult(globalConfig.FoodMass, Point(worldInfo.mFood[0].mX, worldInfo.mFood[0].mY), "Move to food")
    }

    private fun getDefaultTarget(center: Point, deltas: Point): Point {
        return Point(center.X + deltas.X, center.Y - deltas.Y)
    }
}

/*
* val first = parseResult.mineInfo.getFragmentConfig(0)
            val food = findFood(parseResult.worldObjectsInfo)
                    ?: return JSONObject(mapOf("X" to 0, "Y" to 0, "Debug" to "No Food"))
            return JSONObject(mapOf<String, Any>("X" to food.mX, "Y" to food.mY))
* */