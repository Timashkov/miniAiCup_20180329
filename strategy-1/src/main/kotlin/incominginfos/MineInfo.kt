package incominginfos

import org.json.JSONArray
import org.json.JSONObject
import utils.Point


class MineInfo(stateJson: JSONArray) {
    val mFragmentsState: ArrayList<MineFragmentInfo> = ArrayList()

    init {
        stateJson.map { it as JSONObject }.forEach { mFragmentsState.add(MineFragmentInfo(it)) }
    }

    fun isNotEmpty(): Boolean {
        return mFragmentsState.isNotEmpty()
    }

    fun getFragmentConfig(index: Int): MineFragmentInfo = mFragmentsState[index]

    fun getCoordinates(): Point{
        return Point(mFragmentsState[0].mX, mFragmentsState[0].mY)
    }
}

//{\"Mine\":[{\"Id\":\"1\",\"M\":40,\"R\":12.649110640673518,\"SX\":0,\"SY\":0,\"TTF\":32,\"X\":474,\"Y\":178}]