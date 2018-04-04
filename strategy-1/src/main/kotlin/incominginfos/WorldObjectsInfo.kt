package incominginfos

import org.json.JSONArray
import org.json.JSONObject
import WorldConfig

class WorldObjectsInfo(enemyJson: JSONArray, globalConfig: WorldConfig) {
    val mViruses: List<VirusInfo>
    var mFood: List<FoodInfo>
    var mEjection: List<EjectionInfo>
    var mEnemies: List<EnemyInfo>

    init {
        val food = ArrayList<FoodInfo>()
        val ejection = ArrayList<EjectionInfo>()
        val enemies = ArrayList<EnemyInfo>()
        val viruses = ArrayList<VirusInfo>()
        enemyJson.map { it as JSONObject }.forEach {
            when (it.getString("T")) {
                "F" -> {
                    val f = FoodInfo(it, globalConfig.FoodMass)
                    if (!food.contains(f)) food.add(f)
                }
                "E" -> {
                    val e = EjectionInfo(it)
                    if (!ejection.contains(e)) ejection.add(e)
                }
                "V" -> {
                    val v = VirusInfo(it)
                    if (!viruses.contains(v)) viruses.add(v)
                }
                "P" -> {
                    val e = EnemyInfo(it)
                    if (!enemies.contains(e)) enemies.add(e)
                }
            }
        }
        mFood = food
        mEjection = ejection
        mEnemies = enemies
        mViruses = viruses
    }
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