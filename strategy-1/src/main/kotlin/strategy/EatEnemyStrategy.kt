package strategy

import WorldConfig
import data.ParseResult
import utils.GameEngine
import utils.Logger
import utils.Vertex

class EatEnemyStrategy(val mGlobalConfig: WorldConfig, val mLogger: Logger) : IStrategy {

    override fun apply(gameEngine: GameEngine, cachedParseResult: ParseResult?): StrategyResult {

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
            me.getMinorFragment().canEatEnemyByMass(it.mMass)
                    && (!me.getMinorFragment().canEatEnemyBySplit(it.mMass) || !me.getMinorFragment().canSplit)
                    && (me.mFragmentsState.none { fragment -> (fragment.mCompass.isVertexInDangerArea(it.mVertex)) })
        }.sortedBy { me.getCoordinates().distance(it.mVertex) }
        if (nearEnemies.isNotEmpty()) {
            val chosenEnemy = nearEnemies[0]
            var targetVertex = chosenEnemy.mVertex
            cachedParseResult?.let { cache ->
                val cachedEnemy = cache.worldObjectsInfo.mEnemies.firstOrNull { it.mId == chosenEnemy.mId }
                cachedEnemy?.let { enemy ->
                    targetVertex = targetVertex.plus(targetVertex.minus(enemy.mVertex))
                }
            }

            val res = StrategyResult(10, gameEngine.getMovementPointForTarget(me.getMainFragment().mId, me.getCoordinates(), targetVertex), debugMessage = "Try to eat ${chosenEnemy.mId}")
            mLogger.writeLog("Enemy strat: ${res}")
            return res
        }


        // Stage 2 search for small fragments < 2.5

        if (me.canSplit) {
            val nearLowerEnemies = enemies.filter {
                me.getMinorFragment().canEatEnemyBySplit(it.mMass) && (me.mFragmentsState.none { fragment -> fragment.mCompass.isVertexInDangerArea(it.mVertex) })
            }.sortedBy { me.getCoordinates().distance(it.mVertex) }
            if (nearLowerEnemies.isNotEmpty()) {
                val chosenEnemy = nearLowerEnemies[0]
                var targetVertex = chosenEnemy.mVertex
                cachedParseResult?.let { cache ->
                    val cachedEnemy = cache.worldObjectsInfo.mEnemies.firstOrNull { it.mId == chosenEnemy.mId }
                    cachedEnemy?.let { enemy ->
                        targetVertex = targetVertex.plus(targetVertex.minus(enemy.mVertex))
                    }
                }

                val res = StrategyResult(10, chosenEnemy.mVertex, split = true, debugMessage = "Try to eat ${chosenEnemy.mId}")
                mLogger.writeLog("Enemy 2 strat: ${res}")
                return res
            }
        }

        ////


        return StrategyResult(-1, Vertex(0.0f, 0.0f), debugMessage = "Eat Enemy: Not applied")
    }
}