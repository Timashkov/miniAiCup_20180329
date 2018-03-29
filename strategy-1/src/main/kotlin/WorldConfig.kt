import org.json.JSONObject

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
}