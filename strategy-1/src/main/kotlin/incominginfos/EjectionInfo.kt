package incominginfos

import org.json.JSONObject
import utils.Vertex

class EjectionInfo(ejectionJson: JSONObject) {
    val mVertex = Vertex(ejectionJson.getFloat("X"), ejectionJson.getFloat("Y"))
}