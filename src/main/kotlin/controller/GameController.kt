package controller

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import model.Card
import model.Game
import model.PlayerMove
import model.player.Player
import model.player.ai.EvaluateAi
import model.player.ai.RandomAi
import java.util.concurrent.CompletableFuture

/**
 * Contrôleur principal pour gérer la logique du jeu Président.
 * Gère la communication entre le modèle (Game) et la vue (JavaFX).
 * 
 * @author BOUJU Maxime
 */
class GameController {
    
    // Properties observables pour la vue
    val currentPlayerProperty = SimpleStringProperty("")
    val gameStateProperty = SimpleStringProperty("Configuration")
    val humanPlayerHandProperty = SimpleObjectProperty<List<Card>>(emptyList())
    val lastPlayedMoveProperty = SimpleObjectProperty<PlayerMove?>(null)
    val currentPileProperty = SimpleObjectProperty<List<Card>>(emptyList())
    val canPlayProperty = SimpleBooleanProperty(false)
    val playerRoleProperty = SimpleStringProperty("")
    val playersInfoProperty = SimpleObjectProperty<List<PlayerInfo>>(emptyList())
    val gameMessageProperty = SimpleStringProperty("")
    
    private var game: Game? = null
    private var humanPlayer: GuiHumanPlayer? = null
    private var gameThread: Thread? = null
    private var isGameRunning = false
    
    data class PlayerInfo(
        val name: String,
        val role: Player.Role,
        val cardCount: Int,
        val isCurrentPlayer: Boolean
    )
    
    /**
     * Démarre une nouvelle partie avec les paramètres fournis.
     */
    fun startNewGame(
        nbPlayers: Int,
        withCarreMagique: Boolean,
        withTaGueule: Boolean,
        aiDifficulty: Game.GameParameters.DifficultyLevel
    ) {
        stopGame()
        
        val gameModeParams = Game.GameParameters.GameModeParameters(
            withStraight = withCarreMagique,
            withForcePlay = withTaGueule
        )
        
        val gameParams = Game.GameParameters(
            nbPlayers = nbPlayers,
            gameMode = Game.GameParameters.GameMode.LOCAL,
            aiDifficulty = aiDifficulty,
            consoleOutput = false,
            gameModeParameters = gameModeParams
        )
        
        game = Game(gameParams)
        
        // Créer le joueur humain
        humanPlayer = GuiHumanPlayer("Vous", mutableListOf(), this)
        game!!.players.add(humanPlayer!!)
        
        // Ajouter les joueurs IA
        for (i in 1 until nbPlayers) {
            val aiPlayer = when (aiDifficulty) {
                Game.GameParameters.DifficultyLevel.EASY -> RandomAi("Bot $i", mutableListOf())
                Game.GameParameters.DifficultyLevel.MEDIUM -> EvaluateAi("Bot $i", mutableListOf())
                Game.GameParameters.DifficultyLevel.HARD -> EvaluateAi("Bot $i", mutableListOf())
            }
            // Wrapper l'IA pour observer ses actions
            val observableAi = ObservableAi(aiPlayer, this)
            game!!.players.add(observableAi)
        }
        
        updateGameState("Partie prête")
        updateGameMessage("Cliquez sur 'Démarrer la partie' pour commencer")
    }
    
    /**
     * Lance la partie de jeu dans un thread séparé.
     */
    fun runGame() {
        if (game == null || isGameRunning) return
        
        isGameRunning = true
        updateGameState("Partie en cours")
        
        gameThread = Thread {
            try {
                game?.startGame()
                Platform.runLater {
                    updateGameState("Partie terminée")
                    updateGameMessage("La partie est terminée! Les rôles ont été attribués.")
                    updatePlayersInfo()
                }
            } catch (e: Exception) {
                Platform.runLater {
                    updateGameState("Erreur")
                    updateGameMessage("Erreur: ${e.message}")
                }
                e.printStackTrace()
            } finally {
                isGameRunning = false
            }
        }
        gameThread?.start()
    }
    
    /**
     * Arrête la partie en cours.
     * Note: L'interruption du thread est acceptable ici car le thread de jeu
     * est conçu pour gérer les interruptions et se terminer proprement.
     */
    fun stopGame() {
        isGameRunning = false
        gameThread?.interrupt()
        gameThread = null
        game = null
        humanPlayer = null
    }
    
    /**
     * Met à jour l'état de la partie.
     */
    fun updateGameState(state: String) {
        Platform.runLater { gameStateProperty.set(state) }
    }
    
    /**
     * Met à jour le message de jeu.
     */
    fun updateGameMessage(message: String) {
        Platform.runLater { gameMessageProperty.set(message) }
    }
    
    /**
     * Met à jour le joueur actuel.
     */
    fun updateCurrentPlayer(playerName: String) {
        Platform.runLater { currentPlayerProperty.set(playerName) }
    }
    
    /**
     * Met à jour la main du joueur humain.
     */
    fun updateHumanPlayerHand(hand: List<Card>) {
        Platform.runLater { humanPlayerHandProperty.set(hand.toList()) }
    }
    
    /**
     * Met à jour le dernier coup joué.
     */
    fun updateLastPlayedMove(move: PlayerMove?) {
        Platform.runLater { lastPlayedMoveProperty.set(move) }
    }
    
    /**
     * Met à jour la pile actuelle.
     */
    fun updateCurrentPile(pile: List<Card>) {
        Platform.runLater { currentPileProperty.set(pile.toList()) }
    }
    
    /**
     * Active ou désactive la possibilité de jouer.
     */
    fun setCanPlay(canPlay: Boolean) {
        Platform.runLater { canPlayProperty.set(canPlay) }
    }
    
    /**
     * Met à jour le rôle du joueur humain.
     */
    fun updatePlayerRole(role: Player.Role) {
        Platform.runLater { playerRoleProperty.set(role.displayName) }
    }
    
    /**
     * Met à jour les informations de tous les joueurs.
     */
    fun updatePlayersInfo() {
        val players = game?.players ?: return
        val currentPlayer = humanPlayer
        
        val infos = players.map { player ->
            PlayerInfo(
                name = player.id,
                role = player.role,
                cardCount = player.hand.size,
                isCurrentPlayer = player == currentPlayer
            )
        }
        
        Platform.runLater { playersInfoProperty.set(infos) }
    }
    
    /**
     * Indique au joueur humain que c'est son tour.
     */
    fun notifyHumanPlayerTurn(
        pile: MutableList<Card>,
        discardPile: MutableList<Card>,
        lastPlayerMove: PlayerMove?,
        straightRank: Card.Rank?
    ): CompletableFuture<PlayerMove?> {
        val future = CompletableFuture<PlayerMove?>()
        
        Platform.runLater {
            updateCurrentPlayer("À vous de jouer!")
            updateLastPlayedMove(lastPlayerMove)
            updateHumanPlayerHand(humanPlayer?.hand ?: emptyList())
            updatePlayerRole(humanPlayer?.role ?: Player.Role.NEUTRAL)
            updatePlayersInfo()
            setCanPlay(true)
            
            val message = if (lastPlayerMove != null) {
                "Dernier coup: ${lastPlayerMove}. Choisissez vos cartes ou passez."
            } else {
                "Vous commencez! Choisissez vos cartes."
            }
            updateGameMessage(message)
            
            humanPlayer?.setMoveFuture(future, pile, discardPile, lastPlayerMove, straightRank)
        }
        
        return future
    }
    
    /**
     * Notifie qu'un joueur IA joue.
     */
    fun notifyAiPlayerTurn(playerName: String, move: PlayerMove?) {
        Platform.runLater {
            updateCurrentPlayer(playerName)
            updateLastPlayedMove(move)
            updatePlayersInfo()
            
            val message = if (move != null) {
                "$playerName joue: $move"
            } else {
                "$playerName passe son tour"
            }
            updateGameMessage(message)
        }
    }
    
    /**
     * Soumet le coup du joueur humain.
     */
    fun submitPlayerMove(move: PlayerMove?) {
        humanPlayer?.submitMove(move)
    }
}
