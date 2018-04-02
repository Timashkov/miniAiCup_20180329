package strategy

import org.json.JSONObject
import utils.Vertex

data class StrategyResult(val achievementScore: Int, val targetVertex: Vertex, val eject: Boolean = false, val split: Boolean = false, val debugMessage: String = "") {
    override fun toString(): String {
        return "Achiev: $achievementScore, Target: $targetVertex, Debug: $debugMessage"
    }

    fun toJSONCommand(): JSONObject {
        val args = mutableMapOf("X" to targetVertex.X, "Y" to targetVertex.Y,"Debug" to debugMessage)
        if (eject) args["Eject"] = true
        if (split) args["Split"] = true
        return JSONObject(args)
    }
}