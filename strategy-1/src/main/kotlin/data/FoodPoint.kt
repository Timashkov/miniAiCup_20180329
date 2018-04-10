package data

import utils.Vertex

data class FoodPoint(val target: Vertex, val foodPoints: List<Vertex>, val fragmentId: String){
    companion object {
        val DEFAULT = FoodPoint(Vertex.DEFAULT, ArrayList(), "")
    }
}