package com.blindfoldchess.trainer.feature.drills

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.blindfoldchess.trainer.R

@Composable
internal fun ReplayNavigation(
    canGoToStart: Boolean,
    canStepBack: Boolean,
    canStepForward: Boolean,
    canGoToLatest: Boolean,
    onGoToStart: () -> Unit,
    onStepBack: () -> Unit,
    onStepForward: () -> Unit,
    onGoToLatest: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NavTextButton(
            label = stringResource(R.string.famous_games_nav_start),
            enabled = canGoToStart,
            onClick = onGoToStart,
            modifier = Modifier.weight(1f),
        )
        NavChevronButton(
            forward = false,
            description = stringResource(R.string.famous_games_nav_back),
            enabled = canStepBack,
            onClick = onStepBack,
            modifier = Modifier.weight(1f),
        )
        NavChevronButton(
            forward = true,
            description = stringResource(R.string.famous_games_nav_forward),
            enabled = canStepForward,
            onClick = onStepForward,
            modifier = Modifier.weight(1f),
        )
        NavTextButton(
            label = stringResource(R.string.famous_games_nav_latest),
            enabled = canGoToLatest,
            onClick = onGoToLatest,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun NavTextButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavButton(
        enabled = enabled,
        onClick = onClick,
        modifier = modifier,
    ) {
        Text(
            text = label,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge.merge(
                TextStyle(
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.Both,
                    ),
                ),
            ),
        )
    }
}

@Composable
private fun NavChevronButton(
    forward: Boolean,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavButton(
        enabled = enabled,
        onClick = onClick,
        modifier = modifier.semantics { contentDescription = description },
    ) {
        NavChevron(forward = forward)
    }
}

@Composable
private fun NavButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(ButtonDefaults.MinHeight),
        contentPadding = PaddingValues(0.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Composable
private fun NavChevron(forward: Boolean) {
    val color = LocalContentColor.current
    Canvas(modifier = Modifier.size(18.dp)) {
        val stroke = Stroke(
            width = 2.25.dp.toPx(),
            cap = StrokeCap.Round,
        )
        val insetX = size.width * 0.34f
        val insetY = size.height * 0.22f
        val tipX = if (forward) size.width - insetX else insetX
        val tailX = if (forward) insetX else size.width - insetX
        val midY = size.height / 2f
        drawLine(
            color = color,
            start = Offset(tailX, insetY),
            end = Offset(tipX, midY),
            strokeWidth = stroke.width,
            cap = stroke.cap,
        )
        drawLine(
            color = color,
            start = Offset(tipX, midY),
            end = Offset(tailX, size.height - insetY),
            strokeWidth = stroke.width,
            cap = stroke.cap,
        )
    }
}
