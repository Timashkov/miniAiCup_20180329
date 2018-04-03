package strategy

import incominginfos.MineInfo
import WorldConfig
import utils.GameEngine
import utils.Logger
import utils.Vertex
import utils.Square
import kotlin.math.abs

class DefaultTurnStrategy(val mGlobalConfig: WorldConfig, val mLogger: Logger) : IStrategy {

    //TODO:
//: 618.75 : 866.25?? R > Ymax-Y
    val squares: ArrayList<Square> = ArrayList()

    init {
        initSquares(mGlobalConfig)
    }

    val mCenter: Vertex = mGlobalConfig.getCenter()
    var currentCornerIndex: Int = 0
    var currentSquareIndex: Int = -1

    override fun apply(gameEngine: GameEngine): StrategyResult {
        val me = gameEngine.worldParseResult.mineInfo
        if (currentSquareIndex == -1) {
            currentSquareIndex = getSquareByDirectionAndPosition(me)
        }
        val corner = squares[currentSquareIndex].corners[currentCornerIndex]
        if (abs(corner.X - me.getCoordinates().X) < me.getFragmentConfig(0).mRadius &&
                abs(corner.Y - me.getCoordinates().Y) < me.getFragmentConfig(0).mRadius)
            currentCornerIndex++
        if (currentCornerIndex == 4) {
            currentCornerIndex = 0
            currentSquareIndex = getNextSquareIndex(currentSquareIndex)
        }

        val targetVertex = squares[currentSquareIndex].corners[currentCornerIndex]

        val targetVector = gameEngine.getMovementPointForTarget(me.getCoordinates(), targetVertex, me.getMainFragment().mRadius)


        return StrategyResult(0, targetVertex, debugMessage = "DEFAULT: Go TO $targetVertex")
    }

    private fun getSquareByDirectionAndPosition(mineInfo: MineInfo): Int {
        val isInCenterSquare = Square(mCenter, mGlobalConfig.GameWidth / 8.0f).isInSquare(mineInfo.getCoordinates())

        val quart = getQuart(mineInfo.getCoordinates(), mCenter) - 1

        when (mineInfo.getDirection()) {
            MineInfo.Direction.TOP_LEFT -> {
                return if (isInCenterSquare)
                    0
                else
                    3
            }
            MineInfo.Direction.TOP_RIGHT -> return if (isInCenterSquare) 1 else 2
            MineInfo.Direction.BOTTOM_RIGHT -> return if (isInCenterSquare) 2 else 1
            MineInfo.Direction.BOTTOM_LEFT -> return if (isInCenterSquare) 3 else 0
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

    fun getQuart(vertex: Vertex, center: Vertex): Int {
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
        squares.add(Square(Vertex(globalConfig.GameWidth / 4f, globalConfig.GameHeight / 4f), globalConfig.GameWidth / 8.0f))
        squares.add(Square(Vertex(globalConfig.GameWidth / 4f * 3f, globalConfig.GameHeight / 4f), globalConfig.GameWidth / 8.0f))
        squares.add(Square(Vertex(globalConfig.GameWidth / 4f * 3f, globalConfig.GameHeight / 4f * 3f), globalConfig.GameWidth / 8.0f))
        squares.add(Square(Vertex(globalConfig.GameWidth / 4f, globalConfig.GameHeight / 4f * 3f), globalConfig.GameWidth / 8.0f))
    }
}