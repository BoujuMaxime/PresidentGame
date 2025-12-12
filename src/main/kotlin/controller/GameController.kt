package controller

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
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
 *
 * Gère la communication entre le modèle ([Game]) et la vue (JavaFX).
 *
 * Fournit des propriétés observables pour la liaison UI et des méthodes
 * pour démarrer, exécuter et arrêter une partie, ainsi que pour notifier
 * les changements d'état et les tours des joueurs (humain et IA).
 *
 * @author BOUJU Maxime
 */
class GameController {

    /**
     * Nom du joueur actuellement actif, utilisé pour l'affichage dans la vue.
     */
    val currentPlayerProperty = SimpleStringProperty("")

    /**
     * État global de la partie (ex: "Configuration", "Partie en cours", "Partie terminée", "Erreur").
     */
    val gameStateProperty = SimpleStringProperty("Configuration")

    /**
     * Main du joueur humain exposée à la vue.
     */
    val humanPlayerHandProperty = SimpleObjectProperty<List<Card>>(emptyList())

    /**
     * Dernier coup joué dans la partie, utilisé pour l'affichage et la logique de jeu.
     */
    val lastPlayedMoveProperty = SimpleObjectProperty<PlayerMove?>(null)

    /**
     * Pile actuelle de cartes sur la table, exposée à la vue.
     */
    val currentPileProperty = SimpleObjectProperty<List<Card>>(emptyList())

    /**
     * Indique si le joueur humain peut jouer (activation des contrôles UI).
     */
    val canPlayProperty = SimpleBooleanProperty(false)

    /**
     * Rôle affiché du joueur humain (ex: "Président", "Neutre", etc.).
     */
    val playerRoleProperty = SimpleStringProperty("")

    /**
     * Liste d'informations simplifiées sur chaque joueur pour la vue.
     */
    val playersInfoProperty = SimpleObjectProperty<List<PlayerInfo>>(emptyList())

    /**
     * Message informatif à afficher dans l'interface utilisateur.
     */
    val gameMessageProperty = SimpleStringProperty("")

    /**
     * Instance du modèle de jeu. Peut être null si aucune partie n'est créée.
     */
    private var game: Game? = null

    /**
     * Référence au joueur humain (wrapper GUI). Null si non initialisé.
     */
    private var humanPlayer: GuiHumanPlayer? = null

    /**
     * Thread dans lequel la boucle de jeu est exécutée.
     */
    private var gameThread: Thread? = null

    /**
     * Indicateur si une partie est actuellement en cours.
     */
    private var isGameRunning = false

    /**
     * Représentation synthétique des informations nécessaires pour la vue par joueur.
     *
     * @property name Identifiant du joueur
     * @property role Rôle courant du joueur
     * @property cardCount Nombre de cartes restantes dans la main
     * @property isCurrentPlayer Vrai si ce joueur est le joueur local affiché comme actif
     */
    data class PlayerInfo(
        val name: String,
        val role: Player.Role,
        val cardCount: Int,
        val isCurrentPlayer: Boolean
    )

    /**
     * Crée et configure une nouvelle partie locale avec les paramètres fournis.
     *
     * Initialise l'instance [Game], crée le joueur humain ([GuiHumanPlayer]) et
     * ajoute les joueurs IA selon le niveau de difficulté choisi.
     *
     * @param nbPlayers Nombre total de joueurs (incluant le joueur humain)
     * @param withCarreMagique Active la règle "Carré Magique" si true
     * @param withTaGueule Active la règle "Ta Gueule" si true
     * @param aiDifficulty Niveau de difficulté des IA
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

        game = Game(gameParams, onPileUpdated = { updateCurrentPile(it) })

        humanPlayer = GuiHumanPlayer("Vous", mutableListOf(), this)
        game!!.players.add(humanPlayer!!)

        for (i in 1 until nbPlayers) {
            val aiPlayer = when (aiDifficulty) {
                Game.GameParameters.DifficultyLevel.EASY -> RandomAi("Bot $i", mutableListOf())
                Game.GameParameters.DifficultyLevel.MEDIUM -> EvaluateAi("Bot $i", mutableListOf())
                Game.GameParameters.DifficultyLevel.HARD -> EvaluateAi("Bot $i", mutableListOf())
            }
            val observableAi = ObservableAi(aiPlayer, this)
            game!!.players.add(observableAi)
        }

        updateGameState("Partie prête")
        updateGameMessage("Cliquez sur 'Démarrer la partie' pour commencer")
    }

    /**
     * Démarre l'exécution de la partie dans un thread séparé.
     *
     * La méthode vérifie qu'une partie est configurée et qu'aucune autre partie
     * n'est déjà en cours. Met à jour les propriétés UI en conséquence et gère
     * proprement les exceptions levées pendant l'exécution du jeu.
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
     * Arrête et nettoie la partie en cours.
     *
     * Interrompt le thread de jeu si nécessaire et réinitialise les références
     * internes pour permettre de démarrer une nouvelle partie proprement.
     */
    fun stopGame() {
        isGameRunning = false
        gameThread?.interrupt()
        gameThread = null
        game = null
        humanPlayer = null
    }

    /**
     * Met à jour l'état de la partie visible dans l'UI.
     *
     * @param state Nouveau texte d'état à afficher
     */
    fun updateGameState(state: String) {
        Platform.runLater { gameStateProperty.set(state) }
    }

    /**
     * Met à jour le message d'information principal affiché à l'utilisateur.
     *
     * @param message Texte à afficher
     */
    fun updateGameMessage(message: String) {
        Platform.runLater { gameMessageProperty.set(message) }
    }

    /**
     * Met à jour le nom du joueur actuellement actif pour l'affichage.
     *
     * @param playerName Nom à afficher comme joueur courant
     */
    fun updateCurrentPlayer(playerName: String) {
        Platform.runLater { currentPlayerProperty.set(playerName) }
    }

    /**
     * Met à jour la vue avec la main actuelle du joueur humain.
     *
     * @param hand Liste des cartes qui composent la main du joueur humain
     */
    fun updateHumanPlayerHand(hand: List<Card>) {
        Platform.runLater { humanPlayerHandProperty.set(hand.toList()) }
    }

    /**
     * Met à jour le dernier coup joué, utilisé pour la logique de suivi et l'affichage.
     *
     * @param move Le dernier coup joué ou null si aucune action précédente
     */
    fun updateLastPlayedMove(move: PlayerMove?) {
        Platform.runLater { lastPlayedMoveProperty.set(move) }
    }

    /**
     * Met à jour la pile de cartes visible sur la table.
     *
     * @param pile Liste des cartes actuellement dans la pile
     */
    fun updateCurrentPile(pile: List<Card>) {
        Platform.runLater { currentPileProperty.set(pile.toList()) }
    }

    /**
     * Active ou désactive les contrôles de jeu pour le joueur humain.
     *
     * @param canPlay True pour permettre au joueur de jouer, false sinon
     */
    fun setCanPlay(canPlay: Boolean) {
        Platform.runLater { canPlayProperty.set(canPlay) }
    }

    /**
     * Met à jour l'affichage du rôle du joueur humain.
     *
     * @param role Rôle courant du joueur humain
     */
    fun updatePlayerRole(role: Player.Role) {
        Platform.runLater { playerRoleProperty.set(role.displayName) }
    }

    /**
     * Reconstruit et publie la liste des informations des joueurs pour la vue.
     *
     * Récupère l'état courant des joueurs depuis le modèle et crée des instances
     * [PlayerInfo] qui sont ensuite expédiées sur le thread UI.
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
     * Notifie le contrôleur que c'est au tour du joueur humain et prépare l'UI.
     *
     * Met à disposition un [CompletableFuture] qui sera complété lorsque le joueur
     * humain soumettra son coup via l'interface.
     *
     * @param pile Pile courante sur laquelle jouer
     * @param discardPile Pile de défausse
     * @param lastPlayerMove Dernier coup joué par un autre joueur, ou null
     * @param straightRank Rang requis pour une suite (si applicable)
     * @return Un [CompletableFuture] complété avec le [PlayerMove] choisi ou null si passe
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
     * Met à jour l'UI pour indiquer qu'un joueur IA a joué et affiche son coup.
     *
     * @param playerName Nom de l'IA
     * @param move Coup joué par l'IA, ou null si elle passe
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
     * Soumet le coup choisi par le joueur humain au modèle de jeu.
     *
     * @param move Coup choisi ou null pour passer
     */
    fun submitPlayerMove(move: PlayerMove?) {
        humanPlayer?.submitMove(move)
    }

    /**
     * Retourne la liste des coups possibles pour le joueur humain.
     *
     * @return Liste des coups possibles, ou liste vide si aucun joueur humain n'est actif
     */
    fun getPossibleMoves(): List<PlayerMove> {
        return humanPlayer?.getPossibleMoves() ?: emptyList()
    }
}
