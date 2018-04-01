package strategy

import incominginfos.MineInfo
import incominginfos.WorldObjectsInfo
import utils.Logger


interface IStrategy {
    fun apply(worldInfo: WorldObjectsInfo, mineInfo: MineInfo): StrategyResult
}