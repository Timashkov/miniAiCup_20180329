package incominginfos

import org.json.JSONObject

class EjectionInfo(ejectionJson: JSONObject) {
    val mX: Float = ejectionJson.getFloat("X")
    val mY: Float = ejectionJson.getFloat("Y")
}