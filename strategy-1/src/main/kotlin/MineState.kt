import org.json.JSONArray
import org.json.JSONObject


class MineState(stateJson: JSONArray) {
    val mFragmentsState: ArrayList<MineFragmentState> = ArrayList()

    init {
        stateJson.map { it as JSONObject }.forEach { mFragmentsState.add(MineFragmentState(it)) }
    }

    fun isNotEmpty(): Boolean {
        return mFragmentsState.isNotEmpty()
    }

    fun getFragmentConfig(index: Int): MineFragmentState = mFragmentsState[index]

}

//{\"Mine\":[{\"Id\":\"1\",\"M\":40,\"R\":12.649110640673518,\"SX\":0,\"SY\":0,\"TTF\":32,\"X\":474,\"Y\":178}]