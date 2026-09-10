package com.blindfoldchess.trainer.feature.home

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blindfoldchess.trainer.core.chess.ChessMan
import com.blindfoldchess.trainer.core.chess.ChessMoveAnnouncer
import com.blindfoldchess.trainer.core.chess.ChessSession
import com.blindfoldchess.trainer.core.chess.NotationExample
import com.blindfoldchess.trainer.core.chess.NotationTutorial
import com.blindfoldchess.trainer.core.chess.NotationTutorialText
import com.blindfoldchess.trainer.core.chess.OccupiedSquare
import com.blindfoldchess.trainer.core.chess.Square
import com.blindfoldchess.trainer.feature.drills.DrillBackButton
import com.blindfoldchess.trainer.feature.drills.DrillPageHeader
import com.blindfoldchess.trainer.feature.drills.LanguageChips
import com.blindfoldchess.trainer.feature.drills.ScreenBottomSpace
import com.blindfoldchess.trainer.feature.drills.rememberChessTts
import com.blindfoldchess.trainer.feature.drills.rememberVoiceSpeechLanguage

@Composable
fun NotationTutorialScreen(
    onBack: () -> Unit,
    onPiecesChange: (List<OccupiedSquare>) -> Unit = {},
    onSelectedSquareChange: (Square?) -> Unit = {},
    onSquareClickChange: (((Square) -> Unit)?) -> Unit = {},
) {
    val (speechLanguage, setSpeechLanguage) = rememberVoiceSpeechLanguage()
    val announcerLanguage = speechLanguage.announcer
    val copy = remember(announcerLanguage) { NotationTutorial.text(announcerLanguage) }
    val tts = rememberChessTts()
    val startPieces = remember { ChessSession().snapshot().pieces }
    var tappedAlgebraic by rememberSaveable { mutableStateOf<String?>(null) }
    val tappedSquare = tappedAlgebraic?.let { Square.fromAlgebraic(it) }
    val tappedPiece = tappedSquare?.let { square -> startPieces.find { it.square == square } }
    val onPiecesChangeState = rememberUpdatedState(onPiecesChange)
    val onSelectedSquareChangeState = rememberUpdatedState(onSelectedSquareChange)
    val onSquareClickChangeState = rememberUpdatedState(onSquareClickChange)
    val announcerLanguageState = rememberUpdatedState(announcerLanguage)
    val languageTagState = rememberUpdatedState(speechLanguage.tag)
    val ttsState = rememberUpdatedState(tts)

    DisposableEffect(startPieces) {
        onPiecesChangeState.value(startPieces)
        onSquareClickChangeState.value { square ->
            tappedAlgebraic = square.algebraic
            val phrase = ChessMoveAnnouncer.spoken(
                square.algebraic,
                announcerLanguageState.value,
            )
            ttsState.value.speak(phrase, languageTagState.value)
        }
        onDispose {
            onPiecesChangeState.value(emptyList())
            onSelectedSquareChangeState.value(null)
            onSquareClickChangeState.value(null)
        }
    }

    LaunchedEffect(tappedSquare) {
        onSelectedSquareChange(tappedSquare)
    }

    var languageReady by remember { mutableStateOf(false) }
    LaunchedEffect(speechLanguage) {
        if (!languageReady) {
            languageReady = true
            return@LaunchedEffect
        }
        val algebraic = tappedAlgebraic ?: return@LaunchedEffect
        val phrase = ChessMoveAnnouncer.spoken(algebraic, announcerLanguage)
        tts.speak(phrase, speechLanguage.tag)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DrillPageHeader(
            title = copy.title,
            description = copy.description,
            onHome = onBack,
        )

        Spacer(modifier = Modifier.height(12.dp))

        LanguageChips(
            language = speechLanguage,
            onLanguage = setSpeechLanguage,
        )

        Spacer(modifier = Modifier.height(16.dp))
        BodyText(copy.intro)

        SectionTitle(copy.squaresTitle)
        BodyText(copy.squaresBody)
        Spacer(modifier = Modifier.height(8.dp))
        BodyText(copy.squaresTap)
        if (tappedSquare != null) {
            Spacer(modifier = Modifier.height(12.dp))
            SquarePreview(
                square = tappedSquare,
                piece = tappedPiece,
                language = announcerLanguage,
                copy = copy,
            )
        }

        SectionTitle(copy.piecesTitle)
        BodyText(copy.piecesIntro)
        Spacer(modifier = Modifier.height(8.dp))
        PieceLetterTable(copy)
        Spacer(modifier = Modifier.height(8.dp))
        BodyText(copy.piecesKnightNote)
        Spacer(modifier = Modifier.height(4.dp))
        BodyText(copy.piecesPawnNote)

        SectionTitle(copy.writeTitle)
        BodyText(copy.writeIntro)
        ExampleBlock(copy.writePawnAndPiece, copy.writePawnAndPieceExamples)
        ExampleBlock(copy.writeCapture, copy.writeCaptureExamples)
        ExampleBlock(copy.writeCastle, copy.writeCastleExamples)
        ExampleBlock(copy.writeCheck, copy.writeCheckExamples)
        ExampleBlock(copy.writeDisambiguation, copy.writeDisambiguationExamples)

        SectionTitle(copy.readTitle)
        BodyText(copy.readIntro)
        Spacer(modifier = Modifier.height(8.dp))
        copy.readExamples.forEach { example ->
            SanMeaningRow(example)
            Spacer(modifier = Modifier.height(4.dp))
        }

        SectionTitle(copy.speakTitle)
        BodyText(copy.speakIntro)
        Spacer(modifier = Modifier.height(8.dp))
        NotationTutorial.spokenSans.forEach { san ->
            SpeakExampleRow(
                san = san,
                language = announcerLanguage,
                listenLabel = copy.listen,
                onListen = { phrase -> tts.speak(phrase, speechLanguage.tag) },
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = copy.speakTipsTitle,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
        )
        Spacer(modifier = Modifier.height(6.dp))
        copy.speakTips.forEach { tip ->
            Text(
                text = "· $tip",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        BodyText(copy.footer)
        Spacer(modifier = Modifier.height(16.dp))
        DrillBackButton(onClick = onBack)
        ScreenBottomSpace()
    }
}

@Composable
private fun SectionTitle(text: String) {
    Spacer(modifier = Modifier.height(20.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Start,
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun BodyText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Start,
    )
}

@Composable
private fun ExampleBlock(rule: String, examples: List<NotationExample>) {
    Spacer(modifier = Modifier.height(10.dp))
    BodyText(rule)
    Spacer(modifier = Modifier.height(6.dp))
    examples.forEach { example ->
        SanMeaningRow(example)
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun SanMeaningRow(example: NotationExample) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = example.san,
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.widthIn(min = 72.dp),
        )
        Text(
            text = example.meaning,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SpeakExampleRow(
    san: String,
    language: ChessMoveAnnouncer.Language,
    listenLabel: String,
    onListen: (String) -> Unit,
) {
    val phrase = remember(san, language) { ChessMoveAnnouncer.spoken(san, language) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = san,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = phrase,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedButton(
            onClick = { onListen(phrase) },
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                text = listenLabel,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun SquarePreview(
    square: Square,
    piece: OccupiedSquare?,
    language: ChessMoveAnnouncer.Language,
    copy: NotationTutorialText,
) {
    val spoken = remember(square, language) {
        ChessMoveAnnouncer.spoken(square.algebraic, language)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = square.algebraic,
            fontSize = 32.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.primary,
        )
        if (piece != null) {
            Text(
                text = NotationTutorial.occupancyLabel(piece, language, copy),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = spoken,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PieceLetterTable(copy: NotationTutorialText) {
    val rows = listOf(
        Triple(glyph(ChessMan.KING), "K", copy.king),
        Triple(glyph(ChessMan.QUEEN), "Q", copy.queen),
        Triple(glyph(ChessMan.ROOK), "R", copy.rook),
        Triple(glyph(ChessMan.BISHOP), "B", copy.bishop),
        Triple(glyph(ChessMan.KNIGHT), "N", copy.knight),
        Triple(glyph(ChessMan.PAWN), "—", copy.pawn),
    )
    rows.forEach { (pieceGlyph, letter, name) ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = pieceGlyph,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.widthIn(min = 28.dp),
            )
            Text(
                text = letter,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.widthIn(min = 24.dp),
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun glyph(man: ChessMan): String {
    val char = when (man) {
        ChessMan.KING -> "\u265A"
        ChessMan.QUEEN -> "\u265B"
        ChessMan.ROOK -> "\u265C"
        ChessMan.BISHOP -> "\u265D"
        ChessMan.KNIGHT -> "\u265E"
        ChessMan.PAWN -> "\u265F"
    }
    return char + "\uFE0E"
}
