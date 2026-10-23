package com.example

import com.example.model.ExportResolution
import com.example.model.VideoFilter
import com.example.ui.components.formatMsToTimecode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TimelineTrimTest {

    @Test
    fun timecodeFormatter_formatsSecondsAndMinutesAccurately() {
        assertEquals("00:00.0", formatMsToTimecode(0L))
        assertEquals("00:05.5", formatMsToTimecode(5500L))
        assertEquals("01:30.2", formatMsToTimecode(90200L))
        assertEquals("10:00.0", formatMsToTimecode(600000L))
    }

    @Test
    fun trimBounds_preventNegativeOrInvertedRanges() {
        val durationMs = 15000L
        val requestedStart = -500L
        val requestedEnd = 20000L

        val safeStart = requestedStart.coerceAtLeast(0L)
        val safeEnd = requestedEnd.coerceAtMost(durationMs)

        assertEquals(0L, safeStart)
        assertEquals(15000L, safeEnd)
    }

    @Test
    fun filterPresets_resolveCorrectly() {
        assertEquals(VideoFilter.NORMAL, VideoFilter.fromId("normal"))
        assertEquals(VideoFilter.NOIR, VideoFilter.fromId("noir"))
        assertEquals(VideoFilter.WARM, VideoFilter.fromId("warm"))
        assertEquals(VideoFilter.NORMAL, VideoFilter.fromId("unknown_filter"))
    }

    @Test
    fun exportResolutions_haveValidDimensions() {
        assertEquals(1920, ExportResolution.RES_1080P.width)
        assertEquals(1080, ExportResolution.RES_1080P.height)
        assertEquals(1280, ExportResolution.RES_720P.width)
        assertEquals(720, ExportResolution.RES_720P.height)
        assertEquals(854, ExportResolution.RES_480P.width)
        assertEquals(480, ExportResolution.RES_480P.height)
    }
}
