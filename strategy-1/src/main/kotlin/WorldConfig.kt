import org.json.JSONObject
import utils.Vertex

class WorldConfig(configJson: JSONObject) {
    val GameWidth: Int = configJson.getInt("GAME_WIDTH")    //
    val GameHeight: Int = configJson.getInt("GAME_HEIGHT")   //
    val GameTicks: Int = configJson.getInt("GAME_TICKS")    // (7500/25000/40000)
    val FoodMass: Float = configJson.getFloat("FOOD_MASS")  //  [1.0 : 4.0]
    val MaxFragsCnt: Int = configJson.getInt("MAX_FRAGS_CNT")    // [4:16]
    val TicksTillFusion: Int = configJson.getInt("TICKS_TIL_FUSION")    // [150:500]
    val VirusRadius: Float = configJson.getFloat("VIRUS_RADIUS")    // [15.0 : 40.0]
    val VirusSplitMass: Float = configJson.getFloat("VIRUS_SPLIT_MASS")   // [50.0: 100.0]
    val Viscosity: Float = configJson.getFloat("VISCOSITY")    // [0.05: 0.5]
    val InertionFactor: Float = configJson.getFloat("INERTION_FACTOR")  // [1.0 : 20.0]
    val SpeedFactor: Float = configJson.getFloat("SPEED_FACTOR") // [25.0 : 100.0 ]

    fun getDimensions(): Vertex = Vertex(GameWidth.toFloat(), GameHeight.toFloat())
    fun getCenter(): Vertex = Vertex(GameWidth * 0.5f, GameHeight * 0.5f)

    val ltCorner: Vertex
        get() = Vertex(0f, 0f)
    val lbCorner: Vertex
        get() = Vertex(0f, GameHeight.toFloat())
    val rtCorner: Vertex
        get() = Vertex(GameWidth.toFloat(), 0f)
    val rbCorner: Vertex
        get() = Vertex(GameWidth.toFloat(), GameHeight.toFloat())

    companion object {
        val EAT_MASS_FACTOR = 1.2f
        val MIN_SPLITABLE_MASS = 120f
        val MAGIC_MASS4EAT = 3
        val MAGIC_COMPASS_BLACK_DELTA = 4
        val FOW_RADIUS_FACTOR = 4
        val STAR_BURST_DISABLE_TICK = 1200
    }
}