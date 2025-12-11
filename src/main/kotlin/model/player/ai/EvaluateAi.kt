package model.player.ai

import model.Card
import model.Play
import model.player.PlayerUtils
import model.player.ai.AiUtils

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
        val possible = PlayerUtils.possiblePlays(hand, lastPlay, pile, straightRank)
        return AiUtils.chooseLowestPlay(possible)
    }

    override fun giveCardsToPlayer(cards: List<Card>) {
        if (cards.isEmpty()) return
        hand.addAll(cards)
        PlayerUtils.sortHandByRank(hand)
    }
}