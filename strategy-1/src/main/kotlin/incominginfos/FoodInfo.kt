package incominginfos

import org.json.JSONObject
import utils.Vertex

class FoodInfo(foodJson: JSONObject) {
    val mVertex = Vertex(foodJson.getFloat("X"), foodJson.getFloat("Y"))
}

/*
* каждые 40 тактов
* радиус 2.5
* масса - конфиг мира
* */