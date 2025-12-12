package model.player.ai

import model.PlayerMove

object AiUtils {
    /** Choisit aléatoirement un PlayerMove dans la liste, ou null si la liste est vide. */
    fun chooseRandomPlay(playerMoves: List<PlayerMove>): PlayerMove? {
        if (playerMoves.isEmpty()) return null
        return playerMoves.shuffled().first()
    }

    /** Choisit le PlayerMove "le plus faible" — premier dans l'ordre fourni (PlayerUtils garantit tri). */
    fun chooseLowestPlay(playerMoves: List<PlayerMove>): PlayerMove? {
        return playerMoves.firstOrNull()
    }
}
