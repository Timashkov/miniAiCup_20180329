import data.ParseResult
import incominginfos.MineInfo
import incominginfos.WorldObjectsInfo
import org.json.JSONObject
import strategy.*
import utils.Logger

class Processor(configJson: JSONObject) {

    private val mWorldConfig = WorldConfig(configJson)
    private val mLogger = Logger()
    private val mFoodStrategy = FindFoodStrategy(mWorldConfig, mLogger)
    private val mDefaultStrategy = DefaultTurnStrategy(mWorldConfig, mLogger)
    private val mEatEnemyStrategy = EatEnemyStrategy(mWorldConfig, mLogger)
    private val mEvasionFilter = EvasionFilter(mWorldConfig, mLogger)
    private val mStartBurstStrategy = StarBurstStrategy(mWorldConfig, mLogger)
    private val mEscapeStrategy = EscapeStrategy(mWorldConfig, mLogger)
    private var mCurrentTick = 0

    // Tick Process
    fun onTick(tickData: JSONObject): JSONObject {
        mLogger.writeLog(tickData.toString())
        val parsed = parseIncoming(tickData)
        val out = analyzeData(parsed, mCurrentTick)
        mCurrentTick++
        return out
    }

    private fun parseIncoming(tickData: JSONObject): ParseResult = ParseResult(MineInfo(tickData.getJSONArray("Mine"), mWorldConfig), WorldObjectsInfo(tickData.getJSONArray("Objects"), mWorldConfig))

    private fun analyzeData(parseResult: ParseResult, currentTickCount: Int): JSONObject {
        val data = mEvasionFilter.onFilter(parseResult)
        if (data.mineInfo.isNotEmpty()) {

            mLogger.writeLog("Start check strategies")

            val strategyResults = listOf(
                    mEscapeStrategy.apply(data.worldObjectsInfo, data.mineInfo, currentTickCount),
                    // FussionStrategy
                    mEatEnemyStrategy.apply(data.worldObjectsInfo, data.mineInfo, currentTickCount),
                    mFoodStrategy.apply(data.worldObjectsInfo, data.mineInfo, currentTickCount),
                    mStartBurstStrategy.apply(data.worldObjectsInfo, data.mineInfo, currentTickCount),
                    mDefaultStrategy.apply(data.worldObjectsInfo, data.mineInfo, currentTickCount)
            )

            val chosen = strategyResults.sortedByDescending { it.achievementScore }[0]
            mLogger.writeLog("Chosen strategy: $chosen")
            return chosen.toJSONCommand()

//            var startegyResult = mEscapeStrategy.apply(data.worldObjectsInfo, data.mineInfo, currentTickCount)
//            if (startegyResult.achievementScore > -1) {
//                mDefaultStrategy.stopStrategy()
//                return startegyResult.toJSONCommand()
//            }



//            val strategyResults = listOf(mFoodStrategy.apply(data.worldObjectsInfo, data.mineInfo, currentTickCount), mEatEnemyStrategy.apply(data.worldObjectsInfo, data.mineInfo, currentTickCount))
//
//            val filtered = strategyResults.filter { it.achievementScore > 0 }
//            mLogger.writeLog("active strats count: " + filtered.size)
//
//            if (filtered.isNotEmpty()) {
//
//                mLogger.writeLog("Debug strats:---")
//                filtered.forEach {
//                    mLogger.writeLog(it.toString())
//                }
//                mLogger.writeLog("---")
//                val chosen = filtered.sortedByDescending { it.achievementScore }[0]
//                mLogger.writeLog("Chosen turn : " + chosen.toString())
//                mDefaultStrategy.stopStrategy()
//                return chosen.toJSONCommand()
//            }
//            val starBurstResult = mStartBurstStrategy.apply(data.worldObjectsInfo, data.mineInfo, currentTickCount)
//            if (starBurstResult.achievementScore > 0)
//                return starBurstResult.toJSONCommand()
//
//            val defaultResult = mDefaultStrategy.apply(data.worldObjectsInfo, data.mineInfo, currentTickCount)
//            mLogger.writeLog("Default result: " + defaultResult.toString())
//            return defaultResult.toJSONCommand()
        }
        return JSONObject(mapOf("X" to 0, "Y" to 0, "Debug" to "Died"))
    }
}


/*
points: food - 1, enemy_fragment - 10, enemy_total - 100, burst on virus - 2


// направление движения в виде конечной точки;
    // фрагменты будут подстраивать свои скорости (в соответствии с инерцией и остальной физикой),
    // чтобы приехать в указанную точку;
    "X": 123.0,
    "Y": 117.0,

    // отладочный вывод, который попадет в консоль браузера при визуализации;
    // максимум 1000 символов, все остальное будет обрезано;
    "Debug": "No food",

    // выполнить деление, выброс
    // "Split": true,
    // "Eject": true,

    // отладочный вывод в спрайт, который будет прикреплен в визуализаторе к игроку;
    // здесь как раз и нужны переданные во входных данных идентификаторы;
    "Sprite":
    {
        "Id": "1.1",
        "S": "Mass 50" // максимум 40 символов
    }*/

//{"FOOD_MASS":1,"GAME_HEIGHT":660,"GAME_TICKS":75000,"GAME_WIDTH":660,"INERTION_FACTOR":10,"MAX_FRAGS_CNT":10,"SPEED_FACTOR":25,"TICKS_TIL_FUSION":250,"VIRUS_RADIUS":22,"VIRUS_SPLIT_MASS":80,"VISCOSITY":0.25}
