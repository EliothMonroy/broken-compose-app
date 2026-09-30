package com.interviewprep.brokencalc

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CalculatorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun typingDigitsAppendsToDisplay() {
        val vm = CalculatorViewModel()
        vm.onButton("1")
        vm.onButton("2")
        vm.onButton("3")
        assertEquals("123", vm.display)
    }

    @Test
    fun clearEmptiesDisplay() {
        val vm = CalculatorViewModel()
        vm.onButton("7")
        vm.onButton("C")
        assertEquals("", vm.display)
    }

    @Test
    fun previewShowsResultWhileTyping() = runTest(testDispatcher) {
        val vm = CalculatorViewModel()
        vm.onButton("2")
        vm.onButton("+")
        vm.onButton("3")

        // skip the fake "thinking" delay, runTest uses virtual time
        advanceUntilIdle()

        assertEquals("5", vm.preview)
    }
}
