package strategy

import WorldConfig
import data.MovementVector
import data.ParseResult
import utils.GameEngine
import utils.Logger
import utils.Vertex
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2

class EatEnemyStrategy(val mGlobalConfig: WorldConfig, val mLogger: Logger) : IStrategy {

    override fun apply(gameEngine: GameEngine, cachedParseResult: ParseResult?): StrategyResult {

        mLogger.writeLog("Try to apply Eat Enemy")
        if (gameEngine.worldParseResult.worldObjectsInfo.mEnemies.isNotEmpty()) {
            return searchForEnemies(gameEngine, cachedParseResult)
        }

        return StrategyResult(-1, Vertex(0.0f, 0.0f), debugMessage = "Eat Enemy: Not applied")
    }

    override fun stopStrategy() {
    }

    private fun searchForEnemies(gameEngine: GameEngine, cachedParseResult: ParseResult?): StrategyResult {
//TODO: review
        val enemies = gameEngine.worldParseResult.worldObjectsInfo.mEnemies
        val me = gameEngine.worldParseResult.mineInfo

        // Stage 1 - search for 1.25
        val nearEnemies = enemies.filter {
            me.getNearestFragment(it.mVertex).canEatEnemyByMass(it.mMass)
                    && (!me.getNearestFragment(it.mVertex).canEatEnemyBySplit(it.mMass) || me.getNearestFragment(it.mVertex) != me.getMainFragment() || !me.getNearestFragment(it.mVertex).canSplit)
                    && (me.mFragmentsState.none { fragment -> (fragment.mCompass.isVertexInDangerArea(it.mVertex)) })
        }.sortedBy { me.getCoordinates().distance(it.mVertex) }
        if (nearEnemies.isNotEmpty()) {
            val chosenEnemy = nearEnemies[0]
            var targetVertex = chosenEnemy.mVertex
            cachedParseResult?.let { cache ->
                val cachedEnemy = cache.worldObjectsInfo.mEnemies.firstOrNull { it.mId == chosenEnemy.mId }
                cachedEnemy?.let { enemy ->
                    mLogger.writeLog("Cached Enemy : $enemy")
                    val diff = targetVertex.minus(enemy.mVertex)
                    targetVertex = targetVertex.plus(diff).plus(diff)
                    mLogger.writeLog("Target vert : $targetVertex")
                }
            }

            val res = StrategyResult(10, gameEngine.getMovementPointForTarget(me.getMainFragment().mId, targetVertex), debugMessage = "Try to eat ${chosenEnemy.mId}")
            mLogger.writeLog("Enemy strat: $res")
            return res
        }


        // Stage 2 search for small fragments < 2.5
        //TODO: делится самый большой или все сразу ?
        //TODO: проверить на направление

        if (me.canSplit) {
            val nearLowerEnemies = enemies.filter {
                me.getNearestFragment(it.mVertex).canEatEnemyBySplit(it.mMass)
                        && me.getNearestFragment(it.mVertex) == me.getMainFragment()
                        && me.mFragmentsState.none { fragment -> fragment.mCompass.isVertexInDangerArea(it.mVertex, 0.5f) }
                        && me.getNearestFragment(it.mVertex).mCompass.mRumbBorders.none { rumb -> rumb.enemies.any { enemyInfo -> (enemyInfo.mMass + 2) / 1.2f  < me.getNearestFragment(it.mVertex).mMass / 2f } }
            }.sortedBy { me.getCoordinates().distance(it.mVertex) }
            if (nearLowerEnemies.isNotEmpty()) {
                val chosenEnemy = nearLowerEnemies[0]
                var targetVertex = chosenEnemy.mVertex
                var split = false
                cachedParseResult?.let { cache ->
                    val cachedEnemy = cache.worldObjectsInfo.mEnemies.firstOrNull { it.mId == chosenEnemy.mId }
                    cachedEnemy?.let { enemy ->
                        mLogger.writeLog("Cached Enemy : $enemy")
                        val diff = targetVertex.minus(enemy.mVertex)
                        targetVertex = targetVertex.plus(diff).plus(diff)

                        val vecToTarget = MovementVector(targetVertex.X - enemy.mVertex.X, targetVertex.Y - enemy.mVertex.Y)
                        val angleToTarget = (atan2(vecToTarget.SY, vecToTarget.SX) * 180f / PI).toFloat()
                        val currentAngle = (atan2(me.getMainFragment().mSY, me.getMainFragment().mSX) * 180f / PI).toFloat()
                        if (abs(angleToTarget - currentAngle) <= 15f)
                            split = true
                    }
                }

                val res = StrategyResult(10, gameEngine.getMovementPointForTarget(me.getMainFragment().mId, chosenEnemy.mVertex), split = split, debugMessage = "Try to eat ${chosenEnemy.mId}")
                mLogger.writeLog("Enemy 2 strat: $res")
                return res
            }


        }


        ////


        return StrategyResult(-1, Vertex(0.0f, 0.0f), debugMessage = "Eat Enemy: Not applied")
    }
}