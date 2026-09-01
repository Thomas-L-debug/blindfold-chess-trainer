package com.blindfoldchess.trainer.feature.drills

import androidx.lifecycle.ViewModel
import com.blindfoldchess.trainer.core.chess.ChessMan
import com.blindfoldchess.trainer.core.chess.ChessSession
import com.blindfoldchess.trainer.core.chess.ChessSnapshot
import com.blindfoldchess.trainer.core.chess.OccupiedSquare
import com.blindfoldchess.trainer.core.chess.PlayResult
import com.blindfoldchess.trainer.core.chess.ReplayMove
import com.blindfoldchess.trainer.core.chess.Square
import com.blindfoldchess.trainer.core.chess.formatPlayedMoves
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MoveDisambiguation(
    val man: ChessMan,
    val to: Square,
    val origins: List<Square>,
) {
    val askFile: Boolean
        get() = origins.map { it.file }.distinct().size > 1

    val options: List<String>
        get() = if (askFile) {
            origins.map { it.file.toString() }.distinct().sorted()
        } else {
            origins.map { it.rank.toString() }.distinct().sorted()
        }
}

data class FreeBoardUiState(
    val pieces: List<OccupiedSquare> = emptyList(),
    val moves: List<ReplayMove> = emptyList(),
    val isWhiteToMove: Boolean = true,
    val inCheck: Boolean = false,
    val isCheckmate: Boolean = false,
    val isStalemate: Boolean = false,
    val selectedMan: ChessMan? = null,
    val capturing: Boolean = false,
    val originFile: Char? = null,
    val originRank: Int? = null,
    val fromSquare: Square? = null,
    val pendingFile: Char? = null,
    val disambiguation: MoveDisambiguation? = null,
    val lastAttemptCorrect: Boolean? = null,
    val lastAttemptFrom: Square? = null,
    val lastAttemptSquare: Square? = null,
    val lastPlayedFrom: Square? = null,
    val lastPlayedTo: Square? = null,
    val plyIndex: Int = 0,
    val lineLength: Int = 0,
    val flashToken: Int = 0,
    val playerIsWhite: Boolean? = null,
    val botElo: Int? = null,
    val botThinking: Boolean = false,
    val botFailed: Boolean = false,
) {
    val playedMoves: String
        get() = formatPlayedMoves(moves, moves.size)

    val gameOver: Boolean
        get() = isCheckmate || isStalemate

    val canStepBack: Boolean
        get() = plyIndex > 0

    val canStepForward: Boolean
        get() = plyIndex < lineLength

    val canGoToStart: Boolean
        get() = canStepBack

    val canGoToLatest: Boolean
        get() = canStepForward

    val reviewing: Boolean
        get() = plyIndex < lineLength

    val atLatest: Boolean
        get() = plyIndex >= lineLength

    val isPlayerTurn: Boolean
        get() = playerIsWhite == null || isWhiteToMove == playerIsWhite

    val inputEnabled: Boolean
        get() = !gameOver && !botThinking && (reviewing || isPlayerTurn)

    val moveDraft: String
        get() {
            val piece = when (selectedMan) {
                ChessMan.KING -> "K"
                ChessMan.QUEEN -> "Q"
                ChessMan.ROOK -> "R"
                ChessMan.BISHOP -> "B"
                ChessMan.KNIGHT -> "N"
                ChessMan.PAWN, null -> ""
            }
            val origin = buildString {
                originFile?.let { append(it) }
                originRank?.let { append(it) }
            }
            val capture = if (capturing) "x" else ""
            val dest = pendingFile?.toString().orEmpty()
            return "$piece$origin$capture$dest"
        }
}

open class FreeBoardViewModel(
    protected val session: ChessSession = ChessSession(),
) : ViewModel() {

    protected val _uiState = MutableStateFlow(FreeBoardUiState())
    val uiState: StateFlow<FreeBoardUiState> = _uiState.asStateFlow()

    init {
        publish(session.snapshot())
    }

    fun selectMan(man: ChessMan) {
        val state = _uiState.value
        if (!state.inputEnabled) return
        val selected = if (state.selectedMan == man) null else man
        _uiState.update {
            it.copy(
                selectedMan = selected,
                originFile = null,
                originRank = null,
                pendingFile = null,
                disambiguation = null,
                lastAttemptCorrect = null,
            )
        }
    }

    fun toggleCapture() {
        val state = _uiState.value
        if (!state.inputEnabled) return
        val turningOn = !state.capturing
        val promotePendingToOrigin = turningOn && state.pendingFile != null && state.originFile == null
        _uiState.update {
            it.copy(
                capturing = !it.capturing,
                originFile = if (promotePendingToOrigin) state.pendingFile else it.originFile,
                pendingFile = if (promotePendingToOrigin) null else it.pendingFile,
                disambiguation = null,
                lastAttemptCorrect = null,
            )
        }
    }

    fun castle(kingside: Boolean) {
        val state = _uiState.value
        if (!state.inputEnabled) return
        val rank = if (state.isWhiteToMove) 1 else 8
        val from = Square('e', rank)
        val to = Square(if (kingside) 'g' else 'c', rank)
        val san = if (kingside) "O-O" else "O-O-O"
        applyResult(session.playSan(san), attemptedFrom = from, attemptedTo = to)
    }

    fun onSquareTap(square: Square) {
        val state = _uiState.value
        if (!state.inputEnabled) return
        val occupant = state.pieces.firstOrNull { it.square == square }
        val selected = state.fromSquare

        if (selected == null) {
            if (occupant != null && occupant.isWhite == state.isWhiteToMove) {
                _uiState.update {
                    it.copy(
                        fromSquare = square,
                        selectedMan = occupant.man,
                        pendingFile = null,
                        disambiguation = null,
                        lastAttemptCorrect = null,
                    )
                }
            }
            return
        }

        if (square == selected) {
            _uiState.update { it.copy(fromSquare = null, lastAttemptCorrect = null) }
            return
        }

        if (occupant != null && occupant.isWhite == state.isWhiteToMove) {
            _uiState.update {
                it.copy(
                    fromSquare = square,
                    selectedMan = occupant.man,
                    pendingFile = null,
                    disambiguation = null,
                    lastAttemptCorrect = null,
                )
            }
            return
        }

        applyResult(
            session.playSquares(selected, square, ChessMan.QUEEN),
            attemptedFrom = selected,
            attemptedTo = square,
        )
    }

    fun onFile(file: Char) {
        val state = _uiState.value
        val pending = state.disambiguation
        if (pending != null) {
            if (pending.askFile) onDisambiguate(file.toString())
            return
        }
        if (!state.inputEnabled) return
        if (state.pendingFile != null && state.originFile == null) {
            _uiState.update {
                it.copy(
                    originFile = state.pendingFile,
                    pendingFile = file,
                    lastAttemptCorrect = null,
                )
            }
            return
        }
        _uiState.update { it.copy(pendingFile = file, lastAttemptCorrect = null) }
    }

    fun onRank(rank: Int) {
        val state = _uiState.value
        val pending = state.disambiguation
        if (pending != null) {
            if (!pending.askFile) onDisambiguate(rank.toString())
            return
        }
        if (!state.inputEnabled) return
        if (state.pendingFile == null) {
            _uiState.update {
                it.copy(originRank = rank, lastAttemptCorrect = null)
            }
            return
        }
        val man = state.selectedMan ?: ChessMan.PAWN
        val to = Square(state.pendingFile, rank)
        applyResult(
            session.playPieceTo(
                man,
                to,
                originFile = state.originFile,
                originRank = state.originRank,
                promotion = ChessMan.QUEEN,
                capture = state.capturing,
            ),
            attemptedFrom = guessOrigin(
                man,
                to,
                capturing = state.capturing,
                originFile = state.originFile,
                originRank = state.originRank,
            ),
            attemptedTo = to,
        )
    }

    fun onDisambiguate(option: String) {
        val state = _uiState.value
        if (!state.inputEnabled && state.disambiguation == null) return
        val pending = state.disambiguation ?: return
        val remaining = if (pending.askFile) {
            pending.origins.filter { it.file.toString() == option }
        } else {
            pending.origins.filter { it.rank.toString() == option }
        }
        when (remaining.size) {
            0 -> applyResult(
                PlayResult.Illegal,
                attemptedFrom = null,
                attemptedTo = pending.to,
            )
            1 -> applyResult(
                session.playSquares(remaining.first(), pending.to, ChessMan.QUEEN),
                attemptedFrom = remaining.first(),
                attemptedTo = pending.to,
            )
            else -> _uiState.update {
                it.copy(
                    disambiguation = MoveDisambiguation(pending.man, pending.to, remaining),
                    lastAttemptCorrect = null,
                )
            }
        }
    }

    fun stepBack() {
        if (_uiState.value.botThinking) return
        if (!session.stepBack()) return
        publish(session.snapshot())
    }

    fun stepForward() {
        if (_uiState.value.botThinking) return
        if (!session.stepForward()) return
        publish(session.snapshot())
    }

    fun goToStart() {
        if (_uiState.value.botThinking) return
        if (!session.goToStart()) return
        publish(session.snapshot())
    }

    fun goToLatest() {
        if (_uiState.value.botThinking) return
        if (!session.goToLatest()) return
        publish(session.snapshot())
    }

    open fun undo() {
        if (_uiState.value.botThinking) return
        if (!session.undo()) return
        publish(session.snapshot())
    }

    open fun reset() {
        if (_uiState.value.botThinking) return
        session.reset()
        publish(session.snapshot())
        onSessionReset()
    }

    private fun guessOrigin(
        man: ChessMan,
        to: Square,
        capturing: Boolean,
        originFile: Char? = null,
        originRank: Int? = null,
    ): Square? {
        val state = _uiState.value
        state.fromSquare?.let { return it }
        var candidates = state.pieces.filter {
            it.man == man && it.isWhite == state.isWhiteToMove
        }
        if (originFile != null) {
            candidates = candidates.filter { it.square.file == originFile }
        }
        if (originRank != null) {
            candidates = candidates.filter { it.square.rank == originRank }
        }
        if (candidates.size == 1) return candidates.first().square
        if (man == ChessMan.PAWN) {
            val pool = if (capturing) {
                candidates.filter { kotlin.math.abs(it.square.file - to.file) == 1 }
            } else {
                candidates.filter { it.square.file == to.file }
            }
            if (pool.size == 1) return pool.first().square
        }
        return null
    }

    protected open fun onLegalMovePlayed() {}

    protected open fun onSessionReset() {}

    protected fun applyResult(
        result: PlayResult,
        attemptedFrom: Square?,
        attemptedTo: Square?,
    ) {
        when (result) {
            is PlayResult.Played -> {
                publish(
                    snapshot = session.snapshot(),
                    lastAttemptCorrect = true,
                    lastAttemptFrom = result.move.from,
                    lastAttemptSquare = result.move.to,
                )
                onLegalMovePlayed()
            }
            is PlayResult.Ambiguous -> {
                _uiState.update {
                    it.copy(
                        pendingFile = null,
                        originFile = null,
                        originRank = null,
                        disambiguation = MoveDisambiguation(
                            man = result.man,
                            to = result.to,
                            origins = result.origins,
                        ),
                        lastAttemptCorrect = null,
                        lastAttemptFrom = null,
                        lastAttemptSquare = result.to,
                    )
                }
            }
            PlayResult.Illegal, is PlayResult.NeedsPromotion -> {
                _uiState.update {
                    it.copy(
                        fromSquare = null,
                        pendingFile = null,
                        disambiguation = null,
                        lastAttemptCorrect = false,
                        lastAttemptFrom = attemptedFrom,
                        lastAttemptSquare = attemptedTo ?: it.fromSquare,
                        flashToken = it.flashToken + 1,
                    )
                }
            }
        }
    }

    protected fun publish(
        snapshot: ChessSnapshot,
        lastAttemptCorrect: Boolean? = null,
        lastAttemptFrom: Square? = null,
        lastAttemptSquare: Square? = null,
    ) {
        val last = snapshot.moves.lastOrNull()
        _uiState.update {
            it.copy(
                pieces = snapshot.pieces,
                moves = snapshot.moves,
                isWhiteToMove = snapshot.isWhiteToMove,
                inCheck = snapshot.inCheck,
                isCheckmate = snapshot.isCheckmate,
                isStalemate = snapshot.isStalemate,
                selectedMan = null,
                capturing = false,
                originFile = null,
                originRank = null,
                fromSquare = null,
                pendingFile = null,
                disambiguation = null,
                lastAttemptCorrect = lastAttemptCorrect,
                lastAttemptFrom = lastAttemptFrom,
                lastAttemptSquare = lastAttemptSquare,
                lastPlayedFrom = last?.from,
                lastPlayedTo = last?.to,
                plyIndex = snapshot.plyIndex,
                lineLength = snapshot.lineLength,
                flashToken = if (lastAttemptCorrect != null) it.flashToken + 1 else it.flashToken,
                playerIsWhite = it.playerIsWhite,
                botElo = it.botElo,
                botThinking = false,
                botFailed = false,
            )
        }
    }
}
