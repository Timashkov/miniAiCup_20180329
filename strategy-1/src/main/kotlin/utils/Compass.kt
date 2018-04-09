package utils

import WorldConfig.Companion.FOW_RADIUS_FACTOR
import data.MovementVector
import incominginfos.*
import kotlin.math.*
import WorldConfig

// 1/32 of circle
class Compass(private val mFragment: MineFragmentInfo, private val mGlobalConfig: WorldConfig) {

    data class Rumb(val majorBorder: Float, var areaScore: Int = DEFAULT_AREA_FACTOR, var canEat: ArrayList<Vertex> = ArrayList(), var canEatEnemy: ArrayList<Vertex> = ArrayList())

    val mRumbBorders: Array<Rumb>
    val mCenterVertex = mFragment.mVertex

    init {
        val rs = ArrayList<Rumb>()
        for (i in -15..16) {
            rs.add(Rumb(11.25f * i))
        }
        mRumbBorders = rs.toTypedArray()
    }

    fun getRumbIndexByVector(mv: MovementVector): Int {
        val angle = atan2(mv.SY, mv.SX) * 180f / PI
        return getRumbIndexByAngle(angle.toFloat())
    }

    private fun getRumbIndexByAngle(angle: Float): Int {
        var target = angle
        while (angle > 180f) {
            target -= 360f
        }
        while (angle < -180f) {
            target += 360f
        }
        return mRumbBorders.indexOfFirst { angle <= it.majorBorder }
    }

    fun getRumbPointsByVector(mv: MovementVector): Int = mRumbBorders[getRumbIndexByVector(mv)].areaScore

    fun getShiftedIndex(index: Int, shifting: Int): Int {
        val res = index + shifting
        if (res > 0 && res >= mRumbBorders.size)
            return res % mRumbBorders.size

        if (res < 0)
            return mRumbBorders.size + res % mRumbBorders.size
        return res
    }

    fun getRumbIndexByVectorNormalized(mv: MovementVector): Int = getShiftedIndex(getRumbIndexByVector(mv), 16)

    fun setColorsByEnemies(me: MineFragmentInfo, enemy: EnemyInfo) {
        setColorsByEnemiesInternal(me.mVertex, MovementVector(me.mSX, me.mSY), me.mMass, enemy.mVertex, enemy.mRadius, enemy.mMass)
    }

    fun setColorsByFood(food: FoodInfo): Boolean {
        return setColorsByFoodInternal(mCenterVertex, food.mVertex)
    }

    fun setColorsByEjection(ejection: EjectionInfo): Boolean {
        return setColorsByFoodInternal(mCenterVertex, ejection.mVertex)
    }

    // can be private , but tests
    fun setColorsByEnemiesInternal(myPosition: Vertex, myVector: MovementVector, myMass: Float, enemyPosition: Vertex, enemyR: Float, enemyMass: Float) {
        if (myPosition.distance(enemyPosition) < enemyR) {
            if (enemyMass >= myMass * WorldConfig.EAT_MASS_FACTOR)
                setWholeCompassPoints(BLACK_SECTOR_POINTS)
            return
        }

        val vec = myPosition.getMovementVector(enemyPosition)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)

        if (enemyMass >= myMass * WorldConfig.EAT_MASS_FACTOR) {
            val eatDistance = mFragment.mRadius / 3f + enemyR
            val middle = (myPosition.distance(enemyPosition) + eatDistance) / 2
            val shiftedAngle = (asin(enemyR / middle) * 180f / PI).toFloat()

            var searchingAngle = shiftedAngle + directAngle
            var K = 1
            if (searchingAngle > 180f) {
                K = -1
                searchingAngle = directAngle - shiftedAngle
            }

            val shiftedRumbIndex = getRumbIndexByAngle(searchingAngle)

            var indexDelta = K * (shiftedRumbIndex - directMovementIndex)

            // + MAGIC_COMPASS_BLACK_DELTA

//            if (myMovementIndex == directMovementIndex) {
//                mRumbBorders[getShiftedIndex(directMovementIndex, 15)].areaScore = PREFERRED_SECTOR
//                mRumbBorders[getShiftedIndex(directMovementIndex, 16)].areaScore = BLACK_SECTOR_POINTS
//                mRumbBorders[getShiftedIndex(directMovementIndex, 17)].areaScore = PREFERRED_SECTOR
//            } else {
//                if (mRumbBorders[getShiftedIndex(directMovementIndex, 16)].areaScore != BLACK_SECTOR_POINTS)
//                    mRumbBorders[getShiftedIndex(directMovementIndex, 16)].areaScore = PREFERRED_SECTOR
//            }
            markRumbsByDirectAndShifting(directMovementIndex, indexDelta, BLACK_SECTOR_POINTS)
        } else if (enemyMass * WorldConfig.EAT_MASS_FACTOR <= myMass) {

            //анализ расстояния до объектов
//             mRumbBorders[directMovementIndex].areaScore != BLACK_SECTOR_POINTS) {

            markRumbsByDirectAndShifting(directMovementIndex, 1, PREFERRED_SECTOR)
        }
    }

    fun mergeCompass(compass: Compass, factor: Float) {
        compass.mRumbBorders.forEachIndexed { index, it ->
            mRumbBorders[index].areaScore += (it.areaScore * factor).toInt()
            it.canEatEnemy.forEach { enemy ->
                if (!mRumbBorders[index].canEatEnemy.contains(enemy))
                    mRumbBorders[index].canEatEnemy.add(enemy)
            }
            it.canEat.forEach { food ->
                if (!mRumbBorders[index].canEat.contains(food))
                    mRumbBorders[index].canEat.add(food)
            }
        }
    }

    fun setColorsByFoodInternal(myPosition: Vertex, foodPosition: Vertex): Boolean {

        val vec = myPosition.getMovementVector(foodPosition)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)

        if (mRumbBorders[directMovementIndex].areaScore != BLACK_SECTOR_POINTS) {
            mRumbBorders[directMovementIndex].canEat.add(foodPosition)
            mRumbBorders[directMovementIndex].areaScore++
            return true
        }
        return false
    }

    private fun setWholeCompassPoints(points: Int) {
        mRumbBorders.forEach { it.areaScore = points }
    }
    //TODO: set area factor by enemies with calc of distance

    private fun markRumbsByDirectAndShifting(directMovementIndex: Int, indexDelta: Int, points: Int) {
        for (i in indexDelta * -1..indexDelta) {
            if (points == BLACK_SECTOR_POINTS) {
                mRumbBorders[getShiftedIndex(directMovementIndex, i)].areaScore = points
            } else if (mRumbBorders[getShiftedIndex(directMovementIndex, i)].areaScore != BLACK_SECTOR_POINTS) {
                mRumbBorders[getShiftedIndex(directMovementIndex, i)].areaScore = points
            }
        }
    }

    fun isVertexInBlackArea(target: Vertex): Boolean {
        val vec = mCenterVertex.getMovementVector(target)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)
        return mRumbBorders[directMovementIndex].areaScore == BLACK_SECTOR_POINTS
    }

    fun isVertexInBurstArea(target: Vertex): Boolean {
        val vec = mCenterVertex.getMovementVector(target)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)
        return mRumbBorders[directMovementIndex].areaScore == BURST_SECTOR_POINTS
    }

    fun isVertexInDangerArea(target: Vertex): Boolean {
        val vec = mCenterVertex.getMovementVector(target)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)
        return mRumbBorders[directMovementIndex].areaScore in arrayOf(BURST_SECTOR_POINTS, BLACK_SECTOR_POINTS)
    }

    fun getAreaFactor(target: Vertex): Int {
        val vec = mCenterVertex.getMovementVector(target)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)
        return mRumbBorders[directMovementIndex].areaScore
    }

    fun hasBlackAreas(): Boolean {
        return mRumbBorders.any { it.areaScore == BLACK_SECTOR_POINTS }
    }

    fun hasDarkAreas(): Boolean {
        return mRumbBorders.any { it.areaScore < 1 }
    }

    fun setColorsByVirus(virus: VirusInfo): Boolean {
        if (mCenterVertex.distance(virus.mVertex) - virus.mRadius > mFragment.mRadius * FOW_RADIUS_FACTOR) {
            return false
        }
        val vec = mCenterVertex.getMovementVector(virus.mVertex)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)

        if (mRumbBorders[directMovementIndex].areaScore != BLACK_SECTOR_POINTS) {
            mRumbBorders[directMovementIndex].areaScore = BURST_SECTOR_POINTS

            return true
        }

        val shiftedAngle = (asin(virus.mRadius / mCenterVertex.distance(virus.mVertex)) * 180f / PI).toFloat()
        val shiftedRumbIndex = getRumbIndexByAngle(shiftedAngle + directAngle)
        val indexDelta = shiftedRumbIndex - directMovementIndex
        markRumbsByDirectAndShifting(directMovementIndex, indexDelta, BURST_SECTOR_POINTS)
        return false
    }

    fun getWhiteSectorsIndexesArray(): List<Rumb> {
        return mRumbBorders.filter { it.areaScore >= BURST_SECTOR_POINTS }
    }

    fun getMapEdgeBySector(rumbBorder: Float): Vertex {
        // val vec = myPosition.getMovementVector(enemyPosition)
        //val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()

        val K = tan(rumbBorder * PI / 180f).toFloat()
        val b = mCenterVertex.Y - mCenterVertex.X * K

        if (rumbBorder == 90f) {
            return Vertex(mCenterVertex.X, mGlobalConfig.GameHeight.toFloat())
        }
        if (rumbBorder == -90f) {
            return Vertex(mCenterVertex.X, 0f)
        }
        if (rumbBorder < -90 || rumbBorder > 90) {
            return GameEngine.fixByBorders(mCenterVertex, Vertex(0f, b), mGlobalConfig.GameWidth.toFloat(), mGlobalConfig.GameHeight.toFloat())
        } else {
            return GameEngine.fixByBorders(mCenterVertex, Vertex(mGlobalConfig.GameWidth.toFloat(), mGlobalConfig.GameWidth.toFloat() * K + b), mGlobalConfig.GameWidth.toFloat(), mGlobalConfig.GameHeight.toFloat())
        }
    }

    fun setColorByEdge(edgeVertex: Vertex) {
        setColorsByEdgeInternal(mCenterVertex, edgeVertex)
    }

    fun setColorsByEdgeInternal(myPosition: Vertex, edgeVertex: Vertex): Boolean {

        val vec = myPosition.getMovementVector(edgeVertex)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)
        val shiftingDelta = 4

        markRumbsByDirectAndShifting(directMovementIndex, shiftingDelta, EDGE_SECTOR)
        return true
    }

    companion object {
        val BLACK_SECTOR_POINTS = -100 // can be eaten here
        val BURST_SECTOR_POINTS = -50
        val PREFERRED_SECTOR = 5 //
        val EDGE_SECTOR = -10 //
        val DEFAULT_AREA_FACTOR = 1
    }

}