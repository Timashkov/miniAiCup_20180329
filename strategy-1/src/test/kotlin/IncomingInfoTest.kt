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
                "{\"GAME_HEIGHT\":660,\"INERTION_FACTOR\":10,\"FOOD_MASS\":1,\"VIRUS_RADIUS\":22,\"SPEED_FACTOR\":25,\"MAX_FRAGS_CNT\":10,\"TICKS_TIL_FUSION\":250,\"GAME_WIDTH\":660,\"VIRUS_SPLIT_MASS\":80,\"VISCOSITY\":0.25,\"GAME_TICKS\":75000}\n"
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
                "{\"Mine\":[{\"R\":11.135528725660043,\"SX\":8.124982975094903E-17,\"SY\":-1.3269104170691226,\"X\":648.86447127434,\"Y\":373.7352479989854,\"Id\":\"1\",\"M\":31}],\"Objects\":[{\"T\":\"F\",\"X\":627,\"Y\":348},{\"R\":19.28730152198591,\"T\":\"P\",\"X\":633.1506258335062,\"Y\":381.38234900592346,\"Id\":\"2\",\"M\":93},{\"T\":\"V\",\"X\":265,\"Y\":200,\"Id\":\"21\",\"M\":40},{\"T\":\"V\",\"X\":395,\"Y\":200,\"Id\":\"22\",\"M\":40},{\"T\":\"V\",\"X\":395,\"Y\":460,\"Id\":\"23\",\"M\":40},{\"T\":\"V\",\"X\":265,\"Y\":460,\"Id\":\"24\",\"M\":40}]}"
        )
        var parsed = mProcessor.parseIncoming(tickData)
        mProcessor.analyzeData(parsed, 7588)


//        tickData = JSONObject(
//                "{\"Mine\":[{\"R\":13.416407864998739,\"SX\":1.454795282631957E-17,\"SY\":0.23758609970561995,\"X\":644.5080666151704,\"Y\":221.59565925473328,\"Id\":\"1\",\"M\":45}],\"Objects\":[{\"T\":\"F\",\"X\":640,\"Y\":236},{\"T\":\"F\",\"X\":647,\"Y\":273},{\"T\":\"E\",\"X\":644.5080666151704,\"Y\":238.08759263956296,\"pId\":1,\"Id\":\"209\"},{\"R\":17.204650534085253,\"T\":\"P\",\"X\":627.8619770549781,\"Y\":222.6914438773055,\"Id\":\"4\",\"M\":74},{\"R\":17.320508075688775,\"T\":\"P\",\"X\":619.03985753468,\"Y\":226.4131707749518,\"Id\":\"2\",\"M\":75},{\"T\":\"V\",\"X\":92,\"Y\":145,\"Id\":\"21\",\"M\":40},{\"T\":\"V\",\"X\":568,\"Y\":145,\"Id\":\"22\",\"M\":40},{\"T\":\"V\",\"X\":568,\"Y\":515,\"Id\":\"23\",\"M\":40},{\"T\":\"V\",\"X\":92,\"Y\":515,\"Id\":\"24\",\"M\":40}]}"
//        )
//        parsed = mProcessor.parseIncoming(tickData)
//        mProcessor.analyzeData(parsed, 7589)
    }

}

