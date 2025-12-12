package model.player.ai

import model.Card
import model.PlayerMove
import model.player.PlayerUtils

class RandomAi(
    id: String,
    hand: MutableList<Card>
) : Ai(id, hand) {
    override fun playTurn(
        pile: MutableList<Card>,
        discardPile: MutableList<Card>,
        lastPlayerMove: PlayerMove?,
        straightRank: Card.Rank?
    ): PlayerMove? {
        val possible = PlayerUtils.possiblePlays(hand, lastPlayerMove, straightRank)
        return AiUtils.chooseRandomPlay(possible)
    }

    /**
     * Permet de choisir des cartes à échanger avec un autre joueur.
     *
     * Ai random donne soit ses plus fortes cartes, soit ses plus faibles cartes.
     *
     * @param count Le nombre de cartes à échanger.
     * @param highest Si `true`, sélectionne les cartes les plus fortes, sinon il choisit.
     * @return La liste des cartes sélectionnées pour l'échange.
     */
    override fun exchangeCard(count: Int, highest: Boolean): List<Card> {
        val selectableCards = PlayerUtils.selectableCardsForExchange(hand, count, highest).toMutableList()
        selectableCards.sortBy { it.rank.ordinal }

        return if (highest) selectableCards.takeLast(count) else selectableCards.take(count)
    }
}