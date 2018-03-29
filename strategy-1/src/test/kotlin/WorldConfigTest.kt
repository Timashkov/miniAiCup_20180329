import org.json.JSONObject

class WorldConfigTest {

    lateinit var mConfig: WorldConfig

    @org.junit.Before
    fun setUp() {
        val json = JSONObject("{\"FOOD_MASS\":1,\"GAME_HEIGHT\":660,\"GAME_TICKS\":75000,\"GAME_WIDTH\":660,\"INERTION_FACTOR\":10,\"MAX_FRAGS_CNT\":10,\"SPEED_FACTOR\":25,\"TICKS_TIL_FUSION\":250,\"VIRUS_RADIUS\":22,\"VIRUS_SPLIT_MASS\":80,\"VISCOSITY\":0.25}")
        mConfig = WorldConfig(json)
    }

    @org.junit.After
    fun tearDown() {
    }

    @org.junit.Test
    fun getGameWidth() {
        assert(mConfig.FoodMass > 0)
    }
}