package strategy

import incominginfos.MineInfo
import incominginfos.WorldObjectsInfo
import WorldConfig
import utils.Point

class FindFoodStrategy : IStrategy {

    var mDefaultTarget: Point? = null
    var mDeltas = Point(0.0f, 0.0f)

    override fun apply(globalConfig: WorldConfig, worldInfo: WorldObjectsInfo, mineInfo: MineInfo): StrategyResult {
        if (mDefaultTarget == null) {
            mDeltas = globalConfig.getCenter().delta(mineInfo.getCoordinates())
            mDefaultTarget = getDefaultTarget(globalConfig.GameWidth.toFloat(), mineInfo.mFragmentsState[0].mY, mDeltas)
        }

        if (worldInfo.mFood.isNotEmpty()) {
            return moveToFood(globalConfig, worldInfo)
        }

        mDefaultTarget?.let {
            if (mineInfo.mFragmentsState[0].mX == it.X && mineInfo.mFragmentsState[0].mY == it.Y) {
                var deltaX = mDeltas.X
                var deltaY = mDeltas.Y
                if (mDeltas.isOneSign())
                    deltaX *= -1.0f
                else
                    deltaY *= -1.0f
                mDeltas = Point(deltaX, deltaY)
                mDefaultTarget = getDefaultTarget(globalConfig.GameWidth.toFloat(), mineInfo.mFragmentsState[0].mY, mDeltas)
            }
            return StrategyResult(0.0f, it)
        }
        return StrategyResult(0.0f, Point(0.0f, 0.0f), "FAIL")
    }

    private fun moveToFood(globalConfig: WorldConfig, worldInfo: WorldObjectsInfo): StrategyResult {
        return StrategyResult(globalConfig.FoodMass, Point(worldInfo.mFood[0].mX, worldInfo.mFood[0].mY))
    }

    private fun getDefaultTarget(maxX: Float, myY: Float, deltas: Point): Point {
        return Point(maxX / 2.0f + deltas.X, myY + deltas.Y) // all correct : center X and my Y
    }
}

/*
* val first = parseResult.mineInfo.getFragmentConfig(0)
            val food = findFood(parseResult.worldObjectsInfo)
                    ?: return JSONObject(mapOf("X" to 0, "Y" to 0, "Debug" to "No Food"))
            return JSONObject(mapOf<String, Any>("X" to food.mX, "Y" to food.mY))
* */