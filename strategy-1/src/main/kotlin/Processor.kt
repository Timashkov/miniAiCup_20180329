import data.ParseResult
import incominginfos.MineInfo
import incominginfos.WorldObjectsInfo
import org.json.JSONObject
import strategy.*
import utils.GameEngine
import utils.Logger

class Processor(configJson: JSONObject) {

    private val mWorldConfig = WorldConfig(configJson)
    private val mLogger = Logger()
    private val mDefaultStrategy = DefaultTurnStrategy(mWorldConfig, mLogger)
    private val mEatEnemyStrategy = EatEnemyStrategy(mWorldConfig, mLogger)
    private val mEvasionFilter = WorldObjectsFilter(mWorldConfig, mLogger)
    private var mCurrentTick = 0
    private var mCachedParseResult: ParseResult? = null
    private val mFindFoodV2 = FindFoodStrategyV2(mWorldConfig, mLogger)

    enum class ACTIONS {
        MINE, HUNT, PURSUITE, ESCAPE
    }

    private var mAction: ACTIONS = ACTIONS.MINE

    // Tick Process
    fun onTick(tickData: JSONObject): JSONObject {
        mLogger.writeLog("\nTICK $mCurrentTick")
        mLogger.writeLog("INCOMING $tickData")
        val parsed = parseIncoming(tickData)
        val out = analyzeData(parsed, mCurrentTick)
        mCurrentTick++
        return out
    }

    // TODO: склеивать состояния компаса за последние n tick
    // TODO: при разнице масс фрагментов более чем на 10 % сначала полное объединение, потом уже разделение ( делит только один за один ход, то при необъодимости разделить 2 - придется потратить пару ходов )
    // TODO: атака разделением - посчитать время и дистанцию отстрела


    fun parseIncoming(tickData: JSONObject): ParseResult =
            ParseResult(MineInfo(tickData.getJSONArray("Mine"), mWorldConfig, mLogger), WorldObjectsInfo(tickData.getJSONArray("Objects"), mWorldConfig, mLogger))

    fun analyzeData(parseResult: ParseResult, currentTickCount: Int): JSONObject {
        if (parseResult.mineInfo.isNotEmpty()) {
            val data = mEvasionFilter.onFilter(parseResult, currentTickCount)
            try {

                val gameEngine = GameEngine(mWorldConfig, data, currentTickCount, mLogger)
                mLogger.writeLog("GE Parsed. Start check strategies")

                var strategyResult = mEatEnemyStrategy.apply(gameEngine, mCachedParseResult)
                mLogger.writeLog("$strategyResult")
                if (strategyResult.achievementScore > 0) {
                    mDefaultStrategy.stopStrategy()
                    mLogger.writeLog("APPLY eat enemy: $strategyResult\n")
                    return strategyResult.toJSONCommand()
                }

                strategyResult = mFindFoodV2.apply(gameEngine, mCachedParseResult)
                if (strategyResult.achievementScore > -1){
                    mDefaultStrategy.stopStrategy()
                    mLogger.writeLog("APPLY FF2: $strategyResult\n")
                    return strategyResult.toJSONCommand()
                }

                strategyResult = mDefaultStrategy.apply(gameEngine)
                mLogger.writeLog("$strategyResult")
                if (strategyResult.achievementScore >= 0) {
                    mLogger.writeLog("Chosen Default strategy: $strategyResult\n")
                    return strategyResult.toJSONCommand()
                }

            } catch (e: Exception) {
                mLogger.writeLog("Going wrong")
                mLogger.writeLog("${e.message}")
            } finally {
                mCachedParseResult = data
            }
        }
        mLogger.writeLog("DEFAULT DIED")
        return JSONObject(mapOf("X" to 0, "Y" to 0, "Debug" to "Died"))
    }

    private fun checkTriggers(gameEngine: GameEngine) {
        mAction = ACTIONS.MINE
    }
}


/*
points: food - 1, enemy_fragment - 10, enemy_total - 100, burst on virus - 2


// направление движения в виде конечной точки;
    // фрагменты будут подстраивать свои скорости (в соответствии с инерцией и остальной физикой),
    // чтобы приехать в указанную точку;
    "X": 123.0,
    "Y": 117.0,

    // отладочный вывод, который попадет в консоль браузера при визуализации;
    // максимум 1000 символов, все остальное будет обрезано;
    "Debug": "No food",

    // выполнить деление, выброс
    // "Split": true,
    // "Eject": true,

    // отладочный вывод в спрайт, который будет прикреплен в визуализаторе к игроку;
    // здесь как раз и нужны переданные во входных данных идентификаторы;
    "Sprite":
    {
        "Id": "1.1",
        "S": "Mass 50" // максимум 40 символов
    }*/

//{"FOOD_MASS":1,"GAME_HEIGHT":660,"GAME_TICKS":75000,"GAME_WIDTH":660,"INERTION_FACTOR":10,"MAX_FRAGS_CNT":10,"SPEED_FACTOR":25,"TICKS_TIL_FUSION":250,"VIRUS_RADIUS":22,"VIRUS_SPLIT_MASS":80,"VISCOSITY":0.25}
