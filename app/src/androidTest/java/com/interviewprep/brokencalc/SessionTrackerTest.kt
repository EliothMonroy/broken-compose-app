package com.interviewprep.brokencalc

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class SessionTrackerTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var first: SessionTracker

    @Inject
    lateinit var second: SessionTracker

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun sessionTrackerIsASingleton() {
        assertSame(first, second)
    }
}
