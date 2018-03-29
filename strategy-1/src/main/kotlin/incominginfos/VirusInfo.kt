package incominginfos

import org.json.JSONObject

class VirusInfo(virusJson: JSONObject) {
    val mId: String = virusJson.getString("Id")
    val mX: Float = virusJson.getFloat("X")
    val mY: Float = virusJson.getFloat("Y")
    val mMass: Float = virusJson.getFloat("M")
}
/*
// пример игрового объекта - вируса
{
    // идентификатор объекта (string)
    "Id": "153",

    "X": 400.0, "Y": 400.0,

    // масса конкретного вируса (float)
    "M": 60.0,

    // тип объекта (string) (V=Virus)
    "T": "V"
},
        */