import data.ParseResult
import incominginfos.MineInfo
import incominginfos.WorldObjectsInfo
import org.json.JSONArray
import org.json.JSONObject


class IncomingInfoTest {

    lateinit var mMineState: MineInfo
    lateinit var mConfig: WorldConfig
    lateinit var mParseResult: ParseResult

    @org.junit.Before
    fun setUp() {
        val tickData = JSONObject("{\"Mine\":[{\"Id\":\"1.3\",\"M\":60,\"R\":15.491933384829668,\"SX\":-0.26401009407768095,\"SY\":-3.1708358766563576,\"TTF\":202,\"X\":635.91431100617137,\"Y\":284.19782241019578},{\"Id\":\"1.2\",\"M\":61,\"R\":15.620499351813308,\"SX\":-0.71904147560341336,\"SY\":-3.1105331334784512,\"TTF\":202,\"X\":626.40051935027691,\"Y\":118.00999058454401},{\"Id\":\"1.4\",\"M\":61,\"R\":15.620499351813308,\"SX\":1.247499247622373,\"SY\":-2.8945957505692892,\"TTF\":202,\"X\":552.75673223912236,\"Y\":246.7929481496387},{\"Id\":\"1.1\",\"M\":64,\"R\":16,\"SX\":0,\"SY\":0,\"TTF\":202,\"X\":620,\"Y\":91}],\"Objects\":[{\"T\":\"F\",\"X\":558,\"Y\":141},{\"T\":\"F\",\"X\":652,\"Y\":119},{\"T\":\"F\",\"X\":556,\"Y\":127},{\"T\":\"F\",\"X\":623,\"Y\":186},{\"T\":\"F\",\"X\":653,\"Y\":54},{\"T\":\"F\",\"X\":646,\"Y\":111},{\"T\":\"F\",\"X\":583,\"Y\":109},{\"T\":\"F\",\"X\":489,\"Y\":242},{\"T\":\"F\",\"X\":506,\"Y\":250},{\"T\":\"F\",\"X\":505,\"Y\":238},{\"T\":\"F\",\"X\":558,\"Y\":191},{\"T\":\"F\",\"X\":567,\"Y\":320},{\"T\":\"F\",\"X\":558,\"Y\":141},{\"T\":\"F\",\"X\":652,\"Y\":119},{\"T\":\"F\",\"X\":556,\"Y\":127},{\"T\":\"F\",\"X\":645,\"Y\":36},{\"T\":\"F\",\"X\":653,\"Y\":54},{\"T\":\"F\",\"X\":565,\"Y\":43},{\"T\":\"F\",\"X\":646,\"Y\":111},{\"T\":\"F\",\"X\":583,\"Y\":109},{\"Id\":\"20\",\"M\":40,\"T\":\"V\",\"X\":68,\"Y\":141},{\"Id\":\"21\",\"M\":40,\"T\":\"V\",\"X\":592,\"Y\":141},{\"Id\":\"22\",\"M\":40,\"T\":\"V\",\"X\":592,\"Y\":519},{\"Id\":\"23\",\"M\":40,\"T\":\"V\",\"X\":68,\"Y\":519},{\"Id\":\"264\",\"M\":40,\"T\":\"V\",\"X\":63,\"Y\":192},{\"Id\":\"266\",\"M\":40,\"T\":\"V\",\"X\":597,\"Y\":468},{\"Id\":\"267\",\"M\":40,\"T\":\"V\",\"X\":63,\"Y\":468}]}")


        val json = JSONObject("{\"FOOD_MASS\":1,\"GAME_HEIGHT\":660,\"GAME_TICKS\":75000,\"GAME_WIDTH\":660,\"INERTION_FACTOR\":10,\"MAX_FRAGS_CNT\":10,\"SPEED_FACTOR\":25,\"TICKS_TIL_FUSION\":250,\"VIRUS_RADIUS\":22,\"VIRUS_SPLIT_MASS\":80,\"VISCOSITY\":0.25}")
        mConfig = WorldConfig(json)


        mParseResult = ParseResult(MineInfo(tickData.getJSONArray("Mine"), mConfig), WorldObjectsInfo(tickData.getJSONArray("Objects"), mConfig))
        mMineState = mParseResult.mineInfo
    }

    @org.junit.After
    fun tearDown() {
    }

    @org.junit.Test
    fun configNonEmptyTest() {
        assert(mMineState.mFragmentsState.isNotEmpty())
    }

}