package view

import javafx.animation.FadeTransition
import javafx.animation.PauseTransition
import javafx.animation.ScaleTransition
import javafx.animation.SequentialTransition
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.util.Duration

/**
 * Composant de bulle de discussion pour afficher les actions des joueurs.
 * Affiche un message avec une animation d'apparition et de disparition.
 * 
 * @author BOUJU Maxime
 */
class SpeechBubble : StackPane() {
    
    private val messageLabel: Label
    private var currentAnimation: SequentialTransition? = null
    
    init {
        styleClass.add("speech-bubble")
        alignment = Pos.CENTER
        padding = Insets(8.0, 12.0, 8.0, 12.0)
        maxWidth = 160.0
        
        messageLabel = Label()
        messageLabel.styleClass.add("speech-bubble-text")
        messageLabel.isWrapText = true
        messageLabel.maxWidth = 140.0
        messageLabel.alignment = Pos.CENTER
        
        children.add(messageLabel)
        
        // Initialement invisible
        isVisible = false
        opacity = 0.0
    }
    
    /**
     * Affiche un message dans la bulle avec animation.
     * 
     * @param message Le texte à afficher
     * @param displayDuration La durée d'affichage avant disparition
     */
    fun showMessage(message: String, displayDuration: Duration) {
        // Annuler l'animation précédente si elle existe
        currentAnimation?.stop()
        
        messageLabel.text = message
        isVisible = true
        opacity = 0.0
        scaleX = 0.8
        scaleY = 0.8
        
        // Animation d'apparition
        val fadeIn = FadeTransition(Duration.millis(200.0), this)
        fadeIn.fromValue = 0.0
        fadeIn.toValue = 1.0
        
        val scaleIn = ScaleTransition(Duration.millis(200.0), this)
        scaleIn.fromX = 0.8
        scaleIn.fromY = 0.8
        scaleIn.toX = 1.0
        scaleIn.toY = 1.0
        
        // Pause d'affichage
        val pause = PauseTransition(displayDuration)
        
        // Animation de disparition
        val fadeOut = FadeTransition(Duration.millis(300.0), this)
        fadeOut.fromValue = 1.0
        fadeOut.toValue = 0.0
        
        val scaleOut = ScaleTransition(Duration.millis(300.0), this)
        scaleOut.fromX = 1.0
        scaleOut.fromY = 1.0
        scaleOut.toX = 0.9
        scaleOut.toY = 0.9
        
        // Combiner les animations de fade et scale pour l'apparition
        val appearTransition = javafx.animation.ParallelTransition(fadeIn, scaleIn)
        
        // Combiner les animations de fade et scale pour la disparition
        val disappearTransition = javafx.animation.ParallelTransition(fadeOut, scaleOut)
        disappearTransition.setOnFinished { isVisible = false }
        
        // Séquence complète : apparition -> pause -> disparition
        currentAnimation = SequentialTransition(appearTransition, pause, disappearTransition)
        currentAnimation?.play()
    }
    
    /**
     * Cache immédiatement la bulle sans animation.
     */
    fun hide() {
        currentAnimation?.stop()
        isVisible = false
        opacity = 0.0
    }
}
