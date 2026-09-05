package com.blindfoldchess.trainer.feature.drills

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.blindfoldchess.trainer.R
import java.util.Locale

internal enum class VoiceSpeechLanguage(val tag: String) {
    French("fr-FR"),
    English("en-US"),
    ;

    companion object {
        fun fromTag(tag: String?): VoiceSpeechLanguage =
            entries.firstOrNull { it.tag == tag } ?: defaultForDevice()

        fun defaultForDevice(): VoiceSpeechLanguage =
            if (Locale.getDefault().language == "fr") French else English
    }
}

internal data class VoiceInputState(
    val listening: Boolean,
    val error: String?,
    val available: Boolean,
    val onClick: () -> Unit,
)

private const val VOICE_PREFS = "voice_input"
private const val VOICE_PREFS_LANGUAGE = "language_tag"

@Composable
internal fun rememberVoiceSpeechLanguage(): Pair<VoiceSpeechLanguage, (VoiceSpeechLanguage) -> Unit> {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(VOICE_PREFS, Context.MODE_PRIVATE)
    }
    var language by remember {
        mutableStateOf(VoiceSpeechLanguage.fromTag(prefs.getString(VOICE_PREFS_LANGUAGE, null)))
    }
    return language to { next ->
        language = next
        prefs.edit().putString(VOICE_PREFS_LANGUAGE, next.tag).apply()
    }
}

@Composable
internal fun rememberVoiceInput(
    enabled: Boolean,
    languageTag: String,
    onUtterances: (List<String>) -> Unit,
): VoiceInputState {
    val context = LocalContext.current
    val available = remember(context) { SpeechRecognizer.isRecognitionAvailable(context) }
    var listening by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val errorNoMatch = stringResource(R.string.voice_no_match)
    val errorUnavailable = stringResource(R.string.voice_unavailable)
    val errorNeedMic = stringResource(R.string.voice_need_mic)
    val errorGeneric = stringResource(R.string.voice_error)
    val errorNetwork = stringResource(R.string.voice_network)

    val onUtterancesState = rememberUpdatedState(onUtterances)
    val enabledState = rememberUpdatedState(enabled)
    val languageTagState = rememberUpdatedState(languageTag)

    val recognizer = remember(context, available) {
        if (available) {
            runCatching { SpeechRecognizer.createSpeechRecognizer(context) }.getOrNull()
        } else {
            null
        }
    }

    DisposableEffect(recognizer) {
        if (recognizer == null) {
            onDispose { }
        } else {
            val listener = object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    listening = true
                    error = null
                }

                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = Unit
                override fun onPartialResults(partialResults: Bundle?) = Unit
                override fun onEvent(eventType: Int, params: Bundle?) = Unit

                override fun onError(errorCode: Int) {
                    listening = false
                    error = when (errorCode) {
                        SpeechRecognizer.ERROR_NO_MATCH,
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
                        -> errorNoMatch
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> errorNeedMic
                        SpeechRecognizer.ERROR_NETWORK,
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
                        -> errorNetwork
                        SpeechRecognizer.ERROR_CLIENT -> null
                        else -> errorGeneric
                    }
                }

                override fun onResults(results: Bundle?) {
                    listening = false
                    error = null
                    val spoken = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        .orEmpty()
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    if (spoken.isEmpty()) {
                        error = errorNoMatch
                    } else if (enabledState.value) {
                        onUtterancesState.value(spoken)
                    }
                }
            }
            recognizer.setRecognitionListener(listener)
            onDispose {
                recognizer.setRecognitionListener(null)
                runCatching { recognizer.cancel() }
                recognizer.destroy()
                listening = false
            }
        }
    }

    fun startListening() {
        val engine = recognizer
        if (!enabledState.value) return
        if (engine == null) {
            error = errorUnavailable
            return
        }
        error = null
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTagState.value)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageTagState.value)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }
        runCatching {
            engine.cancel()
            engine.startListening(intent)
            listening = true
        }.onFailure {
            listening = false
            error = errorGeneric
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            startListening()
        } else {
            error = errorNeedMic
        }
    }

    val onClick = {
        when {
            !enabledState.value -> Unit
            listening -> {
                runCatching { recognizer?.cancel() }
                listening = false
            }
            !available -> error = errorUnavailable
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED -> {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            else -> startListening()
        }
    }

    return VoiceInputState(
        listening = listening,
        error = error,
        available = available,
        onClick = onClick,
    )
}

@Composable
internal fun VoiceSpeakRow(
    enabled: Boolean,
    voice: VoiceInputState,
    language: VoiceSpeechLanguage,
    onLanguage: (VoiceSpeechLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (voice.listening) {
            Button(
                onClick = voice.onClick,
                enabled = enabled,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.voice_listening))
            }
        } else {
            OutlinedButton(
                onClick = voice.onClick,
                enabled = enabled,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.voice_speak))
            }
        }
        FilterChip(
            selected = language == VoiceSpeechLanguage.French,
            onClick = { onLanguage(VoiceSpeechLanguage.French) },
            label = { Text(stringResource(R.string.voice_lang_fr)) },
            enabled = enabled && !voice.listening,
        )
        FilterChip(
            selected = language == VoiceSpeechLanguage.English,
            onClick = { onLanguage(VoiceSpeechLanguage.English) },
            label = { Text(stringResource(R.string.voice_lang_en)) },
            enabled = enabled && !voice.listening,
        )
    }
}
