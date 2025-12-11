package model.player.ai

import model.Card
import model.Play
import model.player.PlayerUtils

class EvaluateAi(
    id: String,
    hand: MutableList<Card>
) : Ai(id, hand) {
    override fun playTurn(
        pile: MutableList<Card>,
        discardPile: MutableList<Card>,
        lastPlay: Play?,
        straightRank: Card.Rank?
    ): Play? {
        TODO("Not yet implemented")
    }

    override fun giveCardsToPlayer(cards: List<Card>) {
        TODO("Not yet implemented")
    }
}