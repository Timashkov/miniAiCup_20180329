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
        val tickData = JSONObject("{\"Mine\":[{\"R\":12.96148139681572,\"SX\":-0.5422387602493859,\"SY\":0.0058212439094184314,\"TTF\":1072692767,\"X\":35.71991159952174,\"Y\":110.9922718358308,\"Id\":\"1\",\"M\":42}],\"Objects\":[{\"T\":\"F\",\"X\":16,\"Y\":73},{\"T\":\"V\",\"X\":198,\"Y\":283,\"Id\":\"21\",\"M\":40},{\"T\":\"V\",\"X\":462,\"Y\":283,\"Id\":\"22\",\"M\":40},{\"T\":\"V\",\"X\":462,\"Y\":377,\"Id\":\"23\",\"M\":40},{\"T\":\"V\",\"X\":198,\"Y\":377,\"Id\":\"24\",\"M\":40}]}")

        val parsed = mProcessor.parseIncoming(tickData)
        mProcessor.analyzeData(parsed, 2)
        val point = parsed.mineInfo.getBestMovementPoint()
        assert(point != Vertex(200f, 200f))
    }

}

