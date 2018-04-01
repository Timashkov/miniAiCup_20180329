package incominginfos

import org.json.JSONArray
import org.json.JSONObject

class WorldObjectsInfo(enemyJson: JSONArray) {
    val mViruses: ArrayList<VirusInfo> = ArrayList()
    var mFood: ArrayList<FoodInfo> = ArrayList()
    var mEjection: ArrayList<EjectionInfo> = ArrayList()
    var mEnemies: ArrayList<EnemyInfo> = ArrayList()

    init {
        enemyJson.map { it as JSONObject }.forEach {
            when (it.getString("T")) {
                "F" -> mFood.add(FoodInfo(it))
                "E" -> mEjection.add(EjectionInfo(it))
                "V" -> mViruses.add(VirusInfo(it))
                "P" -> mEnemies.add(EnemyInfo(it))
            }
        }
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