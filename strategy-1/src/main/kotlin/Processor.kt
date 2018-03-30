import incominginfos.FoodInfo
import incominginfos.MineInfo
import incominginfos.WorldObjectsInfo
import org.json.JSONObject
import strategy.FindFoodStrategy

class Processor(configJson: JSONObject) {

    private val mWorldConfig = WorldConfig(configJson)
    private val mFoodStrategy = FindFoodStrategy()
    var mCurrentTick = 0

    var mCache: ParseResult? = null

    // Tick Process
    fun onTick(tickData: JSONObject): JSONObject {
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
            val strategyResult = mFoodStrategy.apply(mWorldConfig, parseResult.worldObjectsInfo, parseResult.mineInfo)
            return JSONObject(mapOf("X" to strategyResult.targetPoint.X, "Y" to strategyResult.targetPoint.Y, "Debug" to strategyResult.debugMessage))
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