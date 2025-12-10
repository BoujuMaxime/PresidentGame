package model.player

import model.Card
import model.Play

/**
 * Interface des joueurs
 */
interface PlayerInterface {
    fun playTurn(pile: MutableList<Card>, discardPile: MutableList<Card>, lastPlay: Play): Play
}