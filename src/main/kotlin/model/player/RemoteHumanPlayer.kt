package model.player

import model.Card
import model.PlayerMove

class RemoteHumanPlayer(
    id: String,
    hand: MutableList<Card>
) : Player(id, hand) {
    override fun playTurn(
        pile: MutableList<Card>,
        discardPile: MutableList<Card>,
        lastPlayerMove: PlayerMove?,
        straightRank: Card.Rank?
    ): PlayerMove {
        TODO("Not yet implemented")
    }

    override fun giveCardsToPlayer(cards: List<Card>) {
        TODO("Not yet implemented")
    }
}