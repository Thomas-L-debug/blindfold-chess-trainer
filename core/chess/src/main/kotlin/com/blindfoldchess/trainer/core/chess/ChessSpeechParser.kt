package com.blindfoldchess.trainer.core.chess

data class SpokenPathMove(
    val piece: PieceType?,
    val square: Square?,
)

object ChessSpeechParser {

    fun parsePathMove(utterance: String): SpokenPathMove {
        val prepared = prepare(utterance)
        if (prepared.isEmpty()) return SpokenPathMove(piece = null, square = null)
        val atoms = coalesce(toAtoms(mappedWords(prepared)))
        val letter = atoms.filterIsInstance<Atom.Piece>().firstOrNull()?.letter
        val square = atoms.filterIsInstance<Atom.Sq>().lastOrNull()?.square
            ?: lastSquareIn(prepared)
        return SpokenPathMove(piece = letter.toPieceType(), square = square)
    }

    private fun lastSquareIn(prepared: String): Square? {
        val compact = prepared.replace(" ", "")
        Square.fromAlgebraic(compact.takeLast(2))?.let { return it }
        val tail = compact.takeLast(2)
        if (tail.length == 2 && tail[0] == 's' && tail[1] in '1'..'8') {
            Square.fromAlgebraic("f${tail[1]}")?.let { return it }
        }
        frenchGHomophoneSquare(compact)?.let { return it }
        return Regex("([a-h][1-8])").findAll(prepared).lastOrNull()
            ?.let { Square.fromAlgebraic(it.groupValues[1]) }
    }

    /** French STT often writes G-file as "j'ai" ("j'ai un" → g1). */
    private fun frenchGHomophoneSquare(compact: String): Square? {
        val match = Regex("jai(un|une|[1-8])$").find(compact) ?: return null
        val rankToken = match.groupValues[1]
        val rank = when (rankToken) {
            "un", "une" -> 1
            else -> rankToken.toInt()
        }
        return Square.fromAlgebraic("g$rank")
    }

    fun candidates(utterance: String): List<String> {
        val prepared = prepare(utterance)
        if (prepared.isEmpty()) return emptyList()

        val out = LinkedHashSet<String>()
        val words = mappedWords(prepared)
        assemble(words, out)
        extractCompact(words.joinToString(""), out)
        extractCompact(prepared.replace(" ", ""), out)
        return out.toList()
    }

    fun isUci(token: String): Boolean {
        if (token.length !in 4..5) return false
        return token[0] in 'a'..'h' &&
            token[1] in '1'..'8' &&
            token[2] in 'a'..'h' &&
            token[3] in '1'..'8'
    }

    private fun prepare(raw: String): String {
        return raw.lowercase()
            .replace('’', '\'')
            .replace(Regex("[–—−_-]"), " ")
            .replace(Regex("[^a-z0-9= ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun mappedWords(prepared: String): List<String> {
        var text = " $prepared "
        PHRASES.forEach { (from, to) ->
            text = text.replace(from, " $to ")
        }
        return text.split(Regex("\\s+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { word ->
                when {
                    word in DROP -> null
                    WORD_MAP.containsKey(word) -> WORD_MAP.getValue(word).ifEmpty { null }
                    else -> word
                }
            }
            .flatMap { explode(it) }
            .filter { it.isNotEmpty() }
    }

    private fun explode(word: String): List<String> {
        Regex("^([kqrbn])x([a-h])([1-8])$").matchEntire(word)?.let {
            return listOf(it.groupValues[1].uppercase(), "x", it.groupValues[2], it.groupValues[3])
        }
        Regex("^([kqrbn])([a-h])([a-h])([1-8])$").matchEntire(word)?.let {
            return listOf(
                it.groupValues[1].uppercase(),
                it.groupValues[2],
                it.groupValues[3],
                it.groupValues[4],
            )
        }
        Regex("^([kqrbn])([a-h])([1-8])$").matchEntire(word)?.let {
            return listOf(it.groupValues[1].uppercase(), it.groupValues[2], it.groupValues[3])
        }
        Regex("^([a-h])x([a-h])([1-8])$").matchEntire(word)?.let {
            return listOf(it.groupValues[1], "x", it.groupValues[2], it.groupValues[3])
        }
        Regex("^([a-h])([1-8])$").matchEntire(word)?.let {
            return listOf(it.groupValues[1], it.groupValues[2])
        }
        Regex("^s([1-8])$").matchEntire(word)?.let {
            return listOf("f", it.groupValues[1])
        }
        Regex("^j([1-8])$").matchEntire(word)?.let {
            return listOf("g", it.groupValues[1])
        }
        Regex("^jai([1-8])$").matchEntire(word)?.let {
            return listOf("g", it.groupValues[1])
        }
        Regex("^jai(un|une)$").matchEntire(word)?.let {
            return listOf("g", "1")
        }
        Regex("^([kqrbn])s([1-8])$").matchEntire(word)?.let {
            return listOf(it.groupValues[1].uppercase(), "f", it.groupValues[2])
        }
        Regex("^([kqrbn])xs([1-8])$").matchEntire(word)?.let {
            return listOf(it.groupValues[1].uppercase(), "x", "f", it.groupValues[2])
        }
        Regex("^([a-h])xs([1-8])$").matchEntire(word)?.let {
            return listOf(it.groupValues[1], "x", "f", it.groupValues[2])
        }
        return listOf(word)
    }

    private fun assemble(words: List<String>, out: MutableSet<String>) {
        if (words.isEmpty()) return
        if (words.any { it == "O-O-O" }) out += "O-O-O"
        if (words.any { it == "O-O" }) out += "O-O"

        val atoms = coalesce(toAtoms(words))
        if (atoms.any { it is Atom.Castle && it.long }) out += "O-O-O"
        if (atoms.any { it is Atom.Castle && !it.long }) out += "O-O"

        val piece = atoms.filterIsInstance<Atom.Piece>().firstOrNull()?.letter
        val capture = atoms.any { it is Atom.Capture }
        val squares = atoms.filterIsInstance<Atom.Sq>().map { it.square }
        val originFiles = atoms.filterIsInstance<Atom.File>().map { it.file }
        val originRanks = atoms.filterIsInstance<Atom.Rank>().map { it.rank }

        if (squares.size >= 2) {
            val from = squares[squares.size - 2]
            val to = squares.last()
            out += "${from.algebraic}${to.algebraic}"
            addSan(out, piece, null, null, capture, to)
        }
        if (squares.isNotEmpty()) {
            val dest = squares.last()
            val fileHint = originFiles.lastOrNull()
            val rankHint = originRanks.lastOrNull()
            if (piece == null && capture) {
                addPawnCaptureSans(out, dest)
            }
            addSan(out, piece, fileHint, rankHint, capture, dest)
            if (piece != null) {
                addSan(out, piece, null, null, capture, dest)
            }
        }
    }

    private fun addPawnCaptureSans(out: MutableSet<String>, dest: Square) {
        val left = dest.file - 1
        val right = dest.file + 1
        if (left in 'a'..'h') out += "${left}x${dest.algebraic}"
        if (right in 'a'..'h') out += "${right}x${dest.algebraic}"
    }

    private fun addSan(
        out: MutableSet<String>,
        piece: Char?,
        originFile: Char?,
        originRank: Int?,
        heardCapture: Boolean,
        dest: Square,
    ) {
        fun format(capture: Boolean): String = buildString {
            if (piece != null) append(piece)
            if (originFile != null) append(originFile)
            if (originRank != null) append(originRank)
            if (capture) append('x')
            append(dest.algebraic)
        }
        val without = format(false)
        val with = format(true)
        val captureMakesSense = piece != null || originFile != null
        if (heardCapture && captureMakesSense) {
            out += with
            out += without
        } else {
            out += without
            if (captureMakesSense) out += with
        }
    }

    private fun extractCompact(compact: String, out: MutableSet<String>) {
        if (compact.isEmpty()) return
        val body = compact.lowercase()
        if (body.contains("ooo") || body.contains("000")) out += "O-O-O"
        if (body.contains("oo") || body.contains("00")) out += "O-O"

        Regex("""([kqrbn])x([a-h][1-8])""").findAll(body).forEach { match ->
            val dest = Square.fromAlgebraic(match.groupValues[2]) ?: return@forEach
            addSan(out, match.groupValues[1].uppercase().first(), null, null, true, dest)
        }
        Regex("""([kqrbn])([a-h][1-8])""").findAll(body).forEach { match ->
            val dest = Square.fromAlgebraic(match.groupValues[2]) ?: return@forEach
            addSan(out, match.groupValues[1].uppercase().first(), null, null, false, dest)
        }
        Regex("""([kqrbn])([a-h])x?([a-h][1-8])""").findAll(body).forEach { match ->
            val dest = Square.fromAlgebraic(match.groupValues[3]) ?: return@forEach
            addSan(
                out,
                match.groupValues[1].uppercase().first(),
                match.groupValues[2][0],
                null,
                match.value.contains('x'),
                dest,
            )
        }
        Regex("""([a-h])x([a-h][1-8])""").findAll(body).forEach { match ->
            val dest = Square.fromAlgebraic(match.groupValues[2]) ?: return@forEach
            addSan(out, null, match.groupValues[1][0], null, true, dest)
        }
        Regex("""([a-h][1-8])([a-h][1-8])""").findAll(body).forEach { match ->
            out += match.groupValues[1] + match.groupValues[2]
        }
        Square.fromAlgebraic(body)?.let { out += it.algebraic }
    }

    private fun toAtoms(words: List<String>): List<Atom> {
        val atoms = mutableListOf<Atom>()
        var i = 0
        while (i < words.size) {
            val word = words[i]
            val next = words.getOrNull(i + 1)
            val next2 = words.getOrNull(i + 2)
            when {
                word == "O-O-O" -> atoms += Atom.Castle(long = true)
                word == "O-O" -> atoms += Atom.Castle(long = false)
                word == "o" && next == "o" && next2 == "o" -> {
                    atoms += Atom.Castle(long = true)
                    i += 2
                }
                word == "o" && next == "o" -> {
                    atoms += Atom.Castle(long = false)
                    i += 1
                }
                word == "0" && next == "0" && next2 == "0" -> {
                    atoms += Atom.Castle(long = true)
                    i += 2
                }
                word == "0" && next == "0" -> {
                    atoms += Atom.Castle(long = false)
                    i += 1
                }
                word.length == 2 && Square.fromAlgebraic(word) != null ->
                    atoms += Atom.Sq(Square.fromAlgebraic(word)!!)
                word.length == 1 && word[0] in "KQRBN" ->
                    atoms += Atom.Piece(word[0])
                word == "x" -> atoms += Atom.Capture
                word.length == 1 && word[0] in 'a'..'h' ->
                    atoms += Atom.File(word[0])
                word.length == 1 && word[0] in '1'..'8' ->
                    atoms += Atom.Rank(word[0].digitToInt())
            }
            i++
        }
        return atoms
    }

    private fun coalesce(atoms: List<Atom>): List<Atom> {
        val out = mutableListOf<Atom>()
        var i = 0
        while (i < atoms.size) {
            val current = atoms[i]
            val next = atoms.getOrNull(i + 1)
            if (current is Atom.File && next is Atom.Rank) {
                out += Atom.Sq(Square(current.file, next.rank))
                i += 2
            } else {
                out += current
                i++
            }
        }
        return out
    }

    private fun Char?.toPieceType(): PieceType? = when (this) {
        'N' -> PieceType.KNIGHT
        'B' -> PieceType.BISHOP
        'R' -> PieceType.ROOK
        'Q' -> PieceType.QUEEN
        else -> null
    }

    private sealed interface Atom {
        data class Piece(val letter: Char) : Atom
        data object Capture : Atom
        data class File(val file: Char) : Atom
        data class Rank(val rank: Int) : Atom
        data class Sq(val square: Square) : Atom
        data class Castle(val long: Boolean) : Atom
    }

    private val PHRASES = listOf(
        "queen side castle" to "O-O-O",
        "queenside castle" to "O-O-O",
        "castle queenside" to "O-O-O",
        "castle queen side" to "O-O-O",
        "long castle" to "O-O-O",
        "castle long" to "O-O-O",
        "grand roque" to "O-O-O",
        "grand rock" to "O-O-O",
        "roque long" to "O-O-O",
        "rock long" to "O-O-O",
        "king side castle" to "O-O",
        "kingside castle" to "O-O",
        "castle kingside" to "O-O",
        "castle king side" to "O-O",
        "short castle" to "O-O",
        "castle short" to "O-O",
        "petit roque" to "O-O",
        "petit rock" to "O-O",
        "roque court" to "O-O",
        "rock court" to "O-O",
        "j ai" to "g",
    )

    private val DROP = setOf(
        "the", "to", "on", "at", "please", "move", "moves", "play", "plays", "from", "then",
        "le", "la", "les", "du", "des", "de", "vers", "coup", "joue", "jouer", "va", "aller",
    )

    private val WORD_MAP = mapOf(
        "knight" to "N",
        "knights" to "N",
        "night" to "N",
        "nite" to "N",
        "cavalier" to "N",
        "cavaliers" to "N",
        "bishop" to "B",
        "bishops" to "B",
        "fou" to "B",
        "fous" to "B",
        "rook" to "R",
        "rooks" to "R",
        "tour" to "R",
        "tours" to "R",
        "queen" to "Q",
        "queens" to "Q",
        "dame" to "Q",
        "dames" to "Q",
        "king" to "K",
        "kings" to "K",
        "roi" to "K",
        "rois" to "K",
        "pawn" to "",
        "pawns" to "",
        "pion" to "",
        "pions" to "",
        "takes" to "x",
        "take" to "x",
        "taking" to "x",
        "taken" to "x",
        "capture" to "x",
        "captures" to "x",
        "captured" to "x",
        "prend" to "x",
        "prends" to "x",
        "prendre" to "x",
        "prise" to "x",
        "castle" to "O-O",
        "castling" to "O-O",
        "castles" to "O-O",
        "roque" to "O-O",
        "rock" to "O-O",
        "one" to "1",
        "un" to "1",
        "une" to "1",
        "two" to "2",
        "deux" to "2",
        "three" to "3",
        "trois" to "3",
        "four" to "4",
        "for" to "4",
        "quatre" to "4",
        "five" to "5",
        "cinq" to "5",
        "six" to "6",
        "seven" to "7",
        "sept" to "7",
        "eight" to "8",
        "ate" to "8",
        "huit" to "8",
        "ay" to "a",
        "bee" to "b",
        "be" to "b",
        "see" to "c",
        "sea" to "c",
        "si" to "c",
        "dee" to "d",
        "ee" to "e",
        "ef" to "f",
        "s" to "f",
        "ess" to "f",
        "gee" to "g",
        "j" to "g",
        "jai" to "g",
        "jay" to "g",
        "ai" to "",
        "aitch" to "h",
        "haitch" to "h",
        "hache" to "h",
        "alpha" to "a",
    )
}
