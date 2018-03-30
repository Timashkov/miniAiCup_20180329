package strategy

import incominginfos.MineInfo
import incominginfos.WorldObjectsInfo
import WorldConfig
import utils.Point

class FindFoodStrategy : IStrategy {

    override fun apply(globalConfig: WorldConfig, worldInfo: WorldObjectsInfo, mineInfo: MineInfo): StrategyResult {

        if (worldInfo.mFood.isNotEmpty()) {
            return moveToFood(globalConfig, worldInfo)
        }

        return StrategyResult(-1.0f, Point(0.0f, 0.0f), "FindFood: Not applied")
    }

    private fun moveToFood(globalConfig: WorldConfig, worldInfo: WorldObjectsInfo): StrategyResult {
        return StrategyResult(globalConfig.FoodMass, Point(worldInfo.mFood[0].mX, worldInfo.mFood[0].mY), "Move to food")
    }

}

/*
* val first = parseResult.mineInfo.getFragmentConfig(0)
            val food = findFood(parseResult.worldObjectsInfo)
                    ?: return JSONObject(mapOf("X" to 0, "Y" to 0, "Debug" to "No Food"))
            return JSONObject(mapOf<String, Any>("X" to food.mX, "Y" to food.mY))
* */