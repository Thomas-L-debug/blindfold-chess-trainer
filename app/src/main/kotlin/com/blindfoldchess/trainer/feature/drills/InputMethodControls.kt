package com.blindfoldchess.trainer.feature.drills

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.blindfoldchess.trainer.R

private const val INPUT_PREFS = "input_methods"
private const val INPUT_PREFS_SHOW_PAD = "show_pad"
private const val INPUT_PREFS_SHOW_VOICE = "show_voice"

internal data class InputMethodVisibility(
    val showPad: Boolean,
    val showVoice: Boolean,
    val onShowPadChange: (Boolean) -> Unit,
    val onShowVoiceChange: (Boolean) -> Unit,
)

@Composable
internal fun rememberInputMethodVisibility(): InputMethodVisibility {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(INPUT_PREFS, Context.MODE_PRIVATE)
    }
    var showPad by remember {
        mutableStateOf(prefs.getBoolean(INPUT_PREFS_SHOW_PAD, true))
    }
    var showVoice by remember {
        mutableStateOf(prefs.getBoolean(INPUT_PREFS_SHOW_VOICE, true))
    }
    return InputMethodVisibility(
        showPad = showPad,
        showVoice = showVoice,
        onShowPadChange = { next ->
            showPad = next
            prefs.edit().putBoolean(INPUT_PREFS_SHOW_PAD, next).apply()
        },
        onShowVoiceChange = { next ->
            showVoice = next
            prefs.edit().putBoolean(INPUT_PREFS_SHOW_VOICE, next).apply()
        },
    )
}

@Composable
internal fun InputMethodControls(
    voiceEnabled: Boolean,
    voice: VoiceInputState,
    language: VoiceSpeechLanguage,
    onLanguage: (VoiceSpeechLanguage) -> Unit,
    pad: @Composable () -> Unit,
) {
    val visibility = rememberInputMethodVisibility()
    LaunchedEffect(visibility.showVoice, voice.listening) {
        if (!visibility.showVoice && voice.listening) {
            voice.cancel()
        }
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            InputMethodToggle(
                label = stringResource(R.string.input_pad),
                checked = visibility.showPad,
                onCheckedChange = visibility.onShowPadChange,
                modifier = Modifier.weight(1f),
            )
            InputMethodToggle(
                label = stringResource(R.string.input_voice),
                checked = visibility.showVoice,
                onCheckedChange = visibility.onShowVoiceChange,
                modifier = Modifier.weight(1f),
            )
        }
        if (visibility.showVoice) {
            Spacer(modifier = Modifier.height(8.dp))
            VoiceSpeakRow(
                enabled = voiceEnabled,
                voice = voice,
                language = language,
                onLanguage = onLanguage,
            )
        }
        if (visibility.showPad) {
            Spacer(modifier = Modifier.height(8.dp))
            pad()
        }
    }
}

@Composable
private fun InputMethodToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    if (checked) {
        Button(
            onClick = { onCheckedChange(false) },
            modifier = modifier,
            contentPadding = contentPadding,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            ToggleLabel(label)
        }
    } else {
        OutlinedButton(
            onClick = { onCheckedChange(true) },
            modifier = modifier,
            contentPadding = contentPadding,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            ToggleLabel(label)
        }
    }
}

@Composable
private fun ToggleLabel(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        textAlign = TextAlign.Center,
        maxLines = 1,
    )
}
