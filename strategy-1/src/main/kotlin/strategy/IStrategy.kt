package strategy

import data.ParseResult
import utils.GameEngine


interface IStrategy {
    fun apply(gameEngine: GameEngine, cache: ParseResult? = null): StrategyResult
    fun stopStrategy()
}