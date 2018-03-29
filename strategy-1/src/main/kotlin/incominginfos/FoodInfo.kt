package incominginfos

import org.json.JSONObject

class FoodInfo(foodJson: JSONObject) {
    val mX: Float = foodJson.getFloat("X")
    val mY: Float = foodJson.getFloat("Y")
}