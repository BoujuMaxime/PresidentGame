package view

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.FontWeight

/**
 * Dialog affiché à la fin d'une manche pour proposer de continuer ou quitter.
 * 
 * @author BOUJU Maxime
 */
class RoundFinishedDialog(
    private val onNewRound: () -> Unit,
    private val onQuit: () -> Unit
) : VBox() {
    
    init {
        spacing = 20.0
        padding = Insets(30.0)
        alignment = Pos.CENTER
        style = """
            -fx-background-color: linear-gradient(to bottom, 
                rgba(45, 52, 54, 0.98) 0%, 
                rgba(30, 39, 46, 0.98) 100%);
            -fx-background-radius: 15;
            -fx-border-color: linear-gradient(to right, #00b894, #00d9a8);
            -fx-border-width: 3;
            -fx-border-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.8), 25, 0.0, 0, 5);
        """.trimIndent()
        
        minWidth = 450.0
        minHeight = 250.0
        
        // Icône de trophée
        val trophyLabel = Label("🏆")
        trophyLabel.font = Font.font("Arial", FontWeight.BOLD, 48.0)
        
        // Titre
        val title = Label("Manche terminée !")
        title.font = Font.font("Arial", FontWeight.BOLD, 32.0)
        title.style = "-fx-text-fill: #ffffff; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.6), 3, 0.0, 0, 2);"
        
        // Message
        val message = Label("Les rôles ont été attribués.")
        message.font = Font.font("Arial", FontWeight.NORMAL, 16.0)
        message.style = "-fx-text-fill: #e8f5e9;"
        
        val question = Label("Souhaitez-vous lancer une nouvelle manche ?")
        question.font = Font.font("Arial", FontWeight.NORMAL, 14.0)
        question.style = "-fx-text-fill: #b2bec3;"
        
        // Boutons
        val buttonBox = HBox(15.0)
        buttonBox.alignment = Pos.CENTER
        
        val newRoundButton = createStyledButton(
            "🔄 Nouvelle Manche",
            "linear-gradient(to bottom, #00d9a8 0%, #00b894 100%)",
            "linear-gradient(to bottom, #00f5c4 0%, #00d9a8 100%)"
        ) { onNewRound() }
        
        val quitButton = createStyledButton(
            "🚪 Quitter",
            "linear-gradient(to bottom, #ff7675 0%, #d63031 100%)",
            "linear-gradient(to bottom, #ff9999 0%, #ff7675 100%)"
        ) { onQuit() }
        
        buttonBox.children.addAll(newRoundButton, quitButton)
        
        // Ajouter tous les éléments
        children.addAll(trophyLabel, title, message, question, buttonBox)
    }
    
    /**
     * Crée un bouton stylisé avec les couleurs fournies et les effets hover.
     *
     * @param text Le texte du bouton
     * @param normalColor La couleur normale du bouton
     * @param hoverColor La couleur du bouton au survol
     * @param action L'action à exécuter lors du clic
     * @return Le bouton stylisé
     */
    private fun createStyledButton(
        text: String,
        normalColor: String,
        hoverColor: String,
        action: () -> Unit
    ): Button {
        val button = Button(text)
        button.font = Font.font("Arial", FontWeight.BOLD, 14.0)
        
        val normalStyle = """
            -fx-background-color: $normalColor;
            -fx-text-fill: white;
            -fx-padding: 15 35 15 35;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.4), 8, 0.0, 0, 3);
        """.trimIndent()
        
        val hoverStyle = """
            -fx-background-color: $hoverColor;
            -fx-text-fill: white;
            -fx-padding: 15 35 15 35;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.6), 12, 0.0, 0, 4);
        """.trimIndent()
        
        button.style = normalStyle
        button.setOnMouseEntered { button.style = hoverStyle }
        button.setOnMouseExited { button.style = normalStyle }
        button.setOnAction { action() }
        
        return button
    }
}
