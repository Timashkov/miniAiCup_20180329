package strategy

import WorldConfig
import data.ParseResult
import utils.GameEngine
import utils.Logger
import utils.Vertex

class FindFoodStrategyV2(val mGlobalConfig: WorldConfig, val mLogger: Logger) : IStrategy {


    override fun apply(gameEngine: GameEngine, cache: ParseResult?): StrategyResult {
        val mp = gameEngine.worldParseResult.mineInfo.getBestMovementPoint()
        if (mp != Vertex.DEFAULT) {
            return StrategyResult(1, mp, debugMessage = "FindFoodStrategyV2: $mp")
        }
        return StrategyResult(-1, Vertex.DEFAULT, debugMessage = "FindFoodStrategyV2: Not applied")
    }

    override fun stopStrategy() {

    }
}