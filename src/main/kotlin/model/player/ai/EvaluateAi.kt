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
        lastPlay: Play?
    ): Play? {
        val possible = PlayerUtils.possiblePlays(hand, lastPlay)
        if (possible.isEmpty()) return null
        if (lastPlay == null) {
            // Commence : jouer le coup le plus faible
            return AiUtils.chooseLowestPlay(possible)
        }
        // Choisir le plus faible qui bat lastPlay (PlayerUtils.possiblePlays déjà filtre, donc on prend le plus faible)
        return AiUtils.chooseLowestPlay(possible)
    }

    override fun giveCardsToPlayer(cards: List<Card>) {
        hand.addAll(cards)
        PlayerUtils.sortHandByRank(hand)
    }

}