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
        return if (possible.isEmpty()) null else possible.first()
    }

    override fun giveCardsToPlayer(cards: List<Card>) {
        // Ajouter les cartes reçues à la main et trier
        hand.addAll(cards)
        PlayerUtils.sortHandByRank(hand)
    }
}