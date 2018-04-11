import data.StepPoint
import org.json.JSONObject
import utils.Logger


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
        val tickData = JSONObject("{\"Mine\":[{\"R\":24.526865565149517,\"SX\":-1.878009309442472E-6,\"SY\":2.2998980895776544E-22,\"X\":461.5853414244786,\"Y\":24.526865565149517,\"Id\":\"1\",\"M\":150.39178361272928}],\"Objects\":[{\"T\":\"F\",\"X\":514,\"Y\":14},{\"T\":\"F\",\"X\":513,\"Y\":97},{\"T\":\"F\",\"X\":370,\"Y\":72},{\"T\":\"F\",\"X\":430,\"Y\":104},{\"T\":\"F\",\"X\":478,\"Y\":54},{\"T\":\"F\",\"X\":468,\"Y\":95},{\"T\":\"F\",\"X\":401,\"Y\":61},{\"T\":\"F\",\"X\":402,\"Y\":38},{\"T\":\"F\",\"X\":417,\"Y\":47},{\"T\":\"F\",\"X\":400,\"Y\":16},{\"T\":\"F\",\"X\":392,\"Y\":89},{\"R\":19.166561386604375,\"T\":\"P\",\"X\":339.01734009826464,\"Y\":27.07176033825436,\"Id\":\"4.1\",\"M\":91.83926884661845},{\"T\":\"V\",\"X\":265,\"Y\":223,\"Id\":\"21\",\"M\":40},{\"T\":\"V\",\"X\":395,\"Y\":223,\"Id\":\"22\",\"M\":40},{\"T\":\"V\",\"X\":395,\"Y\":437,\"Id\":\"23\",\"M\":40},{\"T\":\"V\",\"X\":265,\"Y\":437,\"Id\":\"24\",\"M\":40},{\"T\":\"V\",\"X\":399,\"Y\":278,\"Id\":\"267\",\"M\":40},{\"T\":\"V\",\"X\":399,\"Y\":382,\"Id\":\"268\",\"M\":40},{\"T\":\"V\",\"X\":261,\"Y\":382,\"Id\":\"269\",\"M\":40},{\"T\":\"V\",\"X\":44,\"Y\":174,\"Id\":\"515\",\"M\":40},{\"T\":\"V\",\"X\":616,\"Y\":174,\"Id\":\"516\",\"M\":40},{\"T\":\"V\",\"X\":616,\"Y\":486,\"Id\":\"517\",\"M\":40},{\"T\":\"V\",\"X\":44,\"Y\":486,\"Id\":\"518\",\"M\":40}]}")

        val parsed = mProcessor.parseIncoming(tickData)
        mProcessor.analyzeData(parsed, 2)
        val point = parsed.mineInfo.getBestMovementPoint(null)
        assert(point != StepPoint.DEFAULT)
    }

}

