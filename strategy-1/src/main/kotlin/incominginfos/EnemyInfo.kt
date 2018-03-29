package incominginfos

import org.json.JSONObject

class EnemyInfo(enemyJson: JSONObject) {
    val mId: String = enemyJson.getString("Id")
    val mX: Float = enemyJson.getFloat("X")
    val mY: Float = enemyJson.getFloat("Y")
    val mMass: Float = enemyJson.getFloat("M")
    val mRadius: Float = enemyJson.getFloat("R")
}