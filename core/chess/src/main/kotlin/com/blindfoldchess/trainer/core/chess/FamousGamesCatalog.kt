package com.blindfoldchess.trainer.core.chess

object FamousGamesCatalog {
    val games: List<FamousGame> = listOf(
        FamousGame(
            id = "opera",
            title = "The Opera Game",
            white = "Paul Morphy",
            black = "Duke of Brunswick & Count Isouard",
            event = "Paris Opera",
            year = 1858,
            result = "1-0",
            san = "e4 e5 Nf3 d6 d4 Bg4 dxe5 Bxf3 Qxf3 dxe5 Bc4 Nf6 Qb3 Qe7 Nc3 c6 " +
                "Bg5 b5 Nxb5 cxb5 Bxb5+ Nbd7 O-O-O Rd8 Rxd7 Rxd7 Rd1 Qe6 Bxd7+ Nxd7 " +
                "Qb8+ Nxb8 Rd8#",
        ),
        FamousGame(
            id = "legal",
            title = "Légal's Mate",
            white = "François de Légal",
            black = "Saint-Brie",
            event = "Paris",
            year = 1750,
            result = "1-0",
            san = "e4 e5 Nf3 d6 Bc4 Bg4 Nc3 g6 Nxe5 Bxd1 Bxf7+ Ke7 Nd5#",
        ),
        FamousGame(
            id = "reti-tartakower",
            title = "Réti's Mate",
            white = "Richard Réti",
            black = "Savielly Tartakower",
            event = "Vienna",
            year = 1910,
            result = "1-0",
            san = "e4 c6 d4 d5 Nc3 dxe4 Nxe4 Nf6 Qd3 e5 dxe5 Qa5+ Bd2 Qxe5 O-O-O Nxe4 " +
                "Qd8+ Kxd8 Bg5+ Kc7 Bd8#",
        ),
        FamousGame(
            id = "immortal",
            title = "The Immortal Game",
            white = "Adolf Anderssen",
            black = "Lionel Kieseritzky",
            event = "London",
            year = 1851,
            result = "1-0",
            san = "e4 e5 f4 exf4 Bc4 Qh4+ Kf1 b5 Bxb5 Nf6 Nf3 Qh6 d3 Nh5 Nh4 Qg5 Nf5 c6 " +
                "g4 Nf6 Rg1 cxb5 h4 Qg6 h5 Qg5 Qf3 Ng8 Bxf4 Qf6 Nc3 Bc5 Nd5 Qxb2 Bd6 Bxg1 " +
                "e5 Qxa1+ Ke2 Na6 Nxg7+ Kd8 Qf6+ Nxf6 Be7#",
        ),
        FamousGame(
            id = "evergreen",
            title = "The Evergreen Game",
            white = "Adolf Anderssen",
            black = "Jean Dufresne",
            event = "Berlin",
            year = 1852,
            result = "1-0",
            san = "e4 e5 Nf3 Nc6 Bc4 Bc5 b4 Bxb4 c3 Ba5 d4 exd4 O-O d3 Qb3 Qf6 e5 Qg6 " +
                "Re1 Nge7 Ba3 b5 Qxb5 Rb8 Qa4 Bb6 Nbd2 Bb7 Ne4 Qf5 Bxd3 Qh5 Nf6+ gxf6 " +
                "exf6 Rg8 Rad1 Qxf3 Rxe7+ Nxe7 Qxd7+ Kxd7 Bf5+ Ke8 Bd7+ Kf8 Bxe7#",
        ),
        FamousGame(
            id = "game-of-the-century",
            title = "Game of the Century",
            white = "Donald Byrne",
            black = "Bobby Fischer",
            event = "Rosenwald Memorial, New York",
            year = 1956,
            result = "0-1",
            san = "Nf3 Nf6 c4 g6 Nc3 Bg7 d4 O-O Bf4 d5 Qb3 dxc4 Qxc4 c6 e4 Nbd7 Rd1 Nb6 " +
                "Qc5 Bg4 Bg5 Na4 Qa3 Nxc3 bxc3 Nxe4 Bxe7 Qb6 Bc4 Nxc3 Bc5 Rfe8+ Kf1 Be6 " +
                "Bxb6 Bxc4+ Kg1 Ne2+ Kf1 Nxd4+ Kg1 Ne2+ Kf1 Nc3+ Kg1 axb6 Qb4 Ra4 Qxb6 " +
                "Nxd1 h3 Rxa2 Kh2 Nxf2 Re1 Rxe1 Qd8+ Bf8 Nxe1 Bd5 Nf3 Ne4 Qb8 b5 h4 h5 " +
                "Ne5 Kg7 Kg1 Bc5+ Kf1 Ng3+ Ke1 Bb4+ Kd1 Bb3+ Kc1 Ne2+ Kb1 Nc3+ Kc1 Rc2#",
        ),
    )

    fun byId(id: String): FamousGame =
        games.firstOrNull { it.id == id }
            ?: error("Unknown famous game: $id")
}
