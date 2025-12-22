package controller

import model.Card
import model.PlayerMove
import model.player.Player
import model.player.ai.Ai

/**
 * Wrapper pour les joueurs IA qui notifie le contrôleur de leurs actions.
 *
 * Cette classe permet de surveiller les actions des joueurs IA en les encapsulant
 * et en notifiant le contrôleur des événements importants, comme les tours joués
 * ou les échanges de cartes.
 *
 * @param wrappedAi L'IA à wrapper
 * @param controller Le contrôleur à notifier
 * @author BOUJU Maxime
 */
class ObservableAi(
    private val wrappedAi: Ai,
    private val controller: GameController
) : Player(wrappedAi.id, wrappedAi.hand) {

    init {
        // Initialise le rôle du joueur en le synchronisant avec l'IA encapsulée
        this.role = wrappedAi.role
    }

    /**
     * Joue le tour de l'IA encapsulée et notifie le contrôleur.
     *
     * Cette méthode appelle la logique de jeu de l'IA encapsulée pour déterminer
     * le coup à jouer, puis notifie le contrôleur de ce coup. Une pause est ajoutée
     * pour permettre à l'utilisateur de visualiser l'action.
     *
     * @param pile La pile actuelle de cartes sur la table
     * @param discardPile La pile de défausse
     * @param lastPlayerMove Le dernier coup joué par un autre joueur, ou null
     * @param straightRank Le rang requis pour une suite, ou null si non applicable
     * @return Le coup joué par l'IA, ou null si elle passe
     */
    override fun playTurn(
        pile: MutableList<Card>,
        discardPile: MutableList<Card>,
        lastPlayerMove: PlayerMove?,
        straightRank: Card.Rank?
    ): PlayerMove? {
        // Laisser l'IA jouer
        val move = wrappedAi.playTurn(pile, discardPile, lastPlayerMove, straightRank)

        // Notifier le contrôleur
        controller.notifyAiPlayerTurn(wrappedAi.id, move)

        // Pause pour visualiser le coup
        Thread.sleep(1000)

        return move
    }

    /**
     * Permet à l'IA d'échanger des cartes pendant la phase d'échange.
     *
     * Cette méthode délègue la logique d'échange à l'IA encapsulée.
     *
     * @param count Le nombre de cartes à échanger
     * @param highest True pour échanger les meilleures cartes, false pour les pires
     * @return La liste des cartes sélectionnées pour l'échange
     */
    override fun exchangeCard(count: Int, highest: Boolean): List<Card> {
        return wrappedAi.exchangeCard(count, highest)
    }
}