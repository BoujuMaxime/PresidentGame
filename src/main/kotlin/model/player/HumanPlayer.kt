package model.player

import model.Card
import model.Play

class HumanPlayer(
    id: String,
    hand: MutableList<Card>
) : Player(id, hand) {
    override fun playTurn(
        pile: MutableList<Card>,
        discardPile: MutableList<Card>,
        lastPlay: Play?,
        straightRank: Card.Rank?
    ): Play? {
        val possible = PlayerUtils.possiblePlays(hand, lastPlay, pile, straightRank)
        // System d'input externe requis pour un vrai joueur humain
        TODO("Not yet implemented")
    }

    override fun giveCardsToPlayer(cards: List<Card>) {
        TODO("Not yet implemented")
    }
}