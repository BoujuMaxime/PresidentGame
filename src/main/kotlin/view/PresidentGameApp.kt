package view

import controller.GameController
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import model.Game
import view.themes.ThemeManager

/**
 * Application principale JavaFX pour le jeu du Président.
 *
 * Point d'entrée de l'interface graphique, gère la navigation entre vues
 * et l'application des thèmes CSS.
 *
 * @property stage Fenêtre principale
 * @property rootPane Conteneur racine des vues
 * @property controller Contrôleur du jeu
 * @property themeManager Gestionnaire de thèmes
 * @property gameBoardView Vue du plateau (null si menu affiché)
 *
 * @author BOUJU Maxime
 */
class PresidentGameApp : Application() {

    private lateinit var stage: Stage
    private lateinit var rootPane: StackPane
    private lateinit var controller: GameController
    private lateinit var themeManager: ThemeManager
    private var gameBoardView: GameBoardView? = null

    /**
     * Initialise et affiche l'application.
     *
     * @param primaryStage Fenêtre principale fournie par JavaFX
     */
    override fun start(primaryStage: Stage) {
        stage = primaryStage
        controller = GameController()
        themeManager = ThemeManager()
        rootPane = StackPane()

        val scene = Scene(rootPane, 1280.0, 800.0)
        themeManager.applyTheme(scene)

        stage.title = "🃏 Jeu du Président"
        stage.scene = scene
        stage.minWidth = 1024.0
        stage.minHeight = 700.0

        showMenu()
        stage.show()
    }

    /**
     * Affiche le menu principal de configuration.
     */
    fun showMenu() {
        gameBoardView = null
        rootPane.children.setAll(MenuView(
            onStartGame = { startGame(it) },
            onThemeChange = { changeTheme(it) },
            themeManager = themeManager
        ))
    }

    /**
     * Démarre une nouvelle partie avec la configuration donnée.
     *
     * @param config Configuration de la partie
     */
    private fun startGame(config: GameConfig) {
        controller.startNewGame(
            nbPlayers = config.playerCount,
            withCarreMagique = config.withCarreMagique,
            withTaGueule = config.withTaGueule,
            aiDifficulty = config.difficulty
        )

        gameBoardView = GameBoardView(controller, { showMenu() }, themeManager)

        controller.onRoundFinished = { gameBoardView?.showRoundFinishedDialog() }
        controller.onCardExchangeRequired = { count, highest -> gameBoardView?.showCardExchangeDialog(count, highest) }
        controller.onDealCards = { cardCounts, onComplete -> gameBoardView?.animateDealCards(cardCounts, onComplete) }

        rootPane.children.setAll(gameBoardView)
        controller.runGame()
    }

    /**
     * Change le thème visuel de l'application.
     *
     * @param themeName Identifiant du thème (casino, cartoon, dark, minimal)
     */
    private fun changeTheme(themeName: String) {
        themeManager.setTheme(themeName)
        themeManager.applyTheme(stage.scene)
    }

    /**
     * Arrête proprement l'application et le thread de jeu.
     */
    override fun stop() {
        controller.stopGame()
    }
}

/**
 * Configuration d'une partie de jeu.
 *
 * @property playerCount Nombre de joueurs (3 à 6)
 * @property difficulty Niveau de difficulté des IA
 * @property withCarreMagique Règle Carré Magique activée
 * @property withTaGueule Règle Ta Gueule activée
 *
 * @author BOUJU Maxime
 */
data class GameConfig(
    val playerCount: Int,
    val difficulty: Game.GameParameters.DifficultyLevel,
    val withCarreMagique: Boolean,
    val withTaGueule: Boolean
)
