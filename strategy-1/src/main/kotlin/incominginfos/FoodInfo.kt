package incominginfos

import org.json.JSONObject
import utils.Vertex

class FoodInfo(foodJson: JSONObject, val mMass: Float) {
    val mVertex = Vertex(foodJson.getFloat("X"), foodJson.getFloat("Y"))
    override fun toString(): String {
        return "$mVertex ($mMass)"
    }
}

/*
* каждые 40 тактов
* радиус 2.5
* масса - конфиг мира
* */