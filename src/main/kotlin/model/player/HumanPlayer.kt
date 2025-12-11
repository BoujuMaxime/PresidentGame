package model.player

import model.Card
import model.Play

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
        displayHand(possiblePlays)
        println("Sélectionnez l'indice du coup à jouer ou appuyez sur Entrée pour passer :")
        while (true) {
            val input = readlnOrNull()?.trim()
            if (input.isNullOrEmpty()) return null
            val index = input.toIntOrNull()
            if (index != null && index in possiblePlays.indices) {
                return possiblePlays[index]
            }
            println("Indice invalide, recommencez ou appuyez sur Entrée pour passer.")
        }
    }

    override fun giveCardsToPlayer(cards: List<Card>) {
        hand.addAll(cards)
        PlayerUtils.sortHandByRank(hand)
    }
}