package utils
import WorldConfig
import data.ParseResult

import incominginfos.MineInfo
import incominginfos.WorldObjectsInfo
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test

import org.junit.Assert.*
import strategy.DefaultTurnStrategy

class GameEngineTest {
    lateinit var parseResult: ParseResult
    lateinit var mConfig: WorldConfig
    val mLogger = Logger()

    lateinit var mDefaultStrategy: DefaultTurnStrategy
    @org.junit.Before
    fun setUp() {

        val json = JSONObject("{\"FOOD_MASS\":1,\"GAME_HEIGHT\":660,\"GAME_TICKS\":75000,\"GAME_WIDTH\":660,\"INERTION_FACTOR\":1,\"MAX_FRAGS_CNT\":10,\"SPEED_FACTOR\":100,\"TICKS_TIL_FUSION\":250,\"VIRUS_RADIUS\":40,\"VIRUS_SPLIT_MASS\":80,\"VISCOSITY\":0.25}")
        mConfig = WorldConfig(json)
        val jsarr = JSONArray("[{\"R\":12.649110640673518,\"SX\":0,\"SY\":0,\"X\":482.7,\"Y\":278.3,\"Id\":\"1\",\"M\":40}]")
        parseResult = ParseResult(MineInfo(jsarr, mConfig), WorldObjectsInfo(JSONArray("[{\"T\":\"V\",\"X\":113,\"Y\":161,\"Id\":\"20\",\"M\":40},{\"T\":\"V\",\"X\":547,\"Y\":161,\"Id\":\"21\",\"M\":40},{\"T\":\"V\",\"X\":547,\"Y\":499,\"Id\":\"22\",\"M\":40},{\"T\":\"V\",\"X\":113,\"Y\":499,\"Id\":\"23\",\"M\":40}]"), mConfig))
        mDefaultStrategy = DefaultTurnStrategy(mConfig, mLogger)
    }



//    INCOMING {"Mine":[{"R":12.649110640673518,"SX":0,"SY":0,"X":482.7,"Y":278.3,"Id":"1","M":40}],"Objects":[{"T":"V","X":113,"Y":161,"Id":"20","M":40},{"T":"V","X":547,"Y":161,"Id":"21","M":40},{"T":"V","X":547,"Y":499,"Id":"22","M":40},{"T":"V","X":113,"Y":499,"Id":"23","M":40}]}
//
//    Start check strategies
//    Chosen strategy: Achiev: 0, Target: Vertex: 305.10645 : -217.03915, Debug: DEFAULT: Go TO Vertex: 412.5 : 82.5
//

//    INCOMING {"Mine":[{"R":12.649110640673518,"SX":-0.2126391169672375,"SY":-0.3332185558089368,"X":482.48736088303275,"Y":277.9667814441911,"Id":"1","M":40}],"Objects":[{"T":"V","X":113,"Y":161,"Id":"20","M":40},{"T":"V","X":547,"Y":161,"Id":"21","M":40},{"T":"V","X":547,"Y":499,"Id":"22","M":40},{"T":"V","X":113,"Y":499,"Id":"23","M":40}]}
//
//    Start check strategies
//    Chosen strategy: Achiev: 0, Target: Vertex: 305.95627 : -215.70746, Debug: DEFAULT: Go TO Vertex: 412.5 : 82.5
//
//    INCOMING {"Mine":[{"R":12.649110640673518,"SX":-0.4192368111403616,"SY":-0.6585684712863039,"X":482.0681240718924,"Y":277.3082129729048,"Id":"1","M":40}],"Objects":[{"T":"V","X":113,"Y":161,"Id":"20","M":40},{"T":"V","X":547,"Y":161,"Id":"21","M":40},{"T":"V","X":547,"Y":499,"Id":"22","M":40},{"T":"V","X":113,"Y":499,"Id":"23","M":40}]}
//
//    Start check strategies
//    Chosen strategy: Achiev: 0, Target: Vertex: 307.10724 : -213.89755, Debug: DEFAULT: Go TO Vertex: 412.5 : 82.5
//
//    INCOMING {"Mine":[{"R":12.649110640673518,"SX":-0.6196790230098348,"SY":-0.9764116514728025,"X":481.44844504888255,"Y":276.331801321432,"Id":"1","M":40}],"Objects":[{"T":"V","X":113,"Y":161,"Id":"20","M":40},{"T":"V","X":547,"Y":161,"Id":"21","M":40},{"T":"V","X":547,"Y":499,"Id":"22","M":40},{"T":"V","X":113,"Y":499,"Id":"23","M":40}]}
//
//    Start check strategies
//    Chosen strategy: Achiev: 0, Target: Vertex: 308.54962 : -211.61978, Debug: DEFAULT: Go TO Vertex: 412.5 : 82.5
//
//    INCOMING {"Mine":[{"R":12.649110640673518,"SX":-0.8138544459046877,"SY":-1.2870977373952577,"X":480.6345906029779,"Y":275.04470358403677,"Id":"1","M":40}],"Objects":[{"T":"V","X":113,"Y":161,"Id":"20","M":40},{"T":"V","X":547,"Y":161,"Id":"21","M":40},{"T":"V","X":547,"Y":499,"Id":"22","M":40},{"T":"V","X":113,"Y":499,"Id":"23","M":40}]}
//
//    Start check strategies
//    Chosen strategy: Achiev: 0, Target: Vertex: 310.27365 : -208.88446, Debug: DEFAULT: Go TO Vertex: 412.5 : 82.5
//
//    INCOMING {"Mine":[{"R":12.649110640673518,"SX":-1.0016520722274045,"SY":-1.590965053598061,"X":479.6329385307505,"Y":273.4537385304387,"Id":"1","M":40}],"Objects":[{"T":"V","X":113,"Y":161,"Id":"20","M":40},{"T":"V","X":547,"Y":161,"Id":"21","M":40},{"T":"V","X":547,"Y":499,"Id":"22","M":40},{"T":"V","X":113,"Y":499,"Id":"23","M":40}]}
//
//    Start check strategies
//    Chosen strategy: Achiev: 0, Target: Vertex: 312.2692 : -205.70105, Debug: DEFAULT: Go TO Vertex: 412.5 : 82.5
//
//    INCOMING {"Mine":[{"R":12.649110640673518,"SX":-1.1829591610263912,"SY":-1.888341252328191,"X":478.4499793697241,"Y":271.56539727811054,"Id":"1","M":40}],"Objects":[{"T":"V","X":113,"Y":161,"Id":"20","M":40},{"T":"V","X":547,"Y":161,"Id":"21","M":40},{"T":"V","X":547,"Y":499,"Id":"22","M":40},{"T":"V","X":113,"Y":499,"Id":"23","M":40}]}
//
//    Start check strategies
//    Chosen strategy: Achiev: 0, Target: Vertex: 314.52612 : -202.0787, Debug: DEFAULT: Go TO Vertex: 412.5 : 82.5
//
//    INCOMING {"Mine":[{"R":12.649110640673518,"SX":-1.3576589612572934,"SY":-2.1795440433900297,"X":477.0923204084668,"Y":269.3858532347205,"Id":"1","M":40}],"Objects":[{"T":"V","X":113,"Y":161,"Id":"20","M":40},{"T":"V","X":547,"Y":161,"Id":"21","M":40},{"T":"V","X":547,"Y":499,"Id":"22","M":40},{"T":"V","X":113,"Y":499,"Id":"23","M":40}]}
//
//    Start check strategies
//    Chosen strategy: Achiev: 0, Target: Vertex: 317.034 : -198.02612, Debug: DEFAULT: Go TO Vertex: 412.5 : 82.5
//
//    INCOMING {"Mine":[{"R":12.649110640673518,"SX":-1.5256286731899718,"SY":-2.464881681702072,"X":475.5666917352768,"Y":266.92097155301843,"Id":"1","M":40}],"Objects":[{"T":"V","X":113,"Y":161,"Id":"20","M":40},{"T":"V","X":547,"Y":161,"Id":"21","M":40},{"T":"V","X":547,"Y":499,"Id":"22","M":40},{"T":"V","X":113,"Y":499,"Id":"23","M":40}]}
//
//    Start check strategies
//    Chosen strategy: Achiev: 0, Target: Vertex: 319.7823 : -193.55145, Debug: DEFAULT: Go TO Vertex: 412.5 : 82.5
//
//    INCOMING {"Mine":[{"R":12.649110640673518,"SX":-1.686737274256687,"SY":-2.7446534142333894,"X":473.8799544610201,"Y":264.17631813878506,"Id":"1","M":40}],"Objects":[{"T":"V","X":113,"Y":161,"Id":"20","M":40},{"T":"V","X":547,"Y":161,"Id":"21","M":40},{"T":"V","X":547,"Y":499,"Id":"22","M":40},{"T":"V","X":113,"Y":499,"Id":"23","M":40}]}
//
//    Start check strategies
//    Chosen strategy: Achiev: 0, Target: Vertex: 322.76007 : -188.66254, Debug: DEFAULT: Go TO Vertex: 412.5 : 82.5
//
//    INCOMING {"Mine":[{"R":12.649110640673518,"SX":-1.8408435588443643,"SY":-3.019149648867041,"X":472.0391109021757,"Y":261.15716848991804,"Id":"1","M":40}],"Objects":[{"T":"V","X":113,"Y":161,"Id":"20","M":40},{"T":"V","X":547,"Y":161,"Id":"21","M":40},{"T":"V","X":547,"Y":499,"Id":"22","M":40},{"T":"V","X":113,"Y":499,"Id":"23","M":40}]}
//
//    Start check strategies
//    Chosen strategy: Achiev: 0, Target: Vertex: 325.9563 : -183.36673, Debug: DEFAULT: Go TO Vertex: 412.5 : 82.5
//
//    INCOMING {"Mine":[{"R":12.649110640673518,"SX":-1.9877937566787662,"SY":-3.2886521860774347,"X":470.05131714549697,"Y":257.8685163038406,"Id":"1","M":40}],"Objects":[{"T":"V","X":113,"Y":161,"Id":"20","M":40},{"T":"V","X":547,"Y":161,"Id":"21","M":40},{"T":"V","X":547,"Y":499,"Id":"22","M":40},{"T":"V","X":113,"Y":499,"Id":"23","M":40}]}
//
//    Start check strategies
//    Chosen strategy: Achiev: 0, Target: Vertex: 329.35974 : -177.67096, Debug: DEFAULT: Go TO Vertex: 412.5 : 82.5
//
//    INCOMING {"Mine":[{"R":12.649110640673518,"SX":-2.1274191948254786,"SY":-3.5534341966942153,"X":467.92389795067146,"Y":254.31508210714637,"Id":"1","M":40}],"Objects":[{"T":"V","X":113,"Y":161,"Id":"20","M":40},{"T":"V","X":547,"Y":161,"Id":"21","M":40},{"T":"V","X":547,"Y":499,"Id":"22","M":40},{"T":"V","X":113,"Y":499,"Id":"23","M":40}]}
//
//    Start check strategies
//    Chosen strategy: Achiev: 0, Target: Vertex: 332.95874 : -171.58168, Debug: DEFAULT: Go TO Vertex: 412.5 : 82.5
//
//    INCOMING {"Mine":[{"R":12.649110640673518,"SX":-2.2595339684082494,"SY":-3.8137599319755857,"X":465.6643639822632,"Y":250.50132217517077,"Id":"1","M":40}],"Objects":[{"T":"V","X":113,"Y":161,"Id":"20","M":40},{"T":"V","X":547,"Y":161,"Id":"21","M":40},{"T":"V","X":547,"Y":499,"Id":"22","M":40},{"T":"V","X":113,"Y":499,"Id":"23","M":40}]}
//
//    Start check strategies
//    Chosen strategy: Achiev: 0, Target: Vertex: 336.74127 : -165.10521, Debug: DEFAULT: Go TO Vertex: 412.5 : 82.5
//
//    INCOMING {"Mine":[{"R":12.649110640673518,"SX":-2.3839324130441475,"SY":-4.069884236389367,"X":463.28043156921905,"Y":246.43143793878141,"Id":"1","M":40}],"Objects":[{"T":"V","X":113,"Y":161,"Id":"20","M":40},{"T":"V","X":547,"Y":161,"Id":"21","M":40},{"T":"V","X":547,"Y":499,"Id":"22","M":40},{"T":"V","X":113,"Y":499,"Id":"23","M":40}]}
//
//    Start check strategies
//    Chosen strategy: Achiev: 0, Target: Vertex: 340.69513 : -158.24727, Debug: DEFAULT: Go TO Vertex: 412.5 : 82.5
//
//    INCOMING {"Mine":[{"R":12.649110640673518,"SX":-2.500386123206663,"SY":-4.322051931077001,"X":460.78004544601237,"Y":242.10938600770442,"Id":"1","M":40}],"Objects":[{"T":"V","X":113,"Y":161,"Id":"20","M":40},{"T":"V","X":547,"Y":161,"Id":"21","M":40},{"T":"V","X":547,"Y":499,"Id":"22","M":40},{"T":"V","X":113,"Y":499,"Id":"23","M":40}]}
//
//    Start check strategies
//    Chosen strategy: Achiev: 0, Target: Vertex: 344.8075 : -151.01332, Debug: DEFAULT: Go TO Vertex: 412.5 : 82.5
//
//    INCOMING {"Mine":[{"R":12.649110640673518,"SX":-2.6086412205813105,"SY":-4.570496635585668,"X":458.17140422543105,"Y":237.53888937211875,"Id":"1","M":40}],"Objects":[{"T":"V","X":113,"Y":161,"Id":"20","M":40},{"T":"V","X":547,"Y":161,"Id":"21","M":40},{"T":"V","X":547,"Y":499,"Id":"22","M":40},{"T":"V","X":113,"Y":499,"Id":"23","M":40}]}
//
//    Start check strategies
//    Chosen strategy: Achiev: 0, Target: Vertex: 349.06528 : -143.40843, Debug: DEFAULT: Go TO Vertex: 412.5 : 82.5
//
//    INCOMING {"Mine":[{"R":12.649110640673518,"SX":-2.708414961677991,"SY":-4.815439419078886,"X":455.46298926375306,"Y":232.72344995303985,"Id":"1","M":40}],"Objects":[{"T":"V","X":113,"Y":161,"Id":"20","M":40},{"T":"V","X":547,"Y":161,"Id":"21","M":40},{"T":"V","X":547,"Y":499,"Id":"22","M":40},{"T":"V","X":113,"Y":499,"Id":"23","M":40}]}
//
//    Start check strategies
//    Chosen strategy: Achiev: 0, Target: Vertex: 353.4548 : -135.4375, Debug: DEFAULT: Go TO Vertex: 412.5 : 82.5
//
//    INCOMING {"Mine":[{"R":12.649110640673518,"SX":-2.799392384836154,"SY":-5.057086834022061,"X":452.6635968789169,"Y":227.66636311901777,"Id":"1","M":40}],"Objects":[{"T":"V","X":113,"Y":161,"Id":"20","M":40},{"T":"V","X":547,"Y":161,"Id":"21","M":40},{"T":"V","X":547,"Y":499,"Id":"22","M":40},{"T":"V","X":113,"Y":499,"Id":"23","M":40}]}
//
//    Start check strategies
//    Chosen strategy: Achiev: 0, Target: Vertex: 357.96173 : -127.10503, Debug: DEFAULT: Go TO Vertex: 412.5 : 82.5
//
//    INCOMING {"Mine":[{"R":12.649110640673518,"SX":-2.881222782303541,"SY":-5.295628350728044,"X":449.78237409661335,"Y":222.37073476828974,"Id":"1","M":40}],"Objects":[{"T":"V","X":113,"Y":161,"Id":"20","M":40},{"T":"V","X":547,"Y":161,"Id":"21","M":40},{"T":"V","X":547,"Y":499,"Id":"22","M":40},{"T":"V","X":113,"Y":499,"Id":"23","M":40}]}
//
//    Start check strategies
//    Chosen strategy: Achiev: 0, Target: Vertex: 362.57135 : -118.41525, Debug: DEFAULT: Go TO Vertex: 412.5 : 82.5
//
//    INCOMING {"Mine":[{"R":12.649110640673518,"SX":-2.953515503600362,"SY":-5.531233277418952,"X":446.82885859301297,"Y":216.8395014908708,"Id":"1","M":40}],"Objects":[{"T":"V","X":113,"Y":161,"Id":"20","M":40},{"T":"V","X":547,"Y":161,"Id":"21","M":40},{"T":"V","X":547,"Y":499,"Id":"22","M":40},{"T":"V","X":113,"Y":499,"Id":"23","M":40}]}
//
//    Start check strategies
//    Chosen strategy: Achiev: 0, Target: Vertex: 367.26804 : -109.372345, Debug: DEFAULT: Go TO Vertex: 412.5 : 82.5

    @Test
    fun getMovementPointForTarget() {
        val gameEngine = GameEngine(mConfig, parseResult, 1)
        mDefaultStrategy.apply(gameEngine)
    }

}