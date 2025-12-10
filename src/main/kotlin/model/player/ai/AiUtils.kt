package model.player.ai

import model.Play

object AiUtils {
    /** Choisit aléatoirement un Play dans la liste, ou null si la liste est vide. */
    fun chooseRandomPlay(plays: List<Play>): Play? {
        if (plays.isEmpty()) return null
        return plays.shuffled().first()
    }

    /** Choisit le Play "le plus faible" — premier dans l'ordre fourni (PlayerUtils garantit tri). */
    fun chooseLowestPlay(plays: List<Play>): Play? {
        return plays.firstOrNull()
    }
}
