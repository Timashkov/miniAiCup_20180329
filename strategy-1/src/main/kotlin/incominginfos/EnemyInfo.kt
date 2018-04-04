package incominginfos

import org.json.JSONObject
import utils.Vertex

class EnemyInfo(enemyJson: JSONObject) {
    val mVertex = Vertex(enemyJson.getFloat("X"), enemyJson.getFloat("Y"))
    val mId: String = enemyJson.getString("Id")
    val mMass: Float = enemyJson.getFloat("M")
    val mRadius: Float = enemyJson.getFloat("R")

    override fun toString(): String {
        return "Enemy: mass =$mMass rad = $mRadius id=$mId $mVertex"
    }
}