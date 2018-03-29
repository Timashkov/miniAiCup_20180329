import org.json.JSONArray
import org.json.JSONObject

class Processor(configJson: JSONObject) {

    val mWorldConfig = WorldConfig(configJson)

    fun onTick(tickData: JSONObject): JSONObject {
        val mine = MineState(tickData.getJSONArray("Mine"))
        val objects = tickData.getJSONArray("Objects")
        if (mine.isNotEmpty()) {
            val first = mine.getFragmentConfig(0)
            val food = findFood(objects) ?: return JSONObject(mapOf("X" to 0, "Y" to 0, "Debug" to "No Food"))
            return JSONObject(mapOf<String, Any>("X" to food["X"], "Y" to food["Y"]))
        }
        return JSONObject(mapOf("X" to 0, "Y" to 0, "Debug" to "Died"))
    }

    fun findFood(objects: JSONArray): JSONObject? =
            objects.map { it as JSONObject }.firstOrNull { it["T"] == "F" }
}

/*// объекты, видимые участнику (другие игроки, вирусы, еда, выбросы)
    "Objects": [

        // пример игрового объекта - еды
        {
            "X": 200.0, "Y": 200.0,

            // тип объекта (string) (F=Food)
            "T": "F"
        },

        // пример игрового объекта - выброса
        {
            "X": 300.0, "Y": 300.0,

            // тип объекта (string) (E=Ejection)
            "T": "E"
        },

        // пример игрового объекта - вируса
        {
            // идентификатор объекта (string)
            "Id": "153",

            "X": 400.0, "Y": 400.0,

            // масса конкретного вируса (float)
            "M": 60.0,

            // тип объекта (string) (V=Virus)
            "T": "V"
        },

        // пример игрового объекта - чужой игрок
        {
            // идентификатор объекта (string)
            "Id": "2",

            "X": 150.0, "Y": 150.0,

            // масса и радиус (float)
            "M": 60.0, "R": 10.0,

            // тип объекта (string) (P=Player)
            "T": "P"
        },

        // другие объекты аналогично
        ...
    ]*/

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