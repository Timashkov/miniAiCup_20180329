package data

import utils.Vertex

data class FoodPoint(val target: Vertex, val foodPoints: List<Vertex>, val fragmentId: String, var useEjections: Boolean = false){
    companion object {
        val DEFAULT = FoodPoint(Vertex.DEFAULT, ArrayList(), "")
    }
}