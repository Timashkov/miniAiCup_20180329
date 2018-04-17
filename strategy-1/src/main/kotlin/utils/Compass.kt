package utils

import WorldConfig.Companion.FOW_RADIUS_FACTOR
import data.MovementVector
import incominginfos.*
import kotlin.math.*
import WorldConfig
import data.StepPoint

// 1/32 of circle
class Compass(private val mFragment: MineFragmentInfo, private val mGlobalConfig: WorldConfig, private val plain: Boolean = false) {

    data class Rumb(val majorBorder: Float, var areaScore: Int = DEFAULT_AREA_SCORE, var canEat: ArrayList<StepPoint> = ArrayList(), var enemies: ArrayList<EnemyInfo> = ArrayList(), var lastEscapePoint: Boolean = false)

    val mRumbBorders: Array<Rumb>
    val mCenterVertex = mFragment.mVertex

    init {
        val rs = ArrayList<Rumb>()
        for (i in -15..16) {
            rs.add(Rumb(11.25f * i, if (plain) 0 else DEFAULT_AREA_SCORE))
        }
        mRumbBorders = rs.toTypedArray()
    }

    fun getRumbIndexByVector(mv: MovementVector): Int {
        val angle = atan2(mv.SY, mv.SX) * 180f / PI
        return getRumbIndexByAngle(angle.toFloat())
    }

    private fun getRumbIndexByAngle(angle: Float): Int {
        var target = angle
        while (target > 180f) {
            target -= 360f
        }
        while (target < -180f) {
            target += 360f
        }
        return mRumbBorders.indexOfFirst { angle <= it.majorBorder }
    }

    fun getSectorScoreByVector(mv: MovementVector): Int = mRumbBorders[getRumbIndexByVector(mv)].areaScore

    fun getShiftedIndex(index: Int, shifting: Int): Int {
        val res = index + shifting
        if (res > 0 && res >= mRumbBorders.size)
            return res % mRumbBorders.size

        if (res < 0)
            return mRumbBorders.size + res % mRumbBorders.size
        return res
    }

    fun setColorsByEnemies(me: MineFragmentInfo, enemy: EnemyInfo) {
        val distance = me.mVertex.distance(enemy.mVertex)
        if (distance < enemy.mRadius + me.mRadius / 2f) {
            if (me.canBeEatenByEnemy(enemy.mMass)) {
                setWholeCompassPoints(BLACK_SECTOR_SCORE, enemy.mVertex.getMovementVector(me.mVertex))
                mRumbBorders.forEach { it.enemies.add(enemy) }
                return
            }
        }

        val vec = me.mVertex.getMovementVector(enemy.mVertex)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)

        if (me.canBeEatenByEnemy(enemy.mMass)) {

            val shiftedAngle = (asin((enemy.mRadius + me.mRadius / 2) / me.mVertex.distance(enemy.mVertex)) * 180f / PI).toFloat()

            var searchingAngle = shiftedAngle + directAngle
            var aign = 1
            if (searchingAngle > 180f) {
                aign = -1
                searchingAngle = directAngle - shiftedAngle
            }

            val shiftedRumbIndex = getRumbIndexByAngle(searchingAngle)
            val indexDelta = aign * (shiftedRumbIndex - directMovementIndex)

            for (i in -indexDelta..indexDelta) {
                if ((me.mRadius + enemy.mRadius * 5f > me.mVertex.distance(enemy.mVertex)))
                    mRumbBorders[getShiftedIndex(directMovementIndex, i)].areaScore = BLACK_SECTOR_SCORE
                mRumbBorders[getShiftedIndex(directMovementIndex, i)].enemies.add(enemy)
            }

        } else if (me.canEatEnemyByMass(enemy.mMass)) {

            val shiftedAngle = (asin((enemy.mRadius) / me.mVertex.distance(enemy.mVertex)) * 180f / PI).toFloat()

            var searchingAngle = shiftedAngle + directAngle
            var aign = 1
            if (searchingAngle > 180f) {
                aign = -1
                searchingAngle = directAngle - shiftedAngle
            }

            val shiftedRumbIndex = getRumbIndexByAngle(searchingAngle)
            val indexDelta = aign * (shiftedRumbIndex - directMovementIndex)

            for (i in -indexDelta..indexDelta) {
                if (mRumbBorders[getShiftedIndex(directMovementIndex, i)].areaScore != BLACK_SECTOR_SCORE) {
//                    if ((me.mRadius + enemy.mRadius * 5f > me.mVertex.distance(enemy.mVertex)))// мой радиус больше!!!
                    mRumbBorders[getShiftedIndex(directMovementIndex, i)].areaScore = PREFERRED_SECTOR_SCORE
                    mRumbBorders[getShiftedIndex(directMovementIndex, i)].enemies.add(enemy)
                }
            }
        }
    }

    fun mergeCompass(compass: Compass, factor: Float) {
        compass.mRumbBorders.forEachIndexed { index, it ->
            mRumbBorders[index].areaScore += (it.areaScore * factor).toInt()
            it.enemies.forEach { enemy ->
                if (!mRumbBorders[index].enemies.contains(enemy))
                    mRumbBorders[index].enemies.add(enemy)
            }
            it.canEat.forEach { food ->
                if (!mRumbBorders[index].canEat.contains(food))
                    mRumbBorders[index].canEat.add(food)
            }
        }
    }

    private fun setWholeCompassPoints(points: Int, escapeVector: MovementVector?) {
        mRumbBorders.forEach { it.areaScore = points }
        escapeVector?.let { vec ->
            val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
            val directMovementIndex = getRumbIndexByAngle(directAngle)

            for (i in -3..3) {
                mRumbBorders[getShiftedIndex(directMovementIndex, i)].lastEscapePoint = true
                mRumbBorders[getShiftedIndex(directMovementIndex, i)].areaScore = 2
            }
        }
    }

    private fun markRumbsByDirectAndShifting(directMovementIndex: Int, indexDelta: Int, points: Int) {
        for (i in indexDelta * -1..indexDelta) {
            if (points == BLACK_SECTOR_SCORE) {
                mRumbBorders[getShiftedIndex(directMovementIndex, i)].areaScore = points
            } else if (mRumbBorders[getShiftedIndex(directMovementIndex, i)].areaScore != BLACK_SECTOR_SCORE) {
                mRumbBorders[getShiftedIndex(directMovementIndex, i)].areaScore = points
            }
        }
    }

    fun isVertexInBlackArea(target: Vertex): Boolean {
        val vec = mCenterVertex.getMovementVector(target)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)
        if (mRumbBorders[directMovementIndex].areaScore == BLACK_SECTOR_SCORE)
            return true
        return mRumbBorders.any { rb -> rb.enemies.any { en -> en.mVertex.distance(mFragment.mVertex) < mFragment.mRadius + en.mRadius && mFragment.canBeEatenByEnemy(en.mMass) } }
    }

    fun isVertexInBurstArea(target: Vertex): Boolean {
        val vec = mCenterVertex.getMovementVector(target)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)
        return mRumbBorders[directMovementIndex].areaScore == BURST_SECTOR_SCORE
    }

    fun isVertexInDangerArea(target: Vertex, massFactor: Float = 1f): Boolean {
        val vec = mCenterVertex.getMovementVector(target)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)
        if (mRumbBorders[directMovementIndex].areaScore in arrayOf(BURST_SECTOR_SCORE, BLACK_SECTOR_SCORE))
            return true
        if (mRumbBorders.any { rb -> rb.enemies.any { en -> en.mVertex.distance(mFragment.mVertex) < mFragment.mRadius + en.mRadius && mFragment.canBeEatenByEnemy(en.mMass, massFactor) } }) {
            return true
        }
//        mRumbBorders.any { rb -> rb..any { en -> en.mVertex.distance(mFragment.mVertex) < mFragment.mRadius + en.mRadius && mFragment.canBurst(virus) } }){
        return false
    }

    fun isVertexInAreaWithEnemy(target: Vertex): Boolean {
        val vec = mCenterVertex.getMovementVector(target)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)
        return mRumbBorders[directMovementIndex].enemies.isNotEmpty()
    }

    fun getAreaScore(target: Vertex): Int {
        val vec = mCenterVertex.getMovementVector(target)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)
        return mRumbBorders[directMovementIndex].areaScore
    }

    fun setColorsByVirus(virus: VirusInfo): Boolean {
        val virusDistance = mCenterVertex.distance(virus.mVertex)
        if (virusDistance - virus.mRadius > mFragment.mRadius * FOW_RADIUS_FACTOR) {
            return false
        }
        val vec = mCenterVertex.getMovementVector(virus.mVertex)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)
        val shiftedAngle = (asin((virus.mRadius * 2f / 3f + mFragment.mRadius) / virusDistance) * 180f / PI).toFloat()

        var searchingAngle = shiftedAngle + directAngle
        var aign = 1
        if (searchingAngle > 180f) {
            aign = -1
            searchingAngle = directAngle - shiftedAngle
        }

        val shiftedRumbIndex = getRumbIndexByAngle(searchingAngle)
        val indexDelta = aign * (shiftedRumbIndex - directMovementIndex)

        for (i in indexDelta * -1..indexDelta) {
            if (mRumbBorders[getShiftedIndex(directMovementIndex, i)].areaScore != BLACK_SECTOR_SCORE) {
                if (mRumbBorders[getShiftedIndex(directMovementIndex, i)].enemies.isNotEmpty() && mRumbBorders[getShiftedIndex(directMovementIndex, i)].enemies.sortedBy { it.mVertex.distance(mCenterVertex) }[0].mVertex.distance(mCenterVertex) > virusDistance)
                    mRumbBorders[getShiftedIndex(directMovementIndex, i)].areaScore = BURST_SECTOR_SCORE
            }
        }
        return false
    }

    fun getWhiteSectorsIndexesArray(): List<Rumb> {
        return mRumbBorders.filter { it.areaScore >= BURST_SECTOR_SCORE }
    }

    fun hasBlackAreas(): Boolean {
        return mRumbBorders.any { it.areaScore == BLACK_SECTOR_SCORE }
    }

    fun hasDarkAreas(): Boolean {
        return mRumbBorders.any { it.areaScore < 1 }
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

    fun setColorByCorner(cornerVertex: Vertex) {
        when (cornerVertex) {
            mGlobalConfig.ltCorner -> {
                for (i in 0..8) {
                    if (abs(mRumbBorders[i].areaScore) <= CORNER_SECTOR_SCORE)
                        mRumbBorders[i].areaScore = CORNER_SECTOR_SCORE
                }
                return
            }
            mGlobalConfig.lbCorner -> {
                for (i in 24..31) {
                    if (abs(mRumbBorders[i].areaScore) <= CORNER_SECTOR_SCORE)
                        mRumbBorders[i].areaScore = CORNER_SECTOR_SCORE
                }
                return
            }
            mGlobalConfig.rbCorner -> {
                for (i in 16..24) {
                    if (abs(mRumbBorders[i].areaScore) <= CORNER_SECTOR_SCORE)
                        mRumbBorders[i].areaScore = CORNER_SECTOR_SCORE
                }
                return
            }
            mGlobalConfig.rtCorner -> {
                for (i in 8..16) {
                    if (abs(mRumbBorders[i].areaScore) <= CORNER_SECTOR_SCORE)
                        mRumbBorders[i].areaScore = CORNER_SECTOR_SCORE
                }
                return
            }
        }
    }

    fun setColorByVertex(vertex: Vertex, color: Int) {

        val vec = mCenterVertex.getMovementVector(vertex)
        val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
        val directMovementIndex = getRumbIndexByAngle(directAngle)
        if (mRumbBorders[directMovementIndex].areaScore > -1)
            mRumbBorders[directMovementIndex].areaScore = color
    }

    fun reconfigure(foodPoints: ArrayList<Vertex>) {
        setFoodToSectors(foodPoints)
        updateScore()
    }

    private fun setFoodToSectors(foodPoints: ArrayList<Vertex>) {
//        mRumbBorders.forEach { it.canEat.clear() }
        val filtered = foodPoints.filter { !isVertexInDangerArea(it) }

        val sortedByX = filtered.sortedBy { it.X }
        val sortedByR = filtered.sortedBy { mCenterVertex.distance(it) }


        sortedByR.forEach { it ->
            val verts: ArrayList<Vertex> = ArrayList()
            verts.add(it)
            val xIndex = sortedByX.indexOf(it)

            if (xIndex > 0) {
                for (i in xIndex - 1 downTo 0) {
                    if (abs(sortedByX[i].X - it.X) < mFragment.mRadius) {
                        if (it.distance(sortedByX[i]) < mFragment.mRadius * 0.95f)
                            verts.add(sortedByX[i])
                    } else
                        break
                }
            }
            if (xIndex < filtered.size - 1) {
                for (i in xIndex + 1 until filtered.size)
                    if (abs(sortedByX[i].X - it.X) < mFragment.mRadius) {
                        if (it.distance(sortedByX[i]) < mFragment.mRadius * 0.95f)
                            verts.add(sortedByX[i])
                    } else
                        break
            }

            val vec = mCenterVertex.getMovementVector(it)
            val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()
            val directMovementIndex = getRumbIndexByAngle(directAngle)
            mRumbBorders[directMovementIndex].canEat.add(StepPoint(it, it, verts, mFragment.mId))
            mRumbBorders[directMovementIndex].areaScore += verts.size
        }
    }


    private fun updateScore() {
        var first = -16
        var count = 0
        val sectorsSet: HashMap<Int, Int> = HashMap()

        mRumbBorders.forEach { sector ->

            if (sector.areaScore > -1) {
                if (first > -1) {
                    count += 1
                } else {
                    first = mRumbBorders.indexOf(sector)
                    count += 1
                }
            } else {
                if (first > -1) {
                    sectorsSet[first] = count
                    first = -1
                    count = 0
                }
            }
        }

        if (first > -1) {
            // last sector still +
            if (sectorsSet.containsKey(0)) {
                sectorsSet[first] = sectorsSet[0]!! + count
                sectorsSet.remove(0)
            } else
                sectorsSet[first] = count
        }

        val powerMax = getDangerSectorsCount()

        var maxScore = 0
        sectorsSet.forEach { startIndex, count ->
            var power = 0
            if (getDangerSectorsCount() > 16) {
                power = powerMax - 8
            }
            val shifting = count / 2
            var gg = 0
            if (shifting > 2 && shifting % 3 < count)
                gg = shifting % 3

            val directMovementIndex = getShiftedIndex(startIndex, shifting + gg)
            for (i in shifting + 1 downTo 1) {

                if (mRumbBorders[getShiftedIndex(directMovementIndex, i - 1)].areaScore > 0 && count >= shifting + gg + i - 1) {
                    mRumbBorders[getShiftedIndex(directMovementIndex, i - 1)].areaScore *= 2f.pow(power).toInt()
                    if (mRumbBorders[getShiftedIndex(directMovementIndex, i - 1)].areaScore > maxScore)
                        maxScore = mRumbBorders[getShiftedIndex(directMovementIndex, i - 1)].areaScore
                }
                if (mRumbBorders[getShiftedIndex(directMovementIndex, -i)].areaScore > 0 && 0 <= shifting + gg - i) {
                    mRumbBorders[getShiftedIndex(directMovementIndex, -i)].areaScore *= 2f.pow(power).toInt()
                    if (mRumbBorders[getShiftedIndex(directMovementIndex, -i)].areaScore > maxScore)
                        maxScore = mRumbBorders[getShiftedIndex(directMovementIndex, -i)].areaScore
                }
                if (power < powerMax)
                    power++
            }
        }

        if (maxScore > 4) {
            val maxSectors = mRumbBorders.filter { it.areaScore == maxScore }
            if (maxSectors.size > 1) {
                var minAngle = 360f
                var index = -1
                val currentAngle = (atan2(mFragment.mSY, mFragment.mSX) * 180f / PI).toFloat()
                maxSectors.forEach {
                    if (abs(it.majorBorder - currentAngle) < minAngle) {
                        minAngle = abs(it.majorBorder - currentAngle)
                        index = maxSectors.indexOf(it)
                    }
                }
                maxSectors[index].areaScore += 10
            }
        }

    }

    fun getSectorFoodPoint(sector: Rumb): StepPoint {
        val canEat = sector.canEat
        if (canEat.isNotEmpty())
            return canEat.maxBy { it.target.distance(mCenterVertex) }!!

        val target = getVertexBySector(sector.majorBorder)
        return StepPoint(target, target, listOf(target), mFragment.mId)
    }

    fun getVertexBySector(rumbBorder: Float): Vertex {
        // val vec = myPosition.getMovementVector(enemyPosition)
        //val directAngle = (atan2(vec.SY, vec.SX) * 180f / PI).toFloat()

        val K = tan(rumbBorder * PI / 180f).toFloat()
        val b = mCenterVertex.Y - mCenterVertex.X * K

        if (rumbBorder == 90f) {
            return Vertex(mCenterVertex.X, mCenterVertex.Y + mFragment.mRadius * 4)
        }
        if (rumbBorder == -90f) {
            return Vertex(mCenterVertex.X, mCenterVertex.Y - mFragment.mRadius * 4)
        }
        if (rumbBorder < -90 || rumbBorder > 90) {
            return Vertex(mCenterVertex.X - mFragment.mRadius * 4, (mCenterVertex.X - mFragment.mRadius * 4) * K + b)
        } else {
            return Vertex(mCenterVertex.X + mFragment.mRadius * 4, (mCenterVertex.X + mFragment.mRadius * 4) * K + b)
        }
    }

    fun getMaxSectorScore(): Int {
        var score = 0
        mRumbBorders.forEach { if (it.areaScore > score) score = it.areaScore }
        return score
    }

    fun getDangerSectorsCount(): Int = mRumbBorders.filter { it.areaScore <= -50 }.size

    //TODO: set area factor by enemies with calc of distance

    companion object {
        val BLACK_SECTOR_SCORE = -100 // can be eaten here
        val BURST_SECTOR_SCORE = -50
        val PREFERRED_SECTOR_SCORE = 50 //
        val CORNER_SECTOR_SCORE = -10 //
        val DEFAULT_AREA_SCORE = 1
    }

}