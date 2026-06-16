package com.uc.caffeine.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LogAxisScaleTest {

    @Test
    fun linearModeIsIdentity() {
        assertEquals(0.0, caffeineToAxisSpace(0.0, logScale = false), 1e-9)
        assertEquals(123.4, caffeineToAxisSpace(123.4, logScale = false), 1e-9)
        assertEquals(77.0, axisSpaceToCaffeine(77.0, logScale = false), 1e-9)
    }

    @Test
    fun logModePinsFloorAndBelowToBaseline() {
        val floor = 10.0
        // The floor itself maps to 0 (the axis baseline)...
        assertEquals(0.0, caffeineToAxisSpace(floor, logScale = true, floorMg = floor), 1e-9)
        // ...and anything below the floor is clamped to the baseline too.
        assertEquals(0.0, caffeineToAxisSpace(3.0, logScale = true, floorMg = floor), 1e-9)
        assertEquals(0.0, caffeineToAxisSpace(0.0, logScale = true, floorMg = floor), 1e-9)
    }

    @Test
    fun logModeIsMonotonicAndCompressesRange() {
        val floor = 10.0
        val low = caffeineToAxisSpace(20.0, logScale = true, floorMg = floor)
        val mid = caffeineToAxisSpace(100.0, logScale = true, floorMg = floor)
        val high = caffeineToAxisSpace(300.0, logScale = true, floorMg = floor)

        assertTrue(low < mid)
        assertTrue(mid < high)
        // A decade (10x) is a constant axis distance regardless of magnitude.
        val decadeLow = caffeineToAxisSpace(100.0, true, floor) - caffeineToAxisSpace(10.0, true, floor)
        val decadeHigh = caffeineToAxisSpace(1000.0, true, floor) - caffeineToAxisSpace(100.0, true, floor)
        assertEquals(decadeLow, decadeHigh, 1e-9)
    }

    @Test
    fun logModeRoundTripsAboveFloor() {
        val floor = 10.0
        listOf(10.0, 25.0, 100.0, 400.0).forEach { mg ->
            val roundTripped = axisSpaceToCaffeine(
                caffeineToAxisSpace(mg, logScale = true, floorMg = floor),
                logScale = true,
                floorMg = floor,
            )
            assertEquals(mg, roundTripped, 1e-6)
        }
    }
}
