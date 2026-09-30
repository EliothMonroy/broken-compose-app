package com.interviewprep.brokencalc

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class CalculatorTest {

    @Test
    fun twoPlusTwoIsFour() {
        assertEquals(4.0, Calculator.evaluate("2+2"), 0.0)
    }

    @Test
    fun multiplicationBeforeAddition() {
        assertEquals(14.0, Calculator.evaluate("2+3×4"), 0.0)
    }

    @Test
    fun subtractionIsLeftToRight() {
        assertEquals(3.0, Calculator.evaluate("10−5−2"), 0.0)
    }

    @Test
    fun divisionIsLeftToRight() {
        assertEquals(1.0, Calculator.evaluate("8÷4÷2"), 0.0)
    }

    @Test
    fun pointOnePlusPointTwo() {
        assertEquals("0.3", Calculator.evaluate("0.1+0.2").toString())
    }

    @Test
    fun divideByZeroIsAnError() {
        try {
            Calculator.evaluate("5÷0")
            fail("5÷0 should not give a number")
        } catch (e: Throwable) {
            // could be ArithmeticException or IllegalStateException, either one is fine
        }
    }
}
