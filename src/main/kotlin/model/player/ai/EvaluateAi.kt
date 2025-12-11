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
        val possible = PlayerUtils.possiblePlays(hand, lastPlay, pile, straightRank)
        if (possible.isEmpty()) return null
        return if (lastPlay == null) {
            chooseStarterPlay(possible)
        } else {
            chooseResponsePlay(possible)
        }
    }

    override fun giveCardsToPlayer(cards: List<Card>) {
        hand.addAll(cards)
        PlayerUtils.sortHandByRank(hand)
    }

    private fun chooseStarterPlay(plays: List<Play>): Play {
        val combos = plays.filter { it.playType != Play.PlayType.SINGLE }
        return combos.maxWithOrNull(playStrengthComparator)
            ?: plays.maxWithOrNull(singleRankComparator)
            ?: plays.first()
    }

    private fun chooseResponsePlay(plays: List<Play>): Play {
        val combos = plays.filter { it.playType != Play.PlayType.SINGLE }
        return combos.maxWithOrNull(playStrengthComparator)
            ?: plays.maxWithOrNull(singleRankComparator)
            ?: plays.first()
    }

    private companion object {
        private val playStrengthComparator = compareByDescending<Play> { it.size }
            .thenComparing { it.getRank()}

        private val singleRankComparator = playStrengthComparator
    }
}