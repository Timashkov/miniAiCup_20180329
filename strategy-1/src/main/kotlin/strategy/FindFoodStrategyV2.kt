package strategy

import WorldConfig
import data.ParseResult
import utils.GameEngine
import utils.Logger
import utils.Vertex

class FindFoodStrategyV2(val mGlobalConfig: WorldConfig, val mLogger: Logger): IStrategy {


    override fun apply(gameEngine: GameEngine, cache: ParseResult?): StrategyResult {
     return StrategyResult(-1, Vertex(0.0f, 0.0f), debugMessage = "FindFoodStrategyV2: Not applied")
    }

    override fun stopStrategy() {

    }
}