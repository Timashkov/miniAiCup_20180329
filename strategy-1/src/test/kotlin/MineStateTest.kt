import org.json.JSONArray

class MineStateTest {

    lateinit var mMineState: MineState

    @org.junit.Before
    fun setUp() {
        val jsarr = JSONArray("[{\"Id\":\"1\",\"M\":40,\"R\":12.649110640673518,\"SX\":-0.92513099579326519,\"SY\":-0.34741206171139455,\"TTF\":31,\"X\":473.07486900420673,\"Y\":177.6525879382886}]")
        mMineState = MineState(jsarr)
    }

    @org.junit.After
    fun tearDown() {
    }

    @org.junit.Test
    fun configNonEmptyTest() {
        assert(mMineState.mFragmentsState.isNotEmpty())
    }

}