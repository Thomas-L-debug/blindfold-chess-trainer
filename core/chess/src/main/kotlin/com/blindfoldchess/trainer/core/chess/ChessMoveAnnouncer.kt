package com.blindfoldchess.trainer.core.chess

object ChessMoveAnnouncer {

    enum class Language { English, French }

    fun spoken(san: String, language: Language): String {
        val raw = san.trim()
        if (raw.isEmpty()) return ""
        val mate = raw.endsWith('#')
        val check = !mate && raw.endsWith('+')
        val body = raw.trimEnd('#', '+')
        val core = castle(body, language) ?: standard(body, language) ?: letterize(body)
        return buildString {
            append(core)
            when {
                mate -> append(' ').append(if (language == Language.French) "mat" else "mate")
                check -> append(' ').append(if (language == Language.French) "échec" else "check")
            }
        }.replace(Regex("\\s+"), " ").trim()
    }

    private fun castle(body: String, language: Language): String? = when (body) {
        "O-O", "0-0" -> if (language == Language.French) "petit roque" else "castle"
        "O-O-O", "0-0-0" -> if (language == Language.French) "grand roque" else "long castle"
        else -> null
    }

    private fun standard(body: String, language: Language): String? {
        val match = SAN.matchEntire(body) ?: return null
        val piece = match.groupValues[1].singleOrNull()
        val originFile = match.groupValues[2].singleOrNull()
        val originRank = match.groupValues[3].singleOrNull()
        val capture = match.groupValues[4] == "x"
        val dest = match.groupValues[5]
        val promotion = match.groupValues[6].singleOrNull()
        val destFile = dest[0]
        val destRank = dest[1].digitToInt()
        return buildString {
            if (piece != null) {
                append(pieceName(piece, language))
                append(' ')
            }
            if (originFile != null) {
                append(originFile.uppercaseChar())
                append(' ')
            }
            if (originRank != null) {
                append(rankWord(originRank.digitToInt(), language))
                append(' ')
            }
            if (capture) {
                append(if (language == Language.French) "prend" else "takes")
                append(' ')
            }
            append(destFile.uppercaseChar())
            append(' ')
            append(rankWord(destRank, language))
            if (promotion != null) {
                append(' ')
                append(pieceName(promotion, language))
            }
        }.trim()
    }

    private fun letterize(body: String): String =
        body.replace("", " ").trim()

    private fun pieceName(letter: Char, language: Language): String = when (letter.uppercaseChar()) {
        'K' -> if (language == Language.French) "roi" else "king"
        'Q' -> if (language == Language.French) "dame" else "queen"
        'R' -> if (language == Language.French) "tour" else "rook"
        'B' -> if (language == Language.French) "fou" else "bishop"
        'N' -> if (language == Language.French) "cavalier" else "knight"
        else -> letter.uppercaseChar().toString()
    }

    private fun rankWord(rank: Int, language: Language): String = when (language) {
        Language.English -> when (rank) {
            1 -> "one"
            2 -> "two"
            3 -> "three"
            4 -> "four"
            5 -> "five"
            6 -> "six"
            7 -> "seven"
            8 -> "eight"
            else -> rank.toString()
        }
        Language.French -> when (rank) {
            1 -> "un"
            2 -> "deux"
            3 -> "trois"
            4 -> "quatre"
            5 -> "cinq"
            6 -> "six"
            7 -> "sept"
            8 -> "huit"
            else -> rank.toString()
        }
    }

    private val SAN = Regex(
        """^([KQRBN])?([a-h])?([1-8])?(x)?([a-h][1-8])(?:=([QRBN]))?$""",
    )
}
