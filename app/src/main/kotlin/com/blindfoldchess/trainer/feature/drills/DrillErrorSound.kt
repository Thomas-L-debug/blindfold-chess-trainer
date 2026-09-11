package com.blindfoldchess.trainer.feature.drills

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

private const val ErrorVolume = 65
private const val ErrorDurationMs = 180
private const val ErrorReleaseDelayMs = 230L

fun playDrillErrorSound() {
    runCatching {
        val generator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, ErrorVolume)
        generator.startTone(ToneGenerator.TONE_PROP_NACK, ErrorDurationMs)
        Handler(Looper.getMainLooper()).postDelayed({
            runCatching { generator.release() }
        }, ErrorReleaseDelayMs)
    }
}

@Composable
fun DrillErrorSoundEffect(play: Boolean, token: Int) {
    LaunchedEffect(play, token) {
        if (play && token > 0) playDrillErrorSound()
    }
}
