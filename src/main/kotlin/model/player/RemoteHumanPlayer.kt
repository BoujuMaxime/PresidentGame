package model.player

import model.Card

class RemoteHumanPlayer(
    hand: MutableList<Card>,
    id: String
) : Player(hand, id) {
    override fun playTurn() {
        TODO("Not yet implemented")
    }
}