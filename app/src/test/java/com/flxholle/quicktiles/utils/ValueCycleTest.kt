package com.flxholle.quicktiles.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class ValueCycleTest {

    @Test
    fun `goes to the next value`() {
        assertEquals(1, ValueCycle.next(0, 3))
        assertEquals(2, ValueCycle.next(1, 3))
    }

    @Test
    fun `wraps around at the end`() {
        assertEquals(0, ValueCycle.next(2, 3))
    }

    @Test
    fun `starts at the first value when the current one is unknown`() {
        assertEquals(0, ValueCycle.next(-1, 3))
    }

    @Test
    fun `a single value always cycles to itself`() {
        assertEquals(0, ValueCycle.next(0, 1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a tile without values is a programming error`() {
        ValueCycle.next(0, 0)
    }
}
