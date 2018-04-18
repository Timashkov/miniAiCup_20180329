package strategy

import WorldConfig
import data.MovementVector
import data.ParseResult
import incominginfos.EnemyInfo
import utils.GameEngine
import utils.Logger
import utils.Vertex
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2

class EatEnemyStrategy(val mGlobalConfig: WorldConfig, val mLogger: Logger) : IStrategy {

    var mStartSplitTick = -1
    var mTickBackCount = 0
    var mLastKnowTargetVetex: Vertex? = null
    var mCachedEnemy: EnemyInfo? = null

    override fun apply(gameEngine: GameEngine, cachedParseResult: ParseResult?): StrategyResult {

        mLogger.writeLog("Try to apply Eat Enemy")

        mLastKnowTargetVetex?.let { lastKnowTV ->
            mCachedEnemy?.let { cachedEnemy ->
                if (mTickBackCount > 0) {
                    if (gameEngine.worldParseResult.worldObjectsInfo.mEnemies.filter { it -> it.mId == cachedEnemy.mId && it.lastSeenTick == 0 }.isNotEmpty()) {
                        mTickBackCount--
                        val res = StrategyResult(10, mLastKnowTargetVetex!!, debugMessage = "Try to eat : repeate")
                        mLogger.writeLog("Enemy strat: $res")
                        return res
                    }
                }
                mCachedEnemy = null
                mLastKnowTargetVetex = null
                mTickBackCount = 0
            }
        }

        if (gameEngine.worldParseResult.worldObjectsInfo.mEnemies.isNotEmpty()) {
            return searchForEnemies(gameEngine, cachedParseResult)
        }

        return StrategyResult(-1, Vertex(0.0f, 0.0f), debugMessage = "Eat Enemy: Not applied")
    }

    override fun stopStrategy() {
    }

    private fun searchForEnemies(gameEngine: GameEngine, cachedParseResult: ParseResult?): StrategyResult {

//        val enemies = gameEngine.worldParseResult.worldObjectsInfo.mEnemies
        val me = gameEngine.worldParseResult.mineInfo
        val enemies: ArrayList<EnemyInfo> = ArrayList()
        me.mFragmentsState.forEach { frag ->
            frag.mCompass.mRumbBorders.forEach { rumb ->
                rumb.enemies.forEach { en ->
                    if (!enemies.contains(en))
                        enemies.add(en)
                }
            }
        }

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
                    targetVertex = targetVertex.plus(Vertex(diff.X * 4f, diff.Y * 4f))
                    if (me.getCoordinates().distance(chosenEnemy.mVertex) > me.getCoordinates().distance(enemy.mVertex)) {
                        //уходит от меня
                        if (me.getCoordinates().distance(chosenEnemy.mVertex) > me.getMainFragment().mRadius * 3) {
                            return StrategyResult(-1, Vertex.DEFAULT, debugMessage = "Don't pursuit on first game stage")
                        }
                    }
                    mLogger.writeLog("Target vert : $targetVertex")
                }
            }

            var movementPoint = targetVertex
            if (me.mFragmentsState.filter { fr -> fr.canEatEnemyByMass(chosenEnemy.mMass) }.size > 1)
                movementPoint = gameEngine.getMovementPointForTarget(me.getMainFragment().mId, targetVertex)

            val res = StrategyResult(10, movementPoint, debugMessage = "Try to eat ${chosenEnemy.mId}")
            mLogger.writeLog("Enemy strat: $res")
            return res
        }


        // Stage 2 search for small fragments < 2.5
        //TODO: делится самый большой или все сразу ?
        //TODO: проверить на направление

//        enemies.forEach {
//            var bool = me.getNearestFragment(it.mVertex).canEatEnemyBySplit(it.mMass)
//            bool = bool && me.getNearestFragment(it.mVertex) == me.getMainFragment()
//            bool = bool && me.mFragmentsState.none { fragment -> fragment.mCompass.isVertexInDangerArea(it.mVertex, 0.5f) }
//            bool = bool && me.getNearestFragment(it.mVertex).mCompass.mRumbBorders.none { rumb ->
//                rumb.enemies.any { enemyInfo ->
//                    me.getNearestFragment(it.mVertex).canBeEatenByEnemy(enemyInfo.mMass + 2 * mGlobalConfig.FoodMass)
//                }
//            }
//        }

        if (me.canSplit) {
            val nearLowerEnemies = enemies.filter {
                me.getNearestFragment(it.mVertex).canEatEnemyBySplit(it.mMass)
                        && me.getNearestFragment(it.mVertex) == me.getMainFragment()
                        && me.mFragmentsState.none { fragment -> fragment.mCompass.isVertexInDangerArea(it.mVertex, 0.5f) }
                        && me.getNearestFragment(it.mVertex).mCompass.mRumbBorders.none { rumb ->
                    rumb.enemies.any { enemyInfo ->
                        me.getNearestFragment(it.mVertex).canBeEatenByEnemy(enemyInfo.mMass + 2 * mGlobalConfig.FoodMass)
                    }
                }
            }.sortedBy { me.getCoordinates().distance(it.mVertex) }
            if (nearLowerEnemies.isNotEmpty()) {

                val chosenEnemy = nearLowerEnemies[0]
                var targetVertex = chosenEnemy.mVertex
                var split = false
                cachedParseResult?.let { cache ->
                    val cachedEnemy = cache.worldObjectsInfo.mEnemies.firstOrNull { it.mId == chosenEnemy.mId }
                    cachedEnemy?.let { enemy ->

                        if (me.getCoordinates().distance(chosenEnemy.mVertex) > me.getCoordinates().distance(enemy.mVertex)) {
                            //уходит от меня
                            return StrategyResult(-1, Vertex.DEFAULT, debugMessage = "Don't pursuit on first game stage")
                        }

//                        if (me.getCoordinates().distance(chosenEnemy.mVertex) > me.getMainFragment().mRadius * 3) {
//                            return StrategyResult(-1, Vertex.DEFAULT, debugMessage = "Can be ineffective")
//                        }

                        mLogger.writeLog("Cached Enemy : $enemy")
                        val diff = targetVertex.minus(enemy.mVertex)
                        targetVertex = targetVertex.plus(Vertex(diff.X * 4f, diff.Y * 4f))

                        val vecToTarget = MovementVector(targetVertex.X - enemy.mVertex.X, targetVertex.Y - enemy.mVertex.Y)
                        val angleToTarget = (atan2(vecToTarget.SY, vecToTarget.SX) * 180f / PI).toFloat()
                        val currentAngle = (atan2(me.getMainFragment().mSY, me.getMainFragment().mSX) * 180f / PI).toFloat()
                        if (abs(angleToTarget - currentAngle) <= 15f) {
                            split = true
                            mCachedEnemy = chosenEnemy// cache in case of split
                            mStartSplitTick = gameEngine.currentTick
                            mTickBackCount = (8f / mGlobalConfig.Viscosity).toInt()
                        }
                    }
                }

                val movementPoint = gameEngine.getMovementPointForTarget(me.getMainFragment().mId, targetVertex)
                val res = StrategyResult(10, movementPoint, split = split, debugMessage = "Try to eat ${chosenEnemy.mId}")
                mLastKnowTargetVetex = movementPoint
                mLogger.writeLog("Enemy 2 strat: $res")
                return res
            }


        }


        ////


        return StrategyResult(-1, Vertex(0.0f, 0.0f), debugMessage = "Eat Enemy: Not applied")
    }
}