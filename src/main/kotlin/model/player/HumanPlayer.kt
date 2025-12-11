package model.player

import model.Card
import model.PlayerMove

class HumanPlayer(
    id: String,
    hand: MutableList<Card>
) : Player(id, hand) {
    private fun displayHand(possiblePlayerMoves: List<PlayerMove>) {
        PlayerUtils.printHand(hand)
        println("Coups possibles :")
        if (possiblePlayerMoves.isEmpty()) {
            println("  Aucun coup valide")
        } else {
            possiblePlayerMoves.forEachIndexed { index, play ->
                println("  $index -> $play")
            }
        }
    }

    override fun playTurn(
        pile: MutableList<Card>,
        discardPile: MutableList<Card>,
        lastPlayerMove: PlayerMove?,
        straightRank: Card.Rank?
    ): PlayerMove? {
        val possiblePlays = PlayerUtils.possiblePlays(hand, lastPlayerMove, pile, straightRank)
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