package data

import incominginfos.MineInfo
import incominginfos.WorldObjectsInfo
import utils.Vertex

data class ParseResult(val mineInfo: MineInfo, val worldObjectsInfo: WorldObjectsInfo, val phantomFood: ArrayList<Vertex>)