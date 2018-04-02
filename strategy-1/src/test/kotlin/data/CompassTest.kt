package data

import org.junit.After
import org.junit.Before

import org.junit.Test
import utils.Compass

class CompassTest {

    lateinit var rs: Compass

    @Before
    fun setUp() {
        rs = Compass()
    }

    @After
    fun tearDown() {
    }

    @Test
    fun testAngle(){
        assert(4 == rs.getRumbIndexByVectorNormalized(MovementVector(0.99f, 1f)))
    }

    @Test
    fun testColors(){

        
    }

}