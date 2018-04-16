import data.StepPoint
import org.json.JSONObject
import utils.Logger


class IncomingInfoTest {

    lateinit var mConfig: WorldConfig
    lateinit var mProcessor: Processor
    val mLogger = Logger()

    @org.junit.Before
    fun setUp() {


        val json = JSONObject(
                "{\"FOOD_MASS\":1.7677835396083377,\"GAME_HEIGHT\":990,\"GAME_TICKS\":7500,\"GAME_WIDTH\":990,\"INERTION_FACTOR\":19.281551705749358,\"MAX_FRAGS_CNT\":12,\"SPEED_FACTOR\":49.66617623679462,\"TICKS_TIL_FUSION\":424,\"VIRUS_RADIUS\":33.647758329045558,\"VIRUS_SPLIT_MASS\":67.649930556499285,\"VISCOSITY\":0.21262555199043848}"
        )
        mConfig = WorldConfig(json)
        mProcessor = Processor(json)

    }

    @org.junit.After
    fun tearDown() {
    }

    @org.junit.Test
    fun testProcessor() {

        var tickData = JSONObject(
                "{\"Mine\":[{\"R\":10.198039027185569,\"SX\":0.5383674098304833,\"SY\":0,\"X\":496.1566583056965,\"Y\":10.198039027185569,\"Id\":\"1\",\"M\":26}],\"Objects\":[{\"T\":\"E\",\"X\":542.8017353366283,\"Y\":4.076975562437266,\"pId\":1,\"Id\":\"414\"},{\"T\":\"E\",\"X\":528.6703180049285,\"Y\":4,\"pId\":1,\"Id\":\"415\"},{\"R\":19.14283953268935,\"T\":\"P\",\"X\":558.2982432300284,\"Y\":19.14283953268935,\"Id\":\"2.1\",\"M\":91.61207634357355},{\"R\":20.72422604872405,\"T\":\"P\",\"X\":479.7538063247655,\"Y\":20.72422604872405,\"Id\":\"4\",\"M\":107.37338632965312},{\"T\":\"V\",\"X\":187,\"Y\":64,\"Id\":\"21\",\"M\":40},{\"T\":\"V\",\"X\":473,\"Y\":596,\"Id\":\"23\",\"M\":40},{\"T\":\"V\",\"X\":187,\"Y\":596,\"Id\":\"24\",\"M\":40},{\"T\":\"V\",\"X\":129,\"Y\":228,\"Id\":\"265\",\"M\":40},{\"T\":\"V\",\"X\":531,\"Y\":432,\"Id\":\"267\",\"M\":40},{\"T\":\"V\",\"X\":129,\"Y\":432,\"Id\":\"268\",\"M\":40}]}"
        )
        var parsed = mProcessor.parseIncoming(tickData)
        mProcessor.analyzeData(parsed, 7588)


//        tickData = JSONObject(
//                "{\"Mine\":[{\"R\":20.99681521105083,\"SX\":-2.304556602866525,\"SY\":-0.4531364079414227,\"TTF\":1069755105,\"X\":22.324004950615926,\"Y\":144.78444013454558,\"Id\":\"1\",\"M\":110.21656225175388}],\"Objects\":[{\"R\":23.732100031610287,\"T\":\"P\",\"X\":104.74932887514845,\"Y\":154.88353625117745,\"Id\":\"4\",\"M\":140.80314297758926},{\"T\":\"V\",\"X\":527,\"Y\":199,\"Id\":\"22\",\"M\":40},{\"T\":\"V\",\"X\":527,\"Y\":461,\"Id\":\"23\",\"M\":40},{\"T\":\"V\",\"X\":133,\"Y\":461,\"Id\":\"24\",\"M\":40},{\"T\":\"V\",\"X\":241,\"Y\":184,\"Id\":\"265\",\"M\":40},{\"T\":\"V\",\"X\":419,\"Y\":184,\"Id\":\"266\",\"M\":40},{\"T\":\"V\",\"X\":419,\"Y\":476,\"Id\":\"267\",\"M\":40}]}"
//        )
//        parsed = mProcessor.parseIncoming(tickData)
//        mProcessor.analyzeData(parsed, 7589)
    }

}

