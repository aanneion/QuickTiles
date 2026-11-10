package com.flxholle.quicktiles.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TileDiagnosticsTest {

    @Test
    fun `keeps the newest lines only`() {
        val log = "e\nf\ng\n"

        assertEquals("e\nf\n", TileDiagnostics.trimToLines(log, 2))
    }

    @Test
    fun `keeps a short log untouched`() {
        assertEquals("a\nb\n", TileDiagnostics.trimToLines("a\nb\n", 120))
    }

    @Test
    fun `an empty log stays empty`() {
        assertEquals("", TileDiagnostics.trimToLines("", 120))
        assertEquals("", TileDiagnostics.trimToLines(null, 120))
        assertEquals("", TileDiagnostics.trimToLines("a\nb\n", 0))
    }

    @Test
    fun `a fatal error names the thread, the exception and the first frames`() {
        val error = IllegalStateException("tile is not bound")
        error.stackTrace = arrayOf(
            StackTraceElement("com.flxholle.quicktiles.tiles.GrayscaleTileService", "onClick",
                "GrayscaleTileService.kt", 42),
            StackTraceElement("com.flxholle.quicktiles.abstract_tiles.SelectionTileService",
                "onClick", "SelectionTileService.kt", 53),
        )
        val line = TileDiagnostics.formatFatal(Thread.currentThread(), error)

        assertTrue(line.startsWith("FATAL in thread "))
        assertTrue(line.contains(currentThreadName()))
        assertTrue(line.contains("java.lang.IllegalStateException: tile is not bound"))
        assertTrue("The first frame is missing: " + line, line.contains("GrayscaleTileService.onClick"))
        assertTrue("The frames are not separated: " + line, line.contains(" | "))
    }

    @Test
    fun `a fatal error without a thread or exception does not crash`() {
        val line = TileDiagnostics.formatFatal(null, null)

        assertEquals("FATAL in thread ?: ?", line)
    }

    private fun currentThreadName(): String = Thread.currentThread().name
}
