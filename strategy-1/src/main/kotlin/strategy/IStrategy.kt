package strategy

import utils.GameEngine


interface IStrategy {
    fun apply(gameEngine: GameEngine): StrategyResult
    fun stopStrategy()
}