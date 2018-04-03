package data

import org.junit.After
import org.junit.Before

import org.junit.Test
import utils.Compass
import utils.Vertex

class CompassTest {

    lateinit var mCompass: Compass

    @Before
    fun setUp() {
        mCompass = Compass()
    }

    @After
    fun tearDown() {
    }

    @Test
    fun testAngle(){
        assert(4 == mCompass.getRumbIndexByVectorNormalized(MovementVector(0.99f, 1f)))
    }

    @Test
    fun testColors(){
        mCompass.setColorsByEnemiesInternal(Vertex(10f,10f), 1f,  Vertex(13f, 13f), 4f,20f)
        assert(mCompass.mRumbBorders.size == 32)

    }

}