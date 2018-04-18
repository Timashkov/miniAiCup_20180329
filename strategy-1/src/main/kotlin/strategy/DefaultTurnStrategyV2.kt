package strategy

import incominginfos.MineInfo
import WorldConfig
import data.MovementVector
import data.ParseResult
import utils.*
import java.util.*
import kotlin.math.abs

class DefaultTurnStrategyV2(val mGlobalConfig: WorldConfig, val mLogger: Logger) : IStrategy {

    val mPhantomFood: ArrayList<Vertex> = ArrayList()

    var mKnownVertex: Vertex = Vertex.DEFAULT

    override fun apply(gameEngine: GameEngine, cachedParseResult: ParseResult?): StrategyResult {
        val me = gameEngine.worldParseResult.mineInfo

        var xVecFactor = 1f
        var yVecFactor = 1f

        if (me.getMainFragment().mSX > 0 && me.getCoordinates().distance(Vertex(mGlobalConfig.GameWidth.toFloat(), me.getCoordinates().Y)) < me.getMainFragment().mRadius * 5f && mKnownVertex.X >= mGlobalConfig.GameWidth.toFloat()) {
            xVecFactor = -1f
        }

        if (me.getMainFragment().mSY > 0 && me.getCoordinates().distance(Vertex(me.getCoordinates().X, mGlobalConfig.GameHeight.toFloat())) < me.getMainFragment().mRadius * 5f && mKnownVertex.Y >= mGlobalConfig.GameHeight.toFloat()) {
            yVecFactor = -1f
        }

        if (me.getMainFragment().mSX < 0 && me.getCoordinates().distance(Vertex(0f, me.getCoordinates().Y)) < me.getMainFragment().mRadius * 5f && mKnownVertex.X <= 0 ) {
            xVecFactor = -1f
        }

        if (me.getMainFragment().mSY < 0 && me.getCoordinates().distance(Vertex(me.getCoordinates().X, 0f)) < me.getMainFragment().mRadius * 5f && mKnownVertex.Y <= 0) {
            yVecFactor = -1f
        }

        if (xVecFactor == -1f || yVecFactor == -1f) {
            val mv = MovementVector(xVecFactor * me.getMainFragment().mSX, yVecFactor * me.getMainFragment().mSY)
            mKnownVertex = GameEngine.vectorEdgeCrossPoint(me.getMainFragment().mVertex, mv, mGlobalConfig.GameWidth.toFloat(), mGlobalConfig.GameHeight.toFloat())
            return StrategyResult(0, mKnownVertex, debugMessage = "DEFAULT: mirrored $mKnownVertex")
        }

        if (me.getMainFragment().mSX == 0f && me.getMainFragment().mSY == 0f) {
            mKnownVertex = gameEngine.getMovementPointForTarget(me.getMainFragment().mId, mGlobalConfig.getCenter())
        } else if (mKnownVertex == Vertex.DEFAULT){
            val mv = MovementVector(me.getMainFragment().mSX, me.getMainFragment().mSY)
            mKnownVertex = GameEngine.vectorEdgeCrossPoint(me.getMainFragment().mVertex, mv, mGlobalConfig.GameWidth.toFloat(), mGlobalConfig.GameHeight.toFloat())
        }

        return StrategyResult(0, mKnownVertex, debugMessage = "DEFAULT: cross center $mKnownVertex")
    }

    override fun stopStrategy() {
        mKnownVertex = Vertex.DEFAULT
    }

    fun addPhantomFood(phantomFood: ArrayList<Vertex>, me: MineInfo) {
        phantomFood.forEach { it -> mPhantomFood.add(it) }
        while (mPhantomFood.size > 100) {
            mPhantomFood.removeAt(0)
        }

        if (mPhantomFood.isNotEmpty()) {
            mLogger.writeLog("PhantomFood: ${Arrays.toString(mPhantomFood.toTypedArray())}")
            mPhantomFood.removeIf { food ->
                me.mFragmentsState.any { frag -> frag.mVertex.distance(food) < frag.mRadius * WorldConfig.FOW_RADIUS_FACTOR }
            }
            mLogger.writeLog("PhantomFood Filtered: ${Arrays.toString(mPhantomFood.toTypedArray())}")
        }
    }
}