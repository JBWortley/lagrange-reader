package com.vangeaux.lagrange

import android.support.v4.media.session.PlaybackStateCompat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EpubTtsPlaybackServiceTest {
    @Test
    fun `inactive service exposes no stale media actions`() {
        val state = EpubTtsServiceState()

        assertEquals(PlaybackStateCompat.STATE_NONE, epubTtsMediaPlaybackState(state))
        assertEquals(0L, epubTtsMediaPlaybackActions(state))
    }

    @Test
    fun `preparing service exposes close without premature transport controls`() {
        val state = EpubTtsServiceState(readerKey = "book", isPreparing = true)
        val actions = epubTtsMediaPlaybackActions(state)

        assertEquals(PlaybackStateCompat.STATE_BUFFERING, epubTtsMediaPlaybackState(state))
        assertTrue(actions hasAction PlaybackStateCompat.ACTION_STOP)
        assertFalse(actions hasAction PlaybackStateCompat.ACTION_PLAY)
        assertFalse(actions hasAction PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
    }

    @Test
    fun `paused session exposes resumable bounded travel and close controls`() {
        val state = EpubTtsServiceState(
            readerKey = "book",
            isPlaying = false,
            canGoPrevious = true,
            canGoNext = false
        )
        val actions = epubTtsMediaPlaybackActions(state)

        assertEquals(PlaybackStateCompat.STATE_PAUSED, epubTtsMediaPlaybackState(state))
        assertTrue(actions hasAction PlaybackStateCompat.ACTION_PLAY)
        assertTrue(actions hasAction PlaybackStateCompat.ACTION_PAUSE)
        assertTrue(actions hasAction PlaybackStateCompat.ACTION_PLAY_PAUSE)
        assertTrue(actions hasAction PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
        assertFalse(actions hasAction PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
        assertTrue(actions hasAction PlaybackStateCompat.ACTION_STOP)
    }

    @Test
    fun `playing and failed sessions publish accurate media states`() {
        assertEquals(
            PlaybackStateCompat.STATE_PLAYING,
            epubTtsMediaPlaybackState(
                EpubTtsServiceState(readerKey = "book", isPlaying = true)
            )
        )
        val failed = EpubTtsServiceState(
            readerKey = "book",
            failure = EpubTtsFailureKind.GENERIC
        )
        assertEquals(PlaybackStateCompat.STATE_ERROR, epubTtsMediaPlaybackState(failed))
        assertEquals(PlaybackStateCompat.ACTION_STOP, epubTtsMediaPlaybackActions(failed))
    }

    private infix fun Long.hasAction(action: Long): Boolean = this and action != 0L
}
