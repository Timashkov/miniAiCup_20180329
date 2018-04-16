package incominginfos

import org.json.JSONObject
import utils.Vertex

class EjectionInfo(ejectionJson: JSONObject, val mMass: Float = 15f) {
    val mVertex = Vertex(ejectionJson.getFloat("X"), ejectionJson.getFloat("Y"))
    val pId = ejectionJson.getInt("pId")
}