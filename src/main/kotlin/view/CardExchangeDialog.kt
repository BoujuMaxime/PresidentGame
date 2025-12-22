package view

import javafx.animation.ScaleTransition
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.effect.DropShadow
import javafx.scene.layout.FlowPane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.util.Duration
import model.Card

/**
 * Dialog pour sélectionner les cartes à échanger.
 * 
 * @param cards La liste des cartes disponibles pour l'échange
 * @param count Le nombre de cartes à sélectionner
 * @param highest True si le joueur doit donner ses meilleures cartes
 * @param onConfirm Callback appelé avec les cartes sélectionnées
 * @param onCancel Callback appelé si l'échange est annulé (optionnel)
 * 
 * @author BOUJU Maxime
 */
class CardExchangeDialog(
    private val cards: List<Card>,
    private val count: Int,
    private val highest: Boolean,
    private val onConfirm: (List<Card>) -> Unit,
    private val onCancel: (() -> Unit)? = null
) : VBox() {
    
    private val selectedCards = mutableSetOf<Card>()
    private val cardButtons = mutableMapOf<Card, Button>()
    private val statusLabel: Label
    private val confirmButton: Button
    
    // Tailles de carte
    private val cardWidth = 70.0
    private val cardHeight = 95.0
    
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
        
        minWidth = 600.0
        maxWidth = 700.0
        
        // Icône
        val iconLabel = Label("🔄")
        iconLabel.font = Font.font("Georgia", FontWeight.BOLD, 48.0)
        iconLabel.style = "-fx-effect: dropshadow(gaussian, rgba(255, 215, 0, 0.5), 10, 0.0, 0, 0);"
        
        // Titre
        val title = Label("Échange de cartes")
        title.font = Font.font("Georgia", FontWeight.BOLD, 32.0)
        title.style = "-fx-text-fill: #ffd700; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.7), 4, 0.0, 0, 2);"
        
        // Instructions
        val instruction = if (highest) {
            "Sélectionnez vos $count meilleures cartes à donner"
        } else {
            "Sélectionnez $count cartes à donner"
        }
        val instructionLabel = Label(instruction)
        instructionLabel.font = Font.font("Georgia", FontWeight.NORMAL, 16.0)
        instructionLabel.style = "-fx-text-fill: #c8e6c9;"
        instructionLabel.isWrapText = true
        instructionLabel.maxWidth = 550.0
        
        // Status label
        statusLabel = Label(getStatusText())
        statusLabel.font = Font.font("Georgia", FontWeight.BOLD, 14.0)
        statusLabel.style = "-fx-text-fill: #ffd700;"
        
        // Panneau de cartes
        val cardsPane = FlowPane()
        cardsPane.hgap = 10.0
        cardsPane.vgap = 10.0
        cardsPane.alignment = Pos.CENTER
        cardsPane.padding = Insets(10.0)
        cardsPane.maxWidth = 600.0
        cardsPane.style = """
            -fx-background-color: rgba(0, 0, 0, 0.3);
            -fx-background-radius: 10;
            -fx-border-color: rgba(255, 255, 255, 0.2);
            -fx-border-width: 1;
            -fx-border-radius: 10;
        """.trimIndent()
        
        // Créer les boutons de cartes
        cards.forEach { card ->
            val cardButton = createCardButton(card)
            cardButtons[card] = cardButton
            cardsPane.children.add(cardButton)
        }
        
        // Boutons d'action
        val buttonBox = javafx.scene.layout.HBox(15.0)
        buttonBox.alignment = Pos.CENTER
        
        confirmButton = createStyledButton(
            "✓ Confirmer",
            "linear-gradient(to bottom, #2ecc71 0%, #27ae60 50%, #1e8449 100%)",
            "linear-gradient(to bottom, #58d68d 0%, #2ecc71 50%, #27ae60 100%)",
            "#1a5c36"
        ) { handleConfirm() }
        confirmButton.isDisable = true
        
        if (onCancel != null) {
            val cancelButton = createStyledButton(
                "✗ Annuler",
                "linear-gradient(to bottom, #e74c3c 0%, #c0392b 50%, #922b21 100%)",
                "linear-gradient(to bottom, #ec7063 0%, #e74c3c 50%, #c0392b 100%)",
                "#641e16"
            ) { onCancel.invoke() }
            buttonBox.children.addAll(confirmButton, cancelButton)
        } else {
            buttonBox.children.add(confirmButton)
        }
        
        // Ajouter tous les éléments
        children.addAll(iconLabel, title, instructionLabel, statusLabel, cardsPane, buttonBox)
    }
    
    private fun createCardButton(card: Card): Button {
        val button = Button()
        button.prefWidth = cardWidth
        button.prefHeight = cardHeight
        button.minWidth = cardWidth
        button.minHeight = cardHeight
        
        // Conteneur visuel de la carte
        val container = StackPane()
        container.prefWidth = cardWidth
        container.prefHeight = cardHeight
        container.style = """
            -fx-background-color: white;
            -fx-background-radius: 8;
            -fx-border-color: #333;
            -fx-border-width: 2;
            -fx-border-radius: 8;
        """.trimIndent()
        
        // Rang en haut-gauche
        val rankLabel = Label(card.rank.displayName)
        rankLabel.font = Font.font("Arial", FontWeight.BOLD, 16.0)
        rankLabel.style = "-fx-text-fill: ${getCardColor(card.suit)};"
        StackPane.setAlignment(rankLabel, Pos.TOP_LEFT)
        StackPane.setMargin(rankLabel, Insets(4.0, 0.0, 0.0, 6.0))
        
        // Symbole centré
        val suitLabel = Label(card.suit.icon)
        suitLabel.font = Font.font("Arial", FontWeight.BOLD, 32.0)
        suitLabel.style = "-fx-text-fill: ${getCardColor(card.suit)};"
        StackPane.setAlignment(suitLabel, Pos.CENTER)
        
        container.children.addAll(suitLabel, rankLabel)
        button.graphic = container
        button.style = "-fx-background-color: transparent; -fx-padding: 0;"
        
        // Ombre et effet hover
        val normalShadow = DropShadow(8.0, Color.web("#000000", 0.3))
        val hoverShadow = DropShadow(12.0, Color.web("#000000", 0.5))
        container.effect = normalShadow
        
        button.setOnMouseEntered {
            if (!selectedCards.contains(card)) {
                container.scaleX = 1.08
                container.scaleY = 1.08
                container.effect = hoverShadow
            }
        }
        button.setOnMouseExited {
            if (!selectedCards.contains(card)) {
                container.scaleX = 1.0
                container.scaleY = 1.0
                container.effect = normalShadow
            }
        }
        
        button.setOnAction {
            toggleCardSelection(card, container, normalShadow, hoverShadow)
        }
        
        return button
    }
    
    private fun toggleCardSelection(
        card: Card,
        container: StackPane,
        normalShadow: DropShadow,
        hoverShadow: DropShadow
    ) {
        if (selectedCards.contains(card)) {
            // Désélectionner
            selectedCards.remove(card)
            container.style = """
                -fx-background-color: white;
                -fx-background-radius: 8;
                -fx-border-color: #333;
                -fx-border-width: 2;
                -fx-border-radius: 8;
            """.trimIndent()
            container.scaleX = 1.0
            container.scaleY = 1.0
            container.effect = normalShadow
        } else {
            // Vérifier si on peut sélectionner plus de cartes
            if (selectedCards.size >= count) {
                // Animation de rejet
                val shake = ScaleTransition(Duration.millis(100.0), container)
                shake.fromX = 1.0
                shake.fromY = 1.0
                shake.toX = 1.1
                shake.toY = 1.1
                shake.cycleCount = 2
                shake.isAutoReverse = true
                shake.play()
                return
            }
            
            // Sélectionner
            selectedCards.add(card)
            container.style = """
                -fx-background-color: linear-gradient(to bottom, #fff9c4 0%, #fff59d 100%);
                -fx-background-radius: 8;
                -fx-border-color: #ffd700;
                -fx-border-width: 3;
                -fx-border-radius: 8;
            """.trimIndent()
            container.scaleX = 1.05
            container.scaleY = 1.05
            container.effect = hoverShadow
            
            // Animation de sélection
            val pulse = ScaleTransition(Duration.millis(150.0), container)
            pulse.fromX = 1.05
            pulse.fromY = 1.05
            pulse.toX = 1.12
            pulse.toY = 1.12
            pulse.cycleCount = 2
            pulse.isAutoReverse = true
            pulse.play()
        }
        
        updateStatus()
    }
    
    private fun updateStatus() {
        statusLabel.text = getStatusText()
        confirmButton.isDisable = selectedCards.size != count
    }
    
    private fun getStatusText(): String {
        val remaining = count - selectedCards.size
        return if (remaining > 0) {
            "Sélectionnez encore $remaining carte${if (remaining > 1) "s" else ""}"
        } else {
            "✓ Sélection complète ! Vous pouvez confirmer."
        }
    }
    
    private fun handleConfirm() {
        if (selectedCards.size == count) {
            onConfirm(selectedCards.toList())
        }
    }
    
    private fun getCardColor(suit: Card.Suit): String {
        return when (suit) {
            Card.Suit.HEARTS, Card.Suit.DIAMONDS -> "#d32f2f"
            Card.Suit.CLUBS, Card.Suit.SPADES -> "#212121"
        }
    }
    
    /**
     * Crée un bouton stylisé avec les couleurs fournies et les effets hover.
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
