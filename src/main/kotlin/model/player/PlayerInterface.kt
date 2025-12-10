package model.player

import model.Card

/**
 * Interface des joueurs
 */
interface PlayerInterface {
    fun playTurn(cardPlayed: MutableList<Card>)
}