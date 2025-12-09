package model.player.ai

import model.Card

class RandomAi(
    id: String,
    hand: MutableList<Card>
) : Ai(id, hand) {
    override fun playTurn() {
        TODO("Not yet implemented")
    }
}