package controller

import model.Card
import model.PlayerMove
import model.player.Player
import model.player.PlayerUtils
import java.util.concurrent.CompletableFuture

/**
 * Joueur humain qui interagit avec l'interface graphique JavaFX.
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
    
    private var moveFuture: CompletableFuture<PlayerMove?>? = null
    private var currentPile: MutableList<Card>? = null
    private var currentDiscardPile: MutableList<Card>? = null
    private var currentLastPlayerMove: PlayerMove? = null
    private var currentStraightRank: Card.Rank? = null
    
    /**
     * Configure le future pour recevoir le coup du joueur.
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
     * Retourne les coups possibles avec la main actuelle.
     */
    fun getPossibleMoves(): List<PlayerMove> {
        return PlayerUtils.possiblePlays(hand, currentLastPlayerMove, currentStraightRank)
    }
    
    /**
     * Permet au joueur de jouer un coup sélectionné depuis l'interface.
     */
    fun submitMove(move: PlayerMove?) {
        moveFuture?.complete(move)
        moveFuture = null
        controller.setCanPlay(false)
    }
    
    /**
     * Joue le tour du joueur - attend que l'interface fournisse le coup.
     */
    override fun playTurn(
        pile: MutableList<Card>,
        discardPile: MutableList<Card>,
        lastPlayerMove: PlayerMove?,
        straightRank: Card.Rank?
    ): PlayerMove? {
        val future = controller.notifyHumanPlayerTurn(pile, discardPile, lastPlayerMove, straightRank)
        return future.get() // Bloque jusqu'à ce que le joueur fasse un choix dans l'interface
    }
    
    /**
     * Permet de choisir des cartes à échanger.
     */
    override fun exchangeCard(count: Int, highest: Boolean): List<Card> {
        if (count <= 0 || hand.isEmpty()) return emptyList()
        
        if (highest) {
            // Automatique: donner les meilleures cartes
            val picked = PlayerUtils.selectableCardsForExchange(hand, count, true).take(count)
            controller.updateGameMessage("Échange automatique: vous donnez vos $count meilleures cartes")
            return picked
        }
        
        // Pour simplifier, sélection automatique des pires cartes
        // TODO: Implémenter une sélection via l'interface si nécessaire
        val picked = hand.sortedBy { it.rank }.take(count)
        controller.updateGameMessage("Échange: vous donnez $count cartes")
        return picked
    }
}
