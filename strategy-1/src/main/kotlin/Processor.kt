import incominginfos.MineInfo
import incominginfos.WorldObjectsInfo
import org.json.JSONObject
import strategy.DefaultTurnStrategy
import strategy.FindFoodStrategy
import utils.Logger

class Processor(configJson: JSONObject) {

    private val mWorldConfig = WorldConfig(configJson)
    val mLogger = Logger()
    private val mFoodStrategy = FindFoodStrategy(mWorldConfig, mLogger)
    private val mDefaultStrategy = DefaultTurnStrategy(mWorldConfig, mLogger)
    var mCurrentTick = 0

    var mCache: ParseResult? = null

    // Tick Process
    fun onTick(tickData: JSONObject): JSONObject {
        mLogger.writeLog(tickData.toString())
        val parsed = parseIncoming(tickData)
        val out = analyzeData(parsed)
        mCache = parsed
        mCurrentTick++
        return out
    }

    data class ParseResult(val mineInfo: MineInfo, val worldObjectsInfo: WorldObjectsInfo)

    private fun parseIncoming(tickData: JSONObject): ParseResult = ParseResult(MineInfo(tickData.getJSONArray("Mine")), WorldObjectsInfo(tickData.getJSONArray("Objects")))

    private fun analyzeData(parseResult: ParseResult): JSONObject {
        if (parseResult.mineInfo.isNotEmpty()) {
            var strategyResult = mFoodStrategy.apply(parseResult.worldObjectsInfo, parseResult.mineInfo)
            mLogger.writeLog("FoodStrategy: " + strategyResult.toString())
            if (strategyResult.achievementScore < 0) {
                strategyResult = mDefaultStrategy.apply(parseResult.worldObjectsInfo, parseResult.mineInfo)
                mLogger.writeLog("Default Stratgy: " + strategyResult.toString())
            } else {
                mDefaultStrategy.breakPath()
            }

            return JSONObject(mapOf("X" to strategyResult.targetVertex.X, "Y" to strategyResult.targetVertex.Y, "Debug" to strategyResult.debugMessage))
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
