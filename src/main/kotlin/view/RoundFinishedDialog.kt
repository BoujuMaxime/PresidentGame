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
                rgba(30, 74, 53, 0.98) 0%, 
                rgba(20, 50, 35, 0.98) 100%);
            -fx-background-radius: 18;
            -fx-border-color: linear-gradient(to bottom, #6b4c35 0%, #4a3728 50%, #3d2a1c 100%);
            -fx-border-width: 4;
            -fx-border-radius: 18;
            -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.8), 30, 0.0, 0, 8);
        """.trimIndent()
        
        minWidth = 480.0
        minHeight = 280.0
        
        // Icône de trophée
        val trophyLabel = Label("🏆")
        trophyLabel.font = Font.font("Georgia", FontWeight.BOLD, 56.0)
        trophyLabel.style = "-fx-effect: dropshadow(gaussian, rgba(255, 215, 0, 0.5), 10, 0.0, 0, 0);"
        
        // Titre
        val title = Label("Manche terminée !")
        title.font = Font.font("Georgia", FontWeight.BOLD, 36.0)
        title.style = "-fx-text-fill: #ffd700; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.7), 4, 0.0, 0, 2);"
        
        // Message
        val message = Label("Les rôles ont été attribués.")
        message.font = Font.font("Georgia", FontWeight.NORMAL, 16.0)
        message.style = "-fx-text-fill: #c8e6c9;"
        
        val question = Label("Souhaitez-vous lancer une nouvelle manche ?")
        question.font = Font.font("Georgia", FontWeight.NORMAL, 14.0)
        question.style = "-fx-text-fill: #a5d6a7;"
        
        // Boutons
        val buttonBox = HBox(15.0)
        buttonBox.alignment = Pos.CENTER
        
        val newRoundButton = createStyledButton(
            "🔄 Nouvelle Manche",
            "linear-gradient(to bottom, #2ecc71 0%, #27ae60 50%, #1e8449 100%)",
            "linear-gradient(to bottom, #58d68d 0%, #2ecc71 50%, #27ae60 100%)",
            "#1a5c36"
        ) { onNewRound() }
        
        val quitButton = createStyledButton(
            "🚪 Quitter",
            "linear-gradient(to bottom, #e74c3c 0%, #c0392b 50%, #922b21 100%)",
            "linear-gradient(to bottom, #ec7063 0%, #e74c3c 50%, #c0392b 100%)",
            "#641e16"
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
     * @param borderColor La couleur de la bordure
     * @param action L'action à exécuter lors du clic
     * @return Le bouton stylisé
     */
    private fun createStyledButton(
        text: String,
        normalColor: String,
        hoverColor: String,
        borderColor: String,
        action: () -> Unit
    ): Button {
        val button = Button(text)
        button.font = Font.font("Georgia", FontWeight.BOLD, 14.0)
        
        val normalStyle = """
            -fx-background-color: $normalColor;
            -fx-text-fill: white;
            -fx-padding: 15 35 15 35;
            -fx-background-radius: 10;
            -fx-border-color: $borderColor;
            -fx-border-width: 2;
            -fx-border-radius: 10;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 10, 0.0, 0, 4);
        """.trimIndent()
        
        val hoverStyle = """
            -fx-background-color: $hoverColor;
            -fx-text-fill: white;
            -fx-padding: 15 35 15 35;
            -fx-background-radius: 10;
            -fx-border-color: $borderColor;
            -fx-border-width: 2;
            -fx-border-radius: 10;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.7), 15, 0.0, 0, 5);
        """.trimIndent()
        
        button.style = normalStyle
        button.setOnMouseEntered { button.style = hoverStyle }
        button.setOnMouseExited { button.style = normalStyle }
        button.setOnAction { action() }
        
        return button
    }
}
