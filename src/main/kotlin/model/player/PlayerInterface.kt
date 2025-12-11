package model.player

import model.Card
import model.PlayerMove

/**
 * Interface des joueurs
 */
interface PlayerInterface {
    fun playTurn(
        pile: MutableList<Card>,
        discardPile: MutableList<Card>,
        lastPlayerMove: PlayerMove?,
        straightRank: Card.Rank?
    ): PlayerMove?

    fun giveCardsToPlayer(cards: List<Card>)
}