package controller

import model.Card
import model.PlayerMove
import model.player.Player
import model.player.PlayerUtils
import java.util.concurrent.CompletableFuture

/**
 * Joueur humain qui interagit avec l'interface graphique JavaFX.
 *
 * Ce wrapper de [Player] permet de connecter la logique de jeu au contrôleur
 * UI ([GameController]) en exposant un mécanisme asynchrone pour récupérer
 * le coup choisi par l'utilisateur via l'interface.
 *
 * @param id L'identifiant unique du joueur.
 * @param hand La main initiale du joueur.
 * @param controller Le contrôleur pour communiquer avec la vue.
 * @author BOUJU Maxime
 */
class GuiHumanPlayer(
    id: String,
    hand: MutableList<Card>,
    private val controller: GameController
) : Player(id, hand) {

    /**
     * Future utilisé pour compléter le coup sélectionné par l'utilisateur.
     */
    private var moveFuture: CompletableFuture<PlayerMove?>? = null

    /**
     * Future utilisé pour compléter la sélection de cartes d'échange par l'utilisateur.
     */
    private var exchangeFuture: CompletableFuture<List<Card>>? = null

    /**
     * Référence à la pile courante sur la table lors du tour.
     */
    private var currentPile: MutableList<Card>? = null

    /**
     * Référence à la pile de défausse associée au tour.
     */
    private var currentDiscardPile: MutableList<Card>? = null

    /**
     * Dernier coup joué par un autre joueur, utilisé pour filtrer les coups possibles.
     */
    private var currentLastPlayerMove: PlayerMove? = null

    /**
     * Rang requis pour former une suite (si applicable) pendant le tour.
     */
    private var currentStraightRank: Card.Rank? = null

    /**
     * Configure le [CompletableFuture] qui sera complété lorsque l'utilisateur
     * soumettra son coup via l'interface.
     *
     * Cette méthode stocke également le contexte de jeu (piles, dernier coup, rang
     * de suite) afin que la vue puisse afficher correctement l'état et que
     * les méthodes utilitaires puissent calculer les coups possibles.
     *
     * @param future Future qui sera complété avec le [PlayerMove] choisi ou null si passe.
     * @param pile Pile courante sur laquelle jouer.
     * @param discardPile Pile de défausse associée.
     * @param lastPlayerMove Dernier coup joué, ou null s'il n'y en a pas.
     * @param straightRank Rang requis pour une suite, ou null si non applicable.
     */
    fun setMoveFuture(
        future: CompletableFuture<PlayerMove?>,
        pile: MutableList<Card>,
        discardPile: MutableList<Card>,
        lastPlayerMove: PlayerMove?,
        straightRank: Card.Rank?
    ) {
        this.moveFuture = future
        this.currentPile = pile
        this.currentDiscardPile = discardPile
        this.currentLastPlayerMove = lastPlayerMove
        this.currentStraightRank = straightRank
    }

    /**
     * Calcule et retourne la liste des coups possibles avec la main actuelle
     * en tenant compte du dernier coup joué et du rang pour une suite.
     *
     * Utilise [PlayerUtils.possiblePlays] pour déterminer les options valides.
     *
     * @return Liste des [PlayerMove] possibles.
     */
    fun getPossibleMoves(): List<PlayerMove> {
        return PlayerUtils.possiblePlays(hand, currentLastPlayerMove, currentStraightRank)
    }

    /**
     * Complète le future avec le coup sélectionné par l'utilisateur.
     *
     * Cette méthode est appelée par la couche UI lorsque l'utilisateur confirme
     * son choix (ou passe). Elle réinitialise ensuite le future local et désactive
     * la possibilité de jouer côté contrôleur.
     *
     * @param move Coup choisi par l'utilisateur ou null pour passer.
     */
    fun submitMove(move: PlayerMove?) {
        moveFuture?.complete(move)
        moveFuture = null
        controller.setCanPlay(false)
    }

    /**
     * Implémente le tour du joueur humain en bloquant jusqu'à ce que l'interface
     * fournisse la décision via le [CompletableFuture] retourné par le contrôleur.
     *
     * @param pile Pile courante sur laquelle jouer.
     * @param discardPile Pile de défausse.
     * @param lastPlayerMove Dernier coup joué par un autre joueur, ou null.
     * @param straightRank Rang requis pour une suite, ou null si non applicable.
     * @return Le [PlayerMove] choisi par l'utilisateur, ou null si passe.
     */
    override fun playTurn(
        pile: MutableList<Card>,
        discardPile: MutableList<Card>,
        lastPlayerMove: PlayerMove?,
        straightRank: Card.Rank?
    ): PlayerMove? {
        val future = controller.notifyHumanPlayerTurn(pile, discardPile, lastPlayerMove, straightRank)
        return future.get()
    }

    /**
     * Trie la main du joueur par rang des cartes et met à jour l'interface utilisateur.
     */
    fun sortHand() {
        PlayerUtils.sortHandByRank(hand)
        controller.updateHumanPlayerHand()
    }

    /**
     * Permet de choisir des cartes à échanger lors de la phase d'échange.
     *
     * Comportement :
     * - Si [count] <= 0 ou la main est vide, retourne une liste vide.
     * - Demande à l'utilisateur de sélectionner [count] cartes via l'interface graphique.
     * - Si [highest] est true, le joueur doit donner ses meilleures cartes.
     * - Sinon, le joueur peut donner les cartes de son choix (généralement les plus faibles).
     *
     * @param count Nombre de cartes à échanger.
     * @param highest True pour donner les meilleures cartes, false pour donner les pires.
     * @return Liste des cartes sélectionnées pour l'échange.
     */
    override fun exchangeCard(count: Int, highest: Boolean): List<Card> {
        if (count <= 0 || hand.isEmpty()) return emptyList()

        val future = controller.notifyCardExchange(count, highest)
        return future.get()
    }

    /**
     * Configure le [CompletableFuture] qui sera complété lorsque l'utilisateur
     * sélectionnera les cartes à échanger via l'interface.
     *
     * @param future Future qui sera complété avec la liste des cartes sélectionnées.
     */
    fun setExchangeFuture(future: CompletableFuture<List<Card>>) {
        this.exchangeFuture = future
    }

    /**
     * Complète le future avec les cartes sélectionnées par l'utilisateur pour l'échange.
     *
     * Cette méthode est appelée par la couche UI lorsque l'utilisateur confirme
     * sa sélection de cartes à échanger.
     *
     * @param cards Liste des cartes sélectionnées pour l'échange.
     */
    fun submitExchangeCards(cards: List<Card>) {
        exchangeFuture?.complete(cards)
        exchangeFuture = null
    }
}