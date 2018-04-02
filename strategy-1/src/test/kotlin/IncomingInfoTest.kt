import incominginfos.MineInfo
import org.json.JSONArray
import org.json.JSONObject


class IncomingInfoTest {

    lateinit var mMineState: MineInfo
    lateinit var mConfig: WorldConfig

    @org.junit.Before
    fun setUp() {
        val json = JSONObject("{\"FOOD_MASS\":1,\"GAME_HEIGHT\":660,\"GAME_TICKS\":75000,\"GAME_WIDTH\":660,\"INERTION_FACTOR\":10,\"MAX_FRAGS_CNT\":10,\"SPEED_FACTOR\":25,\"TICKS_TIL_FUSION\":250,\"VIRUS_RADIUS\":22,\"VIRUS_SPLIT_MASS\":80,\"VISCOSITY\":0.25}")
        mConfig = WorldConfig(json)
        val jsarr = JSONArray("[{\"Id\":\"1\",\"M\":40,\"R\":12.649110640673518,\"SX\":-0.92513099579326519,\"SY\":-0.34741206171139455,\"TTF\":31,\"X\":473.07486900420673,\"Y\":177.6525879382886}]")
        mMineState = MineInfo(jsarr, mConfig)
    }

    @org.junit.After
    fun tearDown() {
    }

    @org.junit.Test
    fun configNonEmptyTest() {
        assert(mMineState.mFragmentsState.isNotEmpty())
    }

}