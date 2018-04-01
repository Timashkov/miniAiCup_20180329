package strategy

import incominginfos.MineInfo
import incominginfos.WorldObjectsInfo


interface IStrategy {
    fun apply(worldInfo: WorldObjectsInfo, mineInfo: MineInfo): StrategyResult
    fun stopStrategy()
}