package com.blindfoldchess.trainer.feature.drills

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.blindfoldchess.trainer.R
import com.blindfoldchess.trainer.ui.theme.Correct

@Composable
fun DrillPageHeader(
    title: String,
    description: String,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        DrillTitleRow(title = title, onHome = onHome)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun DrillTitleRow(
    title: String,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val titleStyle = MaterialTheme.typography.titleLarge
    val cubeSize = with(LocalDensity.current) {
        if (titleStyle.lineHeight.isSp) titleStyle.lineHeight.toDp() else titleStyle.fontSize.toDp() * 1.25f
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HomeCube(onClick = onHome, size = cubeSize)
        Text(
            text = title,
            style = titleStyle,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
        )
        Spacer(modifier = Modifier.size(cubeSize))
    }
}

@Composable
private fun HomeCube(
    onClick: () -> Unit,
    size: Dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(Correct)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClickLabel = stringResource(R.string.action_home),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(size * 0.62f)) {
            val w = this.size.width
            val h = this.size.height
            val stroke = w * 0.16f
            val cy = h / 2f
            val tip = Offset(w * 0.18f, cy)
            val tail = Offset(w * 0.86f, cy)
            val head = w * 0.38f
            drawLine(Color.White, tip, tail, strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(
                Color.White,
                tip,
                Offset(tip.x + head, cy - head),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
            drawLine(
                Color.White,
                tip,
                Offset(tip.x + head, cy + head),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
fun DrillBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Back",
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Text(label)
    }
}
