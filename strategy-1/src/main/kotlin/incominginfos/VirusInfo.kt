package incominginfos

import org.json.JSONObject

class VirusInfo(virusJson: JSONObject) {
    val mId: String = virusJson.getString("Id")
    val mX: Float = virusJson.getFloat("X")
    val mY: Float = virusJson.getFloat("Y")
    val mMass: Float = virusJson.getFloat("M")
}
/*
каждые 1200 тиков
критическая масса - мировой конфиг
начальная масса 40

если масса игрока > 120 -> происходит взрыв радиусом из мирового конфига
*/