package model.player.ai

import model.Card

class RandomAi(
    hand: MutableList<Card>,
    id: String
) : Ai(hand, id) {
    override fun playTurn() {
        TODO("Not yet implemented")
    }
}