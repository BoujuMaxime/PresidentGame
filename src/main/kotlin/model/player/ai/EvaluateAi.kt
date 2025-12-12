package model.player.ai

import model.Card
import model.PlayerMove
import model.player.PlayerUtils

class EvaluateAi(
    id: String,
    hand: MutableList<Card>
) : Ai(id, hand) {
    private val fallback = RandomAi(id, hand)
    /**
     * Permet à l'IA de jouer son tour en choisissant le meilleur coup possible.
     *
     * @param pile La pile principale de cartes.
     * @param discardPile La pile de défausse.
     * @param lastPlayerMove Le dernier coup joué par un autre joueur, ou null si aucun.
     * @param straightRank Le rang de la séquence en cours, ou null si aucune séquence.
     * @return Le coup choisi par l'IA, ou null si l'IA passe son tour.
     */
    override fun playTurn(
        pile: MutableList<Card>,
        discardPile: MutableList<Card>,
        lastPlayerMove: PlayerMove?,
        straightRank: Card.Rank?
    ): PlayerMove? {
        val possible = PlayerUtils.possiblePlays(hand, lastPlayerMove, straightRank)
        return AiUtils.chooseLowestPlay(possible)
    }

    /**
     * Permet de choisir des cartes à échanger avec un autre joueur.
     *
     * @param count Le nombre de cartes à échanger.
     * @param highest Si `true`, sélectionne les cartes les plus fortes, sinon il choisit.
     * @return La liste des cartes sélectionnées pour l'échange.
     */
    override fun exchangeCard(count: Int, highest: Boolean): List<Card> {
        return fallback.exchangeCard(count, highest)
    }

}