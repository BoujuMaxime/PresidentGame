package model.player.ai

import model.Card
import model.Play

class MiniMaxAi(
    id: String,
    hand: MutableList<Card>
) : Ai(id, hand) {
    private val fallback = EvaluateAi(id, hand)

    override fun playTurn(
        pile: MutableList<Card>,
        discardPile: MutableList<Card>,
        lastPlay: Play?,
        straightRank: Card.Rank?
    ): Play? {
        // Pour l'instant, utiliser l'heuristique EvaluateAi
        return fallback.playTurn(pile, discardPile, lastPlay, straightRank)
    }

    override fun giveCardsToPlayer(cards: List<Card>) {
        fallback.giveCardsToPlayer(cards)
    }
}