package strategy

import org.json.JSONObject
import org.junit.After
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test
import utils.Vertex
import WorldConfig
import utils.Logger

class FindFoodStrategyTest {
    val JSON = JSONObject("{\"FOOD_MASS\":1,\"GAME_HEIGHT\":660,\"GAME_TICKS\":75000,\"GAME_WIDTH\":660,\"INERTION_FACTOR\":10,\"MAX_FRAGS_CNT\":10,\"SPEED_FACTOR\":25,\"TICKS_TIL_FUSION\":250,\"VIRUS_RADIUS\":22,\"VIRUS_SPLIT_MASS\":80,\"VISCOSITY\":0.25}")
    val mLogger = Logger()
    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun testSearchBestWay(){
//        val food = ArrayList<Vertex>()
//        food.add(Vertex(1f,2f))
//        food.add(Vertex(2f,1f))
//        food.add(Vertex(3f,0f))
//        food.add(Vertex(3f,2f))
//        food.add(Vertex(1f,8f))
//        food.add(Vertex(3f,14f))
//        food.add(Vertex(9f,4f))
//        food.add(Vertex(10f,3f))
//        food.add(Vertex(10f,5f))
//        food.add(Vertex(7f,10f))
//
//        val gamer = Vertex(5f,8f)
//        val strategy = FindFoodStrategy(WorldConfig(JSON), mLogger)
//        strategy.findBestWay(food, gamer,1.5f)
    }

}