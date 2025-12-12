package view

import javafx.animation.FadeTransition
import javafx.animation.TranslateTransition
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.util.Duration

/**
 * Menu in-game pour les interactions secondaires.
 * Permet d'accéder à des actions moins importantes sans encombrer le plateau de jeu.
 * 
 * @author BOUJU Maxime
 */
class InGameMenuView : VBox() {
    
    private val menuButtons = mutableListOf<Button>()
    
    init {
        styleClass.add("ig-menu-panel")
        spacing = 10.0
        padding = Insets(20.0)
        alignment = Pos.TOP_CENTER
        minWidth = 220.0
        
        // Titre du menu
        val titleLabel = Label("Menu")
        titleLabel.styleClass.add("ig-menu-title")
        children.add(titleLabel)
        
        // Initialement invisible et décalé vers la droite
        opacity = 0.0
        translateX = 50.0
        isVisible = false
    }
    
    /**
     * Ajoute un bouton au menu.
     * @param text Le texte du bouton
     * @param styleClasses Les classes de style à appliquer au bouton
     * @param action L'action à exécuter lors du clic
     */
    fun addMenuItem(text: String, styleClasses: List<String> = emptyList(), action: () -> Unit) {
        val button = Button(text)
        button.styleClass.add("ig-menu-button")
        styleClasses.forEach { button.styleClass.add(it) }
        button.maxWidth = Double.MAX_VALUE
        button.setOnAction { action() }
        
        menuButtons.add(button)
        children.add(button)
    }
    
    /**
     * Ouvre le menu avec une animation.
     */
    fun open() {
        if (isVisible) return
        
        isVisible = true
        
        // Animation de fondu et de glissement
        val fadeIn = FadeTransition(Duration.millis(200.0), this)
        fadeIn.fromValue = 0.0
        fadeIn.toValue = 1.0
        
        val slideIn = TranslateTransition(Duration.millis(200.0), this)
        slideIn.fromX = 50.0
        slideIn.toX = 0.0
        
        fadeIn.play()
        slideIn.play()
    }
    
    /**
     * Ferme le menu avec une animation.
     */
    fun close() {
        if (!isVisible) return
        
        // Animation de fondu et de glissement
        val fadeOut = FadeTransition(Duration.millis(200.0), this)
        fadeOut.fromValue = 1.0
        fadeOut.toValue = 0.0
        
        val slideOut = TranslateTransition(Duration.millis(200.0), this)
        slideOut.fromX = 0.0
        slideOut.toX = 50.0
        
        fadeOut.setOnFinished { isVisible = false }
        
        fadeOut.play()
        slideOut.play()
    }
    
    /**
     * Bascule l'état du menu (ouvert/fermé).
     */
    fun toggle() {
        if (isVisible) {
            close()
        } else {
            open()
        }
    }
}
