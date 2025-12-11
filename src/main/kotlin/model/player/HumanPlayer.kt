package model.player

import model.Card
import model.Play
import model.player.PlayerUtils

class HumanPlayer(
    id: String,
    hand: MutableList<Card>,
    val onTurn: ((List<Play>) -> Unit)? = null
) : Player(id, hand) {
    var selectedPlay: Play? = null

    private fun displayHand(possiblePlays: List<Play>) {
        PlayerUtils.printHand(hand)
        println("Coups possibles :")
        if (possiblePlays.isEmpty()) {
            println("  Aucun coup valide")
        } else {
            possiblePlays.forEachIndexed { index, play ->
                println("  $index -> $play")
            }
        }
    }

    override fun playTurn(
        pile: MutableList<Card>,
        discardPile: MutableList<Card>,
        lastPlay: Play?,
        straightRank: Card.Rank?
    ): Play? {
        val possiblePlays = PlayerUtils.possiblePlays(hand, lastPlay, pile, straightRank)
        onTurn?.invoke(possiblePlays)
        // Wait for selection
        while (selectedPlay == null) {
            Thread.sleep(100) // Simple polling, not ideal
        }
        val play = selectedPlay
        selectedPlay = null
        return play
    }

    override fun giveCardsToPlayer(cards: List<Card>) {
        hand.addAll(cards)
        PlayerUtils.sortHandByRank(hand)
    }
}