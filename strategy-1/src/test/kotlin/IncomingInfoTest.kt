import org.json.JSONObject
import utils.Logger
import utils.Vertex


class IncomingInfoTest {

    lateinit var mConfig: WorldConfig
    lateinit var mProcessor: Processor
    val mLogger = Logger()

    @org.junit.Before
    fun setUp() {


        val json = JSONObject("{\"FOOD_MASS\":1,\"GAME_HEIGHT\":990,\"GAME_TICKS\":75000,\"GAME_WIDTH\":990,\"INERTION_FACTOR\":10,\"MAX_FRAGS_CNT\":10,\"SPEED_FACTOR\":25,\"TICKS_TIL_FUSION\":250,\"VIRUS_RADIUS\":22,\"VIRUS_SPLIT_MASS\":80,\"VISCOSITY\":0.25}")
        mConfig = WorldConfig(json)
        mProcessor = Processor(json)

    }

    @org.junit.After
    fun tearDown() {
    }

    @org.junit.Test
    fun testProcessor() {
        val tickData = JSONObject("{\"Mine\":[{\"R\":14.696938456699069,\"SX\":1.7194716402941346,\"SY\":-1.095888872607429,\"TTF\":3145431,\"X\":256.0830022718715,\"Y\":87.44331104938362,\"Id\":\"1\",\"M\":54}],\"Objects\":[{\"R\":13.856406460551018,\"T\":\"P\",\"X\":272.2423547812306,\"Y\":103.52145030851474,\"Id\":\"3\",\"M\":48},{\"T\":\"V\",\"X\":82,\"Y\":74,\"Id\":\"21\",\"M\":40},{\"T\":\"V\",\"X\":578,\"Y\":74,\"Id\":\"22\",\"M\":40},{\"T\":\"V\",\"X\":578,\"Y\":586,\"Id\":\"23\",\"M\":40},{\"T\":\"V\",\"X\":82,\"Y\":586,\"Id\":\"24\",\"M\":40}]}")

        val parsed = mProcessor.parseIncoming(tickData)
        mProcessor.analyzeData(parsed, 2)
        val point = parsed.mineInfo.getBestMovementPoint()
        assert(point != Vertex(200f, 200f))
    }

}

