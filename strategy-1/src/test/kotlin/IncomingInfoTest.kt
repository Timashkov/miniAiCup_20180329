import data.FoodPoint
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
        val tickData = JSONObject("{\"Mine\":[{\"R\":19.421243874704498,\"SX\":0.05084647360759604,\"SY\":-2.3195507627690186,\"TTF\":80,\"X\":139.20806753097256,\"Y\":604.817401368203,\"Id\":\"1\",\"M\":94.29617841018674}],\"Objects\":[{\"T\":\"F\",\"X\":117,\"Y\":537},{\"T\":\"F\",\"X\":75,\"Y\":612},{\"T\":\"F\",\"X\":159,\"Y\":639},{\"T\":\"F\",\"X\":118,\"Y\":518},{\"T\":\"F\",\"X\":181,\"Y\":615},{\"T\":\"F\",\"X\":115,\"Y\":542},{\"T\":\"F\",\"X\":211,\"Y\":609},{\"R\":36.24014280031913,\"T\":\"P\",\"X\":143.7510634664611,\"Y\":569.8588243958936,\"Id\":\"3\",\"M\":328.33698754688055},{\"T\":\"V\",\"X\":455,\"Y\":124,\"Id\":\"22\",\"M\":40},{\"T\":\"V\",\"X\":455,\"Y\":536,\"Id\":\"23\",\"M\":40},{\"T\":\"V\",\"X\":205,\"Y\":536,\"Id\":\"24\",\"M\":40},{\"T\":\"V\",\"X\":265,\"Y\":270,\"Id\":\"265\",\"M\":40},{\"T\":\"V\",\"X\":395,\"Y\":270,\"Id\":\"266\",\"M\":40},{\"T\":\"V\",\"X\":395,\"Y\":390,\"Id\":\"267\",\"M\":40},{\"T\":\"V\",\"X\":265,\"Y\":390,\"Id\":\"268\",\"M\":40},{\"T\":\"V\",\"X\":49,\"Y\":154,\"Id\":\"509\",\"M\":40},{\"T\":\"V\",\"X\":611,\"Y\":154,\"Id\":\"510\",\"M\":40},{\"T\":\"V\",\"X\":611,\"Y\":506,\"Id\":\"511\",\"M\":40},{\"T\":\"V\",\"X\":49,\"Y\":506,\"Id\":\"512\",\"M\":40}]}")

        val parsed = mProcessor.parseIncoming(tickData)
        mProcessor.analyzeData(parsed, 2)
        val point = parsed.mineInfo.getBestMovementPoint(null)
        assert(point != FoodPoint.DEFAULT)
    }

}

