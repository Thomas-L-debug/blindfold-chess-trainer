package com.blindfoldchess.trainer.feature.drills

import android.content.Context
import android.media.AudioManager
import android.os.Build

/**
 * Google's speech engine plays its own start/error beeps. There is no public extra
 * to disable them, so we mute the streams it uses for the duration of recognition.
 */
internal class SpeechBeepSilencer(context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val streams = buildList {
        add(AudioManager.STREAM_SYSTEM)
        add(AudioManager.STREAM_NOTIFICATION)
        add(AudioManager.STREAM_MUSIC)
        add(AudioManager.STREAM_RING)
        if (Build.VERSION.SDK_INT >= 32) {
            add(11) // STREAM_ASSISTANT
        }
    }.toIntArray()
    private var saved: Map<Int, StreamState>? = null

    private data class StreamState(val volume: Int, val muted: Boolean)

    @Synchronized
    fun mute() {
        if (saved != null) return
        saved = streams.associateWith { stream ->
            StreamState(
                volume = audioManager.getStreamVolume(stream),
                muted = audioManager.isStreamMute(stream),
            )
        }
        streams.forEach { stream ->
            runCatching {
                audioManager.adjustStreamVolume(stream, AudioManager.ADJUST_MUTE, 0)
                if (!audioManager.isStreamMute(stream) && audioManager.getStreamVolume(stream) > 0) {
                    audioManager.setStreamVolume(stream, 0, 0)
                }
            }
        }
    }

    @Synchronized
    fun unmute() {
        val previous = saved ?: return
        saved = null
        previous.forEach { (stream, state) ->
            runCatching {
                if (state.muted) {
                    audioManager.adjustStreamVolume(stream, AudioManager.ADJUST_MUTE, 0)
                } else {
                    audioManager.adjustStreamVolume(stream, AudioManager.ADJUST_UNMUTE, 0)
                    audioManager.setStreamVolume(stream, state.volume, 0)
                }
            }
        }
    }
}
