package com.blindfoldchess.trainer.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.blindfoldchess.trainer.R
import com.blindfoldchess.trainer.feature.drills.DrillBackButton
import com.blindfoldchess.trainer.feature.drills.DrillPageHeader

@Composable
fun AboutScreen(
    onBack: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val sourceUrl = stringResource(R.string.about_source_url)
    val licenseUrl = stringResource(R.string.about_license_url)
    val privacyUrl = stringResource(R.string.about_privacy_url)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DrillPageHeader(
            title = stringResource(R.string.about_title),
            description = stringResource(R.string.about_description),
            onHome = onBack,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.about_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.about_components),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = { uriHandler.openUri(sourceUrl) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.about_source))
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { uriHandler.openUri(licenseUrl) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.about_license))
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { uriHandler.openUri(privacyUrl) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.about_privacy))
        }

        Spacer(modifier = Modifier.height(16.dp))
        DrillBackButton(onClick = onBack)
        Spacer(modifier = Modifier.height(16.dp))
    }
}
