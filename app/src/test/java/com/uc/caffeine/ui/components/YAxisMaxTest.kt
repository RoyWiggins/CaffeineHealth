package com.uc.caffeine.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class YAxisMaxTest {

    @Test
    fun zoomIn_fromAuto_startsBelowTheDataDrivenMax() {
        // Auto (0) with a data max of 400 → first cap is the highest ladder rung < 400.
        assertEquals(300, nextYAxisMaxMg(currentMg = 0, autoMaxMg = 400, zoomIn = true))
    }

    @Test
    fun zoomIn_steppingDownTheLadder() {
        assertEquals(75, nextYAxisMaxMg(currentMg = 100, autoMaxMg = 400, zoomIn = true))
    }

    @Test
    fun zoomIn_clampsAtTheLowestRung() {
        assertEquals(25, nextYAxisMaxMg(currentMg = 25, autoMaxMg = 400, zoomIn = true))
    }

    @Test
    fun zoomOut_steppingUpTheLadder() {
        assertEquals(150, nextYAxisMaxMg(currentMg = 100, autoMaxMg = 400, zoomIn = false))
        assertEquals(500, nextYAxisMaxMg(currentMg = 400, autoMaxMg = 400, zoomIn = false))
    }

    @Test
    fun zoomOut_pastTheTop_returnsAuto() {
        assertEquals(0, nextYAxisMaxMg(currentMg = 500, autoMaxMg = 400, zoomIn = false))
    }

    @Test
    fun zoomOut_fromAuto_staysAuto() {
        assertEquals(0, nextYAxisMaxMg(currentMg = 0, autoMaxMg = 400, zoomIn = false))
    }

    @Test
    fun niceAxisStep_givesRoundGridlinesThatLandOnTheCap() {
        // For each ladder cap, the nice step should divide it evenly into a few ticks.
        mapOf(
            25.0 to 5.0,
            50.0 to 10.0,
            75.0 to 15.0,
            100.0 to 20.0,
            150.0 to 30.0,
            200.0 to 50.0,
            500.0 to 100.0,
        ).forEach { (max, expectedStep) ->
            assertEquals("step for max=$max", expectedStep, niceAxisStepMg(max), 1e-9)
        }
    }
}
