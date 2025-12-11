package model.player.ai

import model.Card
import model.PlayerMove

class MiniMaxAi(
    id: String,
    hand: MutableList<Card>
) : Ai(id, hand) {
    private val fallback = EvaluateAi(id, hand)

    override fun playTurn(
        pile: MutableList<Card>,
        discardPile: MutableList<Card>,
        lastPlayerMove: PlayerMove?,
        straightRank: Card.Rank?
    ): PlayerMove? {
        return fallback.playTurn(pile, discardPile, lastPlayerMove, straightRank)
    }

    override fun giveCardsToPlayer(cards: List<Card>) {
        fallback.giveCardsToPlayer(cards)
    }
}