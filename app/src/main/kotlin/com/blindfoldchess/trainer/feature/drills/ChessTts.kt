package com.blindfoldchess.trainer.feature.drills

import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

internal class ChessTtsController(
    val speak: (text: String, languageTag: String) -> Unit,
    val stop: () -> Unit,
)

@Composable
internal fun rememberChessTts(): ChessTtsController {
    val context = LocalContext.current
    var engine by remember { mutableStateOf<TextToSpeech?>(null) }
    var ready by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<Pair<String, String>?>(null) }
    val readyState = rememberUpdatedState(ready)
    val engineState = rememberUpdatedState(engine)

    DisposableEffect(context) {
        val tts = TextToSpeech(context) { status ->
            ready = status == TextToSpeech.SUCCESS
        }
        tts.setSpeechRate(0.92f)
        engine = tts
        onDispose {
            runCatching { tts.stop() }
            tts.shutdown()
            engine = null
            ready = false
        }
    }

    DisposableEffect(ready, pending, engine) {
        val queued = pending
        val tts = engine
        if (ready && queued != null && tts != null) {
            speakNow(tts, queued.first, queued.second)
            pending = null
        }
        onDispose { }
    }

    return remember {
        ChessTtsController(
            speak = { text, languageTag ->
                val tts = engineState.value
                if (text.isBlank()) return@ChessTtsController
                if (readyState.value && tts != null) {
                    speakNow(tts, text, languageTag)
                } else {
                    pending = text to languageTag
                }
            },
            stop = {
                pending = null
                runCatching { engineState.value?.stop() }
            },
        )
    }
}

private fun speakNow(tts: TextToSpeech, text: String, languageTag: String) {
    tts.language = Locale.forLanguageTag(languageTag)
    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "bot-move")
}
