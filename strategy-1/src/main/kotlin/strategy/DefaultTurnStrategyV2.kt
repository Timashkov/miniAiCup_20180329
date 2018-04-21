package strategy

import incominginfos.MineInfo
import WorldConfig
import data.MovementVector
import data.ParseResult
import data.scaled
import utils.*
import java.util.*
import kotlin.math.abs
import kotlin.math.sign

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

        if (me.getMainFragment().mSX < 0 && me.getCoordinates().distance(Vertex(0f, me.getCoordinates().Y)) < me.getMainFragment().mRadius * 5f && mKnownVertex.X <= 0) {
            xVecFactor = -1f
        }

        if (me.getMainFragment().mSY < 0 && me.getCoordinates().distance(Vertex(me.getCoordinates().X, 0f)) < me.getMainFragment().mRadius * 5f && mKnownVertex.Y <= 0) {
            yVecFactor = -1f
        }

        if (xVecFactor == -1f || yVecFactor == -1f) {
            val vec = me.getCoordinates().getMovementVector(mKnownVertex)
            var sx = xVecFactor * me.getMainFragment().mSX
            var sy = yVecFactor * me.getMainFragment().mSY
            if (vec.SX.sign != sx.sign && xVecFactor == 1f) {
                sx *= -1f
            }
            if (vec.SY.sign != sy.sign && yVecFactor == 1f) {
                sy *= -1f
            }

            var mv = MovementVector(sx, sy)

            if (abs(mv.SX) < 0.05)
                mv = mv.scaled(mv.SX.sign, 1f)
            if (abs(mv.SY) < 0.05)
                mv = mv.scaled(1f, mv.SY.sign)

            if (abs(mv.SX / mv.SY) > 4f) {
                mv = mv.scaled(0.25f, 4f)
            }
            if (abs(mv.SX / mv.SY) < 0.25f) {
                mv = mv.scaled(4f, 0.25f)
            }
            mKnownVertex = GameEngine.vectorEdgeCrossPoint(me.getMainFragment().mVertex, mv, mGlobalConfig.GameWidth.toFloat(), mGlobalConfig.GameHeight.toFloat())
            return StrategyResult(0, mKnownVertex, debugMessage = "DEFAULT: mirrored $mKnownVertex")
        }

        if (me.getMainFragment().mSX == 0f && me.getMainFragment().mSY == 0f) {
            mKnownVertex = me.getMovementPointForTarget(me.getMainFragment(), mGlobalConfig.getCenter())
        } else if (mKnownVertex == Vertex.DEFAULT) {
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