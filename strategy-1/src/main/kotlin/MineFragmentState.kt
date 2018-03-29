import org.json.JSONObject

class MineFragmentState(fragmentJson: JSONObject) {
    val mId: String = fragmentJson.getString("Id")
    val mMass: Float = fragmentJson.getFloat("M")
    val mRadius : Float = fragmentJson.getFloat("R")
    val mX: Float = fragmentJson.getFloat("X")
    val mY: Float = fragmentJson.getFloat("Y")
    val mSX: Float = fragmentJson.getFloat("SX")
    val mSY: Float = fragmentJson.getFloat("SY")
    val mTTF: Int = if (fragmentJson.has("TTF")) fragmentJson.getInt("TTF") else 0
}

/*    {
            // уникальный идентификатор (string)
            // для игроков через точку записывается номер фрагмента (если есть)
            "Id": "1.1",

            // координаты в пространстве (float)
            "X": 100.0, "Y": 100.0,

            // радиус и масса (float)
            "R": 8.0, "M": 40.0,

            // скорость в проекциях Ox и Oy (float)
            "SX": 0.365, "SY": 14.0,

            // таймер слияния (int) (если есть)
            // "TTF": 250,
        },*/