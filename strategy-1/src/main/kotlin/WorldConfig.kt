import org.json.JSONObject

class WorldConfig(configJson: JSONObject) {
    val GameWidth: Int = configJson.getInt("GAME_WIDTH")
    val GameHeight: Int = configJson.getInt("GAME_HEIGHT")
    val GameTicks: Int = configJson.getInt("GAME_TICKS")
    val FoodMass: Float = configJson.getFloat("FOOD_MASS")
    val MaxFragsCnt: Int = configJson.getInt("MAX_FRAGS_CNT")
    val TicksTillFusion: Int = configJson.getInt("TICKS_TIL_FUSION")
    val VirusRadius: Float = configJson.getFloat("VIRUS_RADIUS")
    val VirusSplitMass: Float = configJson.getFloat("VIRUS_SPLIT_MASS")
    val Viscosity: Float = configJson.getFloat("VISCOSITY")
    val InertionFactor: Float = configJson.getFloat("INERTION_FACTOR")
    val SpeedFactor: Float = configJson.getFloat("SPEED_FACTOR")
}