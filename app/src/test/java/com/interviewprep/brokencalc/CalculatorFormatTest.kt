package com.interviewprep.brokencalc

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorFormatTest {

    @Test
    fun wholeNumbersHaveNoDecimals() {
        assertEquals("4", Calculator.format(4.0))
    }

    @Test
    fun halfIsShownAsPointFive() {
        assertEquals("0.5", Calculator.format(0.5))
    }
}
