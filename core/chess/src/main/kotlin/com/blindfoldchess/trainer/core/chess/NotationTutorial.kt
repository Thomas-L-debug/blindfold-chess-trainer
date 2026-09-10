package com.blindfoldchess.trainer.core.chess

data class NotationExample(
    val san: String,
    val meaning: String,
)

data class NotationTutorialText(
    val title: String,
    val description: String,
    val intro: String,
    val squaresTitle: String,
    val squaresBody: String,
    val squaresTap: String,
    val piecesTitle: String,
    val piecesIntro: String,
    val king: String,
    val queen: String,
    val rook: String,
    val bishop: String,
    val knight: String,
    val pawn: String,
    val white: String,
    val black: String,
    val piecesKnightNote: String,
    val piecesPawnNote: String,
    val writeTitle: String,
    val writeIntro: String,
    val writePawnAndPiece: String,
    val writePawnAndPieceExamples: List<NotationExample>,
    val writeCapture: String,
    val writeCaptureExamples: List<NotationExample>,
    val writeCastle: String,
    val writeCastleExamples: List<NotationExample>,
    val writeCheck: String,
    val writeCheckExamples: List<NotationExample>,
    val writeDisambiguation: String,
    val writeDisambiguationExamples: List<NotationExample>,
    val readTitle: String,
    val readIntro: String,
    val readExamples: List<NotationExample>,
    val speakTitle: String,
    val speakIntro: String,
    val speakTipsTitle: String,
    val speakTips: List<String>,
    val footer: String,
    val listen: String,
)

object NotationTutorial {

    val spokenSans = listOf(
        "e4",
        "Nf3",
        "Nxf3",
        "exd5",
        "O-O",
        "O-O-O",
        "Qh4+",
        "e8=Q#",
        "Nbd2",
        "Raxe1",
    )

    fun text(language: ChessMoveAnnouncer.Language): NotationTutorialText = when (language) {
        ChessMoveAnnouncer.Language.English -> English
        ChessMoveAnnouncer.Language.French -> French
    }

    fun occupancyLabel(
        piece: OccupiedSquare,
        language: ChessMoveAnnouncer.Language,
        copy: NotationTutorialText = text(language),
    ): String {
        val man = when (piece.man) {
            ChessMan.KING -> copy.king
            ChessMan.QUEEN -> copy.queen
            ChessMan.ROOK -> copy.rook
            ChessMan.BISHOP -> copy.bishop
            ChessMan.KNIGHT -> copy.knight
            ChessMan.PAWN -> copy.pawn
        }
        val color = if (piece.isWhite) copy.white else copy.black
        return if (language == ChessMoveAnnouncer.Language.French) {
            "$man $color"
        } else {
            "$color $man"
        }
    }

    private val English = NotationTutorialText(
        title = "Notation",
        description = "Read, write, and speak chess moves",
        intro = "This app uses standard algebraic notation — the same writing as books and chess sites. A move can be typed, tapped on the board, or spoken.",
        squaresTitle = "Squares",
        squaresBody = "Files are the columns, a to h, from White’s left. Ranks are the rows, 1 to 8, from White’s side. A square is the file then the rank: a1, e4, h8. White’s near-left corner is a1; the near-right is h1.",
        squaresTap = "Show the board with Coordinates on, then tap a square to see and hear its name.",
        piecesTitle = "Piece letters",
        piecesIntro = "Each piece has a capital letter, except pawns.",
        king = "king",
        queen = "queen",
        rook = "rook",
        bishop = "bishop",
        knight = "knight",
        pawn = "pawn",
        white = "White",
        black = "Black",
        piecesKnightNote = "The knight is N, because K is already the king.",
        piecesPawnNote = "A pawn has no letter. A pawn move is only the destination square.",
        writeTitle = "Writing a move",
        writeIntro = "Write the piece letter (skip it for pawns), then the destination square.",
        writePawnAndPiece = "Pawns write the arrival square only. Other pieces put their letter first.",
        writePawnAndPieceExamples = listOf(
            NotationExample("e4", "pawn to e4"),
            NotationExample("Nf3", "knight to f3"),
            NotationExample("Bc4", "bishop to c4"),
        ),
        writeCapture = "A capture inserts x before the destination. A capturing pawn also keeps its starting file.",
        writeCaptureExamples = listOf(
            NotationExample("Nxe5", "knight takes e5"),
            NotationExample("exd5", "e-pawn takes d5"),
        ),
        writeCastle = "Castling is written with the letter O:",
        writeCastleExamples = listOf(
            NotationExample("O-O", "king side (short)"),
            NotationExample("O-O-O", "queen side (long)"),
        ),
        writeCheck = "Check is +, mate is #. Promotion is = then the new piece.",
        writeCheckExamples = listOf(
            NotationExample("Qh4+", "queen to h4, check"),
            NotationExample("e8=Q#", "pawn to e8, queen, mate"),
        ),
        writeDisambiguation = "If two identical pieces can go to the same square, add the starting file, or the rank, or both.",
        writeDisambiguationExamples = listOf(
            NotationExample("Nbd2", "knight on the b-file to d2"),
            NotationExample("R1a3", "rook on rank 1 to a3"),
            NotationExample("Raxe1", "rook on the a-file takes e1"),
        ),
        readTitle = "Reading a move",
        readIntro = "Read left to right: the piece, any extra origin, capture, then the destination. A move with no letter is a pawn.",
        readExamples = listOf(
            NotationExample("Nf3", "the knight goes to f3"),
            NotationExample("Bxe5", "the bishop captures on e5"),
            NotationExample("exd5", "the e-pawn captures on d5"),
            NotationExample("O-O", "castle king side"),
            NotationExample("Nbd2", "the knight that stood on b goes to d2"),
        ),
        speakTitle = "Speaking a move",
        speakIntro = "Say the piece’s full name, not the letter. Then the destination file as a letter, then the rank as a word. For a capture, say takes. Castling: castle or long castle.",
        speakTipsTitle = "What the app hears",
        speakTips = listOf(
            "knight F three — or only F three in Piece Path",
            "pawn takes F four",
            "castle, short castle, or petit rock",
            "long castle, or grand rock",
            "English speech often hears F as S — S5 is read as f5",
            "French speech often hears G as “j’ai” — j’ai un is read as g1",
        ),
        footer = "Practice squares in Name the Square. Practice full moves in Free Board or Play the Bot, with Speak.",
        listen = "Listen",
    )

    private val French = NotationTutorialText(
        title = "Notation",
        description = "Lire, écrire et dire les coups",
        intro = "L’app utilise la notation algébrique standard — la même écriture que dans les livres et sur les sites d’échecs. Un coup se tape, se pointe sur l’échiquier, ou se dicte.",
        squaresTitle = "Les cases",
        squaresBody = "Les colonnes vont de a à h, depuis la gauche des Blancs. Les rangées vont de 1 à 8, depuis le camp des Blancs. Une case s’écrit colonne puis rangée : a1, e4, h8. a1 est le coin bas-gauche des Blancs ; h1 est le coin bas-droit.",
        squaresTap = "Affiche l’échiquier avec les coordonnées, puis tape une case pour voir et entendre son nom.",
        piecesTitle = "Lettres des pièces",
        piecesIntro = "Chaque pièce a une lettre majuscule, sauf les pions.",
        king = "roi",
        queen = "dame",
        rook = "tour",
        bishop = "fou",
        knight = "cavalier",
        pawn = "pion",
        white = "blanc",
        black = "noir",
        piecesKnightNote = "Le cavalier s’écrit N, parce que K est déjà le roi.",
        piecesPawnNote = "Un pion n’a pas de lettre. Un coup de pion, c’est seulement la case d’arrivée.",
        writeTitle = "Écrire un coup",
        writeIntro = "On écrit la lettre de la pièce (on la saute pour un pion), puis la case d’arrivée.",
        writePawnAndPiece = "Le pion n’écrit que la case d’arrivée. Les autres pièces mettent leur lettre devant.",
        writePawnAndPieceExamples = listOf(
            NotationExample("e4", "pion en e4"),
            NotationExample("Nf3", "cavalier en f3"),
            NotationExample("Bc4", "fou en c4"),
        ),
        writeCapture = "Une prise s’écrit avec un x devant la case d’arrivée. Un pion qui prend garde aussi sa colonne de départ.",
        writeCaptureExamples = listOf(
            NotationExample("Nxe5", "cavalier prend e5"),
            NotationExample("exd5", "pion e prend d5"),
        ),
        writeCastle = "Le roque s’écrit avec la lettre O :",
        writeCastleExamples = listOf(
            NotationExample("O-O", "petit roque (côté roi)"),
            NotationExample("O-O-O", "grand roque (côté dame)"),
        ),
        writeCheck = "L’échec se marque +, le mat #. Une promotion s’écrit = puis la nouvelle pièce.",
        writeCheckExamples = listOf(
            NotationExample("Qh4+", "dame en h4, échec"),
            NotationExample("e8=Q#", "pion en e8, dame, mat"),
        ),
        writeDisambiguation = "Si deux pièces identiques peuvent aller sur la même case, on ajoute la colonne de départ, ou la rangée, ou les deux.",
        writeDisambiguationExamples = listOf(
            NotationExample("Nbd2", "cavalier de la colonne b vers d2"),
            NotationExample("R1a3", "tour de la rangée 1 vers a3"),
            NotationExample("Raxe1", "tour de la colonne a prend e1"),
        ),
        readTitle = "Lire un coup",
        readIntro = "On lit de gauche à droite : la pièce, l’origine s’il y en a une, la prise, puis l’arrivée. Un coup sans lettre est un pion.",
        readExamples = listOf(
            NotationExample("Nf3", "le cavalier va en f3"),
            NotationExample("Bxe5", "le fou prend en e5"),
            NotationExample("exd5", "le pion e prend en d5"),
            NotationExample("O-O", "petit roque"),
            NotationExample("Nbd2", "le cavalier qui était en b va en d2"),
        ),
        speakTitle = "Dire un coup à voix haute",
        speakIntro = "On dit le nom complet de la pièce, pas la lettre. Puis la colonne d’arrivée, puis le numéro en toutes lettres. Pour une prise : « prend ». Le roque : « petit roque » ou « grand roque ».",
        speakTipsTitle = "Ce que l’app entend",
        speakTips = listOf(
            "cavalier F trois — ou seulement F trois dans Piece Path",
            "pion prend F quatre",
            "petit roque, petit rock",
            "grand roque, grand rock",
            "La dictée française entend souvent G comme « j’ai » — j’ai un est lu g1",
            "La dictée anglaise entend souvent F comme S — S5 est lu f5",
        ),
        footer = "Pour les cases : Nommer la case. Pour les coups complets : Plateau libre ou Jouer contre le bot, avec Parler.",
        listen = "Écouter",
    )
}
