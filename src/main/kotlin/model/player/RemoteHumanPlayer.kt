package model.player

import model.Card
import model.Play

class RemoteHumanPlayer(
    id: String,
    hand: MutableList<Card>
) : Player(id, hand) {
    override fun playTurn(
        pile: MutableList<Card>,
        discardPile: MutableList<Card>,
        lastPlay: Play?
    ): Play? {
        // Stub non-bloquant : se comporte comme un HumanPlayer par défaut
        val possible = PlayerUtils.possiblePlays(hand, lastPlay, pile)
        return if (possible.isEmpty()) null else possible.first()
    }

    override fun giveCardsToPlayer(cards: List<Card>) {
        hand.addAll(cards)
        PlayerUtils.sortHandByRank(hand)
    }
}