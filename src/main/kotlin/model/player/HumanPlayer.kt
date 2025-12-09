package model.player

import model.Card

class HumanPlayer(
    id: String,
    hand: MutableList<Card>
) : Player(id, hand) {
    override fun playTurn() {
        TODO("Not yet implemented")
    }
}