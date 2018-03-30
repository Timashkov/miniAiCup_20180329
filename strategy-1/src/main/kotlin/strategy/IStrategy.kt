package strategy

import WorldConfig
import incominginfos.MineInfo
import incominginfos.WorldObjectsInfo

interface IStrategy {
    fun apply(globalConfig: WorldConfig, worldInfo: WorldObjectsInfo, mineInfo: MineInfo): StrategyResult
}