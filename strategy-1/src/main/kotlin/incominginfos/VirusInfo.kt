package incominginfos

import org.json.JSONObject
import utils.Vertex

class VirusInfo(virusJson: JSONObject) {
    val mVertex = Vertex(virusJson.getFloat("X"), virusJson.getFloat("Y"))
    val mId: String = virusJson.getString("Id")
    val mMass: Float = virusJson.getFloat("M")

    override fun toString(): String {
        return "Virus: id = $mId $mVertex of mass $mMass"
    }
}
/*
каждые 1200 тиков
критическая масса - мировой конфиг
начальная масса 40

если масса игрока > 120 -> происходит взрыв радиусом из мирового конфига
*/