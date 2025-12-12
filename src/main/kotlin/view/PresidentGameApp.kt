package view

import controller.GameController
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage

/**
 * Application JavaFX principale pour le jeu du Président.
 * Point d'entrée de l'interface graphique.
 * 
 * @author BOUJU Maxime
 */
class PresidentGameApp : Application() {
    
    private lateinit var controller: GameController
    
    override fun start(primaryStage: Stage) {
        controller = GameController()
        
        primaryStage.title = "Jeu du Président"
        
        // Créer la vue du menu
        val menuView = MenuView(controller) {
            showGameBoard(primaryStage)
        }
        
        val scene = Scene(menuView, 800.0, 600.0)
        primaryStage.scene = scene
        primaryStage.minWidth = 800.0
        primaryStage.minHeight = 600.0
        primaryStage.show()
    }
    
    /**
     * Affiche le plateau de jeu et démarre la partie.
     */
    private fun showGameBoard(stage: Stage) {
        val gameBoardView = GameBoardView(controller)
        stage.scene.root = gameBoardView
        stage.width = 1000.0
        stage.height = 700.0
        
        // Démarrer la partie dans un thread séparé
        controller.runGame()
    }
    
    override fun stop() {
        controller.stopGame()
        super.stop()
    }
}

/**
 * Point d'entrée pour lancer l'application JavaFX.
 */
fun main() {
    Application.launch(PresidentGameApp::class.java)
}
