package strategy

import incominginfos.MineInfo
import WorldConfig
import data.ParseResult
import utils.*
import kotlin.math.abs

class DefaultTurnStrategy(val mGlobalConfig: WorldConfig, val mLogger: Logger) : IStrategy {

    val squares: ArrayList<Polygon> = ArrayList()

    init {
        initSquares(mGlobalConfig)
    }

    private val mCenter: Vertex = mGlobalConfig.getCenter()
    var currentCornerIndex: Int = 0
    var currentSquareIndex: Int = -1

    override fun apply(gameEngine: GameEngine, cachedParseResult: ParseResult?): StrategyResult {
        val me = gameEngine.worldParseResult.mineInfo
        if (currentSquareIndex == -1) {
            currentSquareIndex = getSquareByDirectionAndPosition(me)
            currentCornerIndex = getNearestCornerIndex(currentSquareIndex, me)
        }
        var corner = squares[currentSquareIndex].corners[currentCornerIndex]

        if (abs(corner.X - me.getCoordinates().X) < me.getFragmentConfig(0).mRadius &&
                abs(corner.Y - me.getCoordinates().Y) < me.getFragmentConfig(0).mRadius)
            corner = getNextCorner()

        val fixedVertex = gameEngine.getMovementPointForTarget(me.getMainFragment().mId, corner)
        mLogger.writeLog("DEFAULT_TURN $fixedVertex \n")
        return StrategyResult(0, fixedVertex, debugMessage = "DEFAULT: Go TO $corner")
    }

    private fun getNearestCornerIndex(squareIndex: Int, me: MineInfo): Int {
        val dir = me.getDirection()
        val myV = me.getCoordinates()
        val verts = squares[squareIndex].corners.sortedBy{it.distance(myV)}

        verts.forEach { it->
            val Sx = it.X - myV.X
            val Sy = it.Y - myV.Y
            var dirToV = MineInfo.Direction.TOP_LEFT
            if (Sx > 0) {
                if (Sy > 0)
                    dirToV = MineInfo.Direction.BOTTOM_RIGHT
                else
                    dirToV = MineInfo.Direction.TOP_RIGHT
            } else {
                if (Sy > 0)
                    dirToV = MineInfo.Direction.BOTTOM_LEFT
                // else TOP LEFT
            }
            if (dirToV == dir)
                return verts.indexOf(it)
        }
        return 2
    }

    private fun getNextCorner(): Vertex {
        currentCornerIndex++
        if (currentCornerIndex == 4) {
            currentCornerIndex = 0
            currentSquareIndex = getNextSquareIndex(currentSquareIndex)
        }
        return squares[currentSquareIndex].corners[currentCornerIndex]
    }

    private fun getSquareByDirectionAndPosition(mineInfo: MineInfo): Int {
        //FIXME:Currentpos!!!!
        val currentPos = mineInfo.getCoordinates()


        when (mineInfo.getDirection()) {
            MineInfo.Direction.TOP_LEFT -> {
                return if (currentPos.Y < mGlobalConfig.GameHeight * 9f / 8f)
                    0
                else
                    3
            }
            MineInfo.Direction.TOP_RIGHT -> return if (currentPos.Y < mGlobalConfig.GameHeight * 9f / 8f) 1 else 2
            MineInfo.Direction.BOTTOM_RIGHT -> return if (currentPos.Y > mGlobalConfig.GameHeight * 7f / 8f) 2 else 1
            MineInfo.Direction.BOTTOM_LEFT -> return if (currentPos.Y > mGlobalConfig.GameHeight * 7f / 8f) 3 else 0
            else -> {
                //в квадрате ?
                val square = squares.firstOrNull { it.isInSquare(mineInfo.getCoordinates()) }
                if (square != null) {
                    return squares.indexOf(square)
                } else {
                    // не в квадрате
                    return getQuart(mineInfo.getCoordinates(), mCenter) - 1
                }
            }
        }
    }

    private fun getNextSquareIndex(current: Int): Int {
        var next = current + 1
        if (next == squares.size)
            next = 0
        return next
    }

    private fun getQuart(vertex: Vertex, center: Vertex): Int {
        if (vertex.X <= center.X) {
            if (vertex.Y <= center.Y)
                return 1
            return 4
        }
        if (vertex.Y <= center.Y) {
            return 2
        }
        return 3
    }

    override fun stopStrategy() {
        currentCornerIndex = 0
        currentSquareIndex = -1
    }

    private fun initSquares(globalConfig: WorldConfig) {
        squares.add(Polygon(Vertex(globalConfig.GameWidth / 4f, globalConfig.GameHeight / 4f), globalConfig.GameWidth / 8.0f, mGlobalConfig.getCenter()))
        squares.add(Polygon(Vertex(globalConfig.GameWidth / 4f * 3f, globalConfig.GameHeight / 4f), globalConfig.GameWidth / 8.0f, mGlobalConfig.getCenter()))
        squares.add(Polygon(Vertex(globalConfig.GameWidth / 4f * 3f, globalConfig.GameHeight / 4f * 3f), globalConfig.GameWidth / 8.0f, mGlobalConfig.getCenter()))
        squares.add(Polygon(Vertex(globalConfig.GameWidth / 4f, globalConfig.GameHeight / 4f * 3f), globalConfig.GameWidth / 8.0f, mGlobalConfig.getCenter()))
    }
}