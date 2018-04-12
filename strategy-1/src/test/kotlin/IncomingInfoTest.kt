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
        val tickData = JSONObject("{\"Mine\":[{\"R\":22.570751223900096,\"SX\":1.218869193301654E-16,\"SY\":-1.9905644536045466,\"X\":637.4292487760999,\"Y\":256.8126524489976,\"Id\":\"1\",\"M\":127.35970270279692}],\"Objects\":[{\"T\":\"F\",\"X\":582,\"Y\":221},{\"T\":\"F\",\"X\":631,\"Y\":156},{\"R\":17.343922722946456,\"T\":\"P\",\"X\":642.6560772770536,\"Y\":195.48282151412562,\"Id\":\"2.2\",\"M\":75.2029138548846},{\"R\":17.45885607419737,\"T\":\"P\",\"X\":642.5411439258027,\"Y\":229.95800620091788,\"Id\":\"2.1\",\"M\":76.2029138548846},{\"R\":22.719485527976467,\"T\":\"P\",\"X\":637.2805144720236,\"Y\":259.6739983956391,\"Id\":\"4\",\"M\":129.04375566398303},{\"T\":\"V\",\"X\":600,\"Y\":100,\"Id\":\"22\",\"M\":40},{\"T\":\"V\",\"X\":60,\"Y\":560,\"Id\":\"24\",\"M\":40},{\"T\":\"V\",\"X\":568,\"Y\":257,\"Id\":\"265\",\"M\":40},{\"T\":\"V\",\"X\":568,\"Y\":403,\"Id\":\"266\",\"M\":40},{\"T\":\"V\",\"X\":92,\"Y\":403,\"Id\":\"267\",\"M\":40}]}")

        val parsed = mProcessor.parseIncoming(tickData)
        mProcessor.analyzeData(parsed, 2)
        val point = parsed.mineInfo.getBestMovementPoint(null)
        assert(point != StepPoint.DEFAULT)
    }

}

