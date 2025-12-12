package model.player.ai

import model.Card
import model.PlayerMove
import model.player.PlayerUtils

class RandomAi(
    id: String,
    hand: MutableList<Card>
) : Ai(id, hand) {
    override fun playTurn(
        pile: MutableList<Card>,
        discardPile: MutableList<Card>,
        lastPlayerMove: PlayerMove?,
        straightRank: Card.Rank?
    ): PlayerMove? {
        val possible = PlayerUtils.possiblePlays(hand, lastPlayerMove, pile, straightRank)
        return AiUtils.chooseRandomPlay(possible)
    }

    override fun giveCardsToPlayer(cards: List<Card>) {
        hand.addAll(cards)
        PlayerUtils.sortHandByRank(hand)
    }
}