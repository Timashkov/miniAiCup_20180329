package data

import utils.Vertex

data class StepPoint(var target: Vertex, var movementTarget: Vertex, val foodPoints: List<Vertex>, val fragmentId: String, var useEjections: Boolean = false, var useSplit: Boolean = false) {
    companion object {
        val DEFAULT = StepPoint(Vertex.DEFAULT, Vertex.DEFAULT, ArrayList(), "")
    }
}