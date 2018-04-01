package strategy

import utils.Vertex

data class StrategyResult(val achievementScore: Float, val targetVertex: Vertex, val debugMessage: String = ""){
    override fun toString(): String {
        return "Achiev: $achievementScore, Target: $targetVertex, Debug: $debugMessage"
    }
}