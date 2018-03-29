package incominginfos

import org.json.JSONObject

class FoodInfo(foodJson: JSONObject) {
    val mX: Float = foodJson.getFloat("X")
    val mY: Float = foodJson.getFloat("Y")
}

/*
* каждые 40 тактов
* радиус 2.5
* масса - конфиг мира
* */