package model.player.ai

import model.Card
import model.PlayerMove

class MiniMaxAi(
    id: String,
    hand: MutableList<Card>
) : Ai(id, hand) {
    private val fallback = EvaluateAi(id, hand)

    override fun playTurn(
        pile: MutableList<Card>,
        discardPile: MutableList<Card>,
        lastPlayerMove: PlayerMove?,
        straightRank: Card.Rank?
    ): PlayerMove? {
        return fallback.playTurn(pile, discardPile, lastPlayerMove, straightRank)
    }

    /**
     * Permet de choisir des cartes à échanger avec un autre joueur.
     *
     * @param count Le nombre de cartes à échanger.
     * @param highest Si `true`, sélectionne les cartes les plus fortes, sinon il choisit.
     * @return La liste des cartes sélectionnées pour l'échange.
     */
    override fun exchangeCard(count: Int, highest: Boolean): List<Card> {
        TODO("Not yet implemented")
    }
}