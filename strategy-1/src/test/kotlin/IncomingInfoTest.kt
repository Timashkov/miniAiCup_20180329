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
        val tickData = JSONObject("{\"Mine\":[{\"Id\":\"4\",\"M\":108.25458262314606,\"R\":20.809092495651612,\"SX\":0.25096509084019769,\"SY\":0,\"X\":731.56809372164776,\"Y\":20.809092495651612}],\"Objects\":[{\"T\":\"F\",\"X\":713,\"Y\":31},{\"T\":\"F\",\"X\":702,\"Y\":79},{\"Id\":\"1\",\"M\":131.48525575463165,\"R\":22.933404087019586,\"T\":\"P\",\"X\":745.30258465916279,\"Y\":75.714466054599754},{\"Id\":\"21\",\"M\":40,\"T\":\"V\",\"X\":350.10925980349384,\"Y\":371.10925980349384},{\"Id\":\"22\",\"M\":40,\"T\":\"V\",\"X\":639.89074019650616,\"Y\":371.10925980349384},{\"Id\":\"24\",\"M\":40,\"T\":\"V\",\"X\":350.10925980349384,\"Y\":618.89074019650616},{\"Id\":\"265\",\"M\":40,\"T\":\"V\",\"X\":46.109259803493821,\"Y\":92.109259803493813},{\"Id\":\"266\",\"M\":40,\"T\":\"V\",\"X\":943.89074019650616,\"Y\":92.109259803493813},{\"Id\":\"267\",\"M\":40,\"T\":\"V\",\"X\":943.89074019650616,\"Y\":897.89074019650616},{\"Id\":\"268\",\"M\":40,\"T\":\"V\",\"X\":46.109259803493821,\"Y\":897.89074019650616}]}")

        val parsed = mProcessor.parseIncoming(tickData)
        mProcessor.analyzeData(parsed, 2)
        val point = parsed.mineInfo.getBestEscapePoint()
        assert(point != Vertex(200f, 200f))
    }

}

