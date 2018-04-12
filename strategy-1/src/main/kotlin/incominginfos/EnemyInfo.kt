package incominginfos

import org.json.JSONObject
import utils.Vertex

class EnemyInfo(var mVertex: Vertex, var mId: String, var mMass: Float, var mRadius: Float) {
    //    var mVertex = Vertex(enemyJson.getFloat("X"), enemyJson.getFloat("Y"))
//    var mId: String = enemyJson.getString("Id")
//    var mMass: Float = enemyJson.getFloat("M")
//    var mRadius: Float = enemyJson.getFloat("R")
    var lastSeenTick: Int = 0

    constructor(enemyJson: JSONObject):
            this(Vertex(enemyJson.getFloat("X"), enemyJson.getFloat("Y")), enemyJson.getString("Id"), enemyJson.getFloat("M"), enemyJson.getFloat("R"))

    override fun toString(): String {
        return "Enemy: mass =$mMass rad = $mRadius id=$mId $mVertex lastSeen=$lastSeenTick"
    }
}