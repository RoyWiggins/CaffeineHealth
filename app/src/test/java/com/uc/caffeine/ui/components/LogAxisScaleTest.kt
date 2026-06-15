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
    fun logModeAnchorsZeroAtBaseline() {
        // log10(1 + 0) == 0, so "no caffeine" still sits on the axis baseline.
        assertEquals(0.0, caffeineToAxisSpace(0.0, logScale = true), 1e-9)
    }

    @Test
    fun logModeIsMonotonicAndCompressesRange() {
        val low = caffeineToAxisSpace(10.0, logScale = true)
        val mid = caffeineToAxisSpace(100.0, logScale = true)
        val high = caffeineToAxisSpace(300.0, logScale = true)

        assertTrue(low < mid)
        assertTrue(mid < high)
        // 10x more caffeine should not be 10x further up the axis.
        assertTrue((high - mid) < (300.0 - 100.0))
    }

    @Test
    fun logModeRoundTrips() {
        listOf(0.0, 5.0, 50.0, 250.0).forEach { mg ->
            val roundTripped = axisSpaceToCaffeine(caffeineToAxisSpace(mg, logScale = true), logScale = true)
            assertEquals(mg, roundTripped, 1e-6)
        }
    }
}
