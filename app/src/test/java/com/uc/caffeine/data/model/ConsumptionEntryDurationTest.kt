package com.uc.caffeine.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ConsumptionEntryDurationTest {

    @Test
    fun pills_defaultToOneMinute() {
        assertEquals(DEFAULT_PILL_DURATION_MINUTES, defaultConsumptionDurationMinutes("pill"))
        assertEquals(DEFAULT_PILL_DURATION_MINUTES, defaultConsumptionDurationMinutes("Pills"))
        assertEquals(1, defaultConsumptionDurationMinutes("PILL"))
    }

    @Test
    fun otherCategories_useTheStandardDefault() {
        assertEquals(DEFAULT_CONSUMPTION_DURATION_MINUTES, defaultConsumptionDurationMinutes("Coffee"))
        assertEquals(DEFAULT_CONSUMPTION_DURATION_MINUTES, defaultConsumptionDurationMinutes("energy"))
        assertEquals(DEFAULT_CONSUMPTION_DURATION_MINUTES, defaultConsumptionDurationMinutes(""))
    }
}
