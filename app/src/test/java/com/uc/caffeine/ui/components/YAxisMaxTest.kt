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
        assertEquals(50, nextYAxisMaxMg(currentMg = 100, autoMaxMg = 400, zoomIn = true))
    }

    @Test
    fun zoomIn_clampsAtTheLowestRung() {
        assertEquals(50, nextYAxisMaxMg(currentMg = 50, autoMaxMg = 400, zoomIn = true))
    }

    @Test
    fun zoomOut_steppingUpTheLadder() {
        assertEquals(150, nextYAxisMaxMg(currentMg = 100, autoMaxMg = 400, zoomIn = false))
        assertEquals(1500, nextYAxisMaxMg(currentMg = 1000, autoMaxMg = 400, zoomIn = false))
    }

    @Test
    fun zoomOut_pastTheTop_returnsAuto() {
        assertEquals(0, nextYAxisMaxMg(currentMg = 2000, autoMaxMg = 400, zoomIn = false))
    }

    @Test
    fun zoomOut_fromAuto_staysAuto() {
        assertEquals(0, nextYAxisMaxMg(currentMg = 0, autoMaxMg = 400, zoomIn = false))
    }
}
