package incominginfos

import WorldConfig.Companion.MAGIC_MASS4EAT
import org.json.JSONObject
import utils.Compass
import utils.Vertex
import WorldConfig
import data.MovementVector
import strategy.StrategyResult
import utils.GameEngine

class MineFragmentInfo(val fragmentJson: JSONObject, val mGlobalConfig: WorldConfig) {
    val mVertex = Vertex(fragmentJson.getFloat("X"), fragmentJson.getFloat("Y"))
    val mId: String = fragmentJson.getString("Id")
    val mMass: Float = fragmentJson.getFloat("M")
    val mRadius: Float = fragmentJson.getFloat("R")
    val mSX: Float = fragmentJson.getFloat("SX")
    val mSY: Float = fragmentJson.getFloat("SY")
    val mTTF: Int = if (fragmentJson.has("TTF")) fragmentJson.getInt("TTF") else 0

    fun canEatEnemyByMass(enemyMass: Float): Boolean = enemyMass * WorldConfig.EAT_MASS_FACTOR < mMass

    fun canEatEnemyBySplit(enemyMass: Float): Boolean = enemyMass * WorldConfig.EAT_MASS_FACTOR * 2 < mMass

    fun canBeEatenByEnemy(enemyMass: Float, massFactor: Float = 1f): Boolean = enemyMass > mMass * WorldConfig.EAT_MASS_FACTOR * massFactor

    val canSplit: Boolean
        get() = mMass > WorldConfig.MIN_SPLITABLE_MASS

    val maySplit: Boolean
        get() = mMass > WorldConfig.MIN_SPLITABLE_MASS * 1f//(1.1f + 4f * mGlobalConfig.Viscosity) / (6f * mGlobalConfig.Viscosity)

    val mCompass: Compass = Compass(this, mGlobalConfig)

    override fun toString(): String {
        return "$mId: mass=$mMass r=$mRadius mSX=$mSX mSY=$mSY mTTF=$mTTF"
    }

    fun equals(other: MineFragmentInfo): Boolean {
        return mId == other.mId
    }

    fun canBurst(it: VirusInfo): Boolean {
        return mRadius > it.mRadius && mMass > WorldConfig.MIN_SPLITABLE_MASS
    }

    fun reconfigureCompass(foodPoints: ArrayList<Vertex>) {
        mCompass.reconfigure(foodPoints)
    }

    fun flippedVectorByEdge(target: Vertex): MovementVector{
        var xVecFactor = 1f
        var yVecFactor = 1f

        if (mSX > 0 && mVertex.distance(Vertex(mGlobalConfig.GameWidth.toFloat(), mVertex.Y)) < mRadius * 5f && target.X >= mGlobalConfig.GameWidth.toFloat()) {
            xVecFactor = -1f
        }

        if (mSY > 0 && mVertex.distance(Vertex(mVertex.X, mGlobalConfig.GameHeight.toFloat())) < mRadius * 5f && target.Y >= mGlobalConfig.GameHeight.toFloat()) {
            yVecFactor = -1f
        }

        if (mSX < 0 && mVertex.distance(Vertex(0f, mVertex.Y)) < mRadius * 5f && target.X <= 0 ) {
            xVecFactor = -1f
        }

        if (mSY < 0 && mVertex.distance(Vertex(mVertex.X, 0f)) < mRadius * 5f && target.Y <= 0) {
            yVecFactor = -1f
        }
        return MovementVector(xVecFactor * mSX, yVecFactor * mSY)
    }
}

/*    {
            // уникальный идентификатор (string)
            // для игроков через точку записывается номер фрагмента (если есть)
            "Id": "1.1",

            // координаты в пространстве (float)
            "X": 100.0, "Y": 100.0,

            // радиус и масса (float)
            "R": 8.0, "M": 40.0,

            // скорость в проекциях Ox и Oy (float)
            "SX": 0.365, "SY": 14.0,

            // таймер слияния (int) (если есть)
            // "TTF": 250,
        },*/