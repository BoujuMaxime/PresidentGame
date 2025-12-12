import javafx.application.Application
import view.PresidentGameApp

/**
 * Point d'entrée principal pour lancer le jeu du Président avec interface JavaFX.
 */
fun main(args: Array<String>) {
    Application.launch(PresidentGameApp::class.java, *args)
}