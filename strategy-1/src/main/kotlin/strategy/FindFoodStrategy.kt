package strategy

import incominginfos.MineInfo
import incominginfos.WorldObjectsInfo
import WorldConfig
import utils.Point

class FindFoodStrategy(globalConfig: WorldConfig) : IStrategy {
    val mFoodMass = globalConfig.FoodMass

    override fun apply(worldInfo: WorldObjectsInfo, mineInfo: MineInfo): StrategyResult {

        if (worldInfo.mFood.isNotEmpty()) {
            analyzePlate(worldInfo)
        }

        return StrategyResult(-1.0f, Point(0.0f, 0.0f), "FindFood: Not applied")
    }

    private fun analyzePlate(worldInfo: WorldObjectsInfo): StrategyResult{
        return StrategyResult(mFoodMass, Point(worldInfo.mFood[0].mX, worldInfo.mFood[0].mY), "Move to food")
    }


}

/*
* val first = parseResult.mineInfo.getFragmentConfig(0)
            val food = findFood(parseResult.worldObjectsInfo)
                    ?: return JSONObject(mapOf("X" to 0, "Y" to 0, "Debug" to "No Food"))
            return JSONObject(mapOf<String, Any>("X" to food.mX, "Y" to food.mY))
* */

/*
* 1) еда одна ? - идем
* 2) выбор группы или одной ( критерии - кол-во и удаленность )
* 3) могу ли я достигнуть выгодной группы за один шаг? - идем
* 4) есть ли в напр группы еще еда , достижимая за один шаг? - идем к еде
* 5) идем к группе
*
* */