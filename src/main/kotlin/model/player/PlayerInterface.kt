package model.player

import model.Card
import model.PlayerMove

/**
 * Interface représentant un joueur dans le jeu.
 * @author BOUJU Maxime
 */
interface PlayerInterface {

    /**
     * Joue un tour pour le joueur.
     *
     * @param pile La pile principale de cartes disponibles pour jouer.
     * @param discardPile La pile de défausse contenant les cartes déjà jouées.
     * @param lastPlayerMove Le dernier mouvement effectué par un joueur, ou `null` s'il n'y en a pas.
     * @param straightRank Le rang de la séquence en cours, ou `null` si aucune séquence n'est active.
     * @return Le mouvement effectué par le joueur, ou `null` si aucun mouvement n'est possible.
     */
    fun playTurn(
        pile: MutableList<Card>,
        discardPile: MutableList<Card>,
        lastPlayerMove: PlayerMove?,
        straightRank: Card.Rank?
    ): PlayerMove?

    /**
     * Donne des cartes au joueur.
     *
     * @param cards La liste des cartes à donner au joueur.
     */
    fun giveCardsToPlayer(cards: List<Card>)
}