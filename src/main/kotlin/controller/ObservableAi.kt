package controller

import model.Card
import model.PlayerMove
import model.player.Player
import model.player.ai.Ai

/**
 * Wrapper pour les joueurs IA qui notifie le contrôleur de leurs actions.
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
        this.role = wrappedAi.role
    }
    
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
        
        // Petite pause pour que l'utilisateur puisse voir le coup
        // Note: Thread.sleep est utilisé ici car nous sommes déjà dans un thread de jeu séparé
        // et nous voulons bloquer ce thread pour ralentir le rythme du jeu
        Thread.sleep(1000)
        
        return move
    }
    
    override fun exchangeCard(count: Int, highest: Boolean): List<Card> {
        return wrappedAi.exchangeCard(count, highest)
    }
}
