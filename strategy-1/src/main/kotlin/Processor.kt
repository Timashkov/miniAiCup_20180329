import incominginfos.FoodInfo
import incominginfos.MineInfo
import incominginfos.WorldObjectsInfo
import org.json.JSONObject

class Processor(configJson: JSONObject) {

    val mWorldConfig = WorldConfig(configJson)

    var mCache: ParseResult? = null

    // Tick Process
    fun onTick(tickData: JSONObject): JSONObject {
        val parsed = parseIncoming(tickData)
        val out = analyzeData(parsed)
        mCache = parsed
        return out
    }

    fun findFood(worldObjects: WorldObjectsInfo): FoodInfo? {
        if (worldObjects.mFood.isEmpty())
            return null
        return worldObjects.mFood[0]
    }

    data class ParseResult(val mineInfo: MineInfo, val worldObjectsInfo: WorldObjectsInfo)

    fun parseIncoming(tickData: JSONObject): ParseResult = ParseResult(MineInfo(tickData.getJSONArray("Mine")), WorldObjectsInfo(tickData.getJSONArray("Objects")))

    fun analyzeData(parseResult: ParseResult): JSONObject {
        if (parseResult.mineInfo.isNotEmpty()) {

            val first = parseResult.mineInfo.getFragmentConfig(0)
            val food = findFood(parseResult.worldObjectsInfo)
                    ?: return JSONObject(mapOf("X" to 0, "Y" to 0, "Debug" to "No Food"))
            return JSONObject(mapOf<String, Any>("X" to food.mX, "Y" to food.mY))

        }
        return JSONObject(mapOf("X" to 0, "Y" to 0, "Debug" to "Died"))
    }
}


/* // направление движения в виде конечной точки;
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