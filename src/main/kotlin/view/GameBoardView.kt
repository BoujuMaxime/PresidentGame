package view

import controller.GameController
import controller.GuiHumanPlayer
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import model.Card
import model.PlayerMove
import model.player.PlayerUtils

/**
 * Vue du plateau de jeu.
 * Affiche la table de jeu, les cartes du joueur et permet l'interaction.
 * 
 * @author BOUJU Maxime
 */
class GameBoardView(private val controller: GameController) : BorderPane() {
    
    private val playerHandPane: FlowPane
    private val centerInfoPane: VBox
    private val playersInfoPane: VBox
    private val messageLabel: Label
    private val lastPlayedLabel: Label
    private val playButton: Button
    private val passButton: Button
    private val newGameButton: Button
    
    private val selectedCards = mutableSetOf<Card>()
    private val cardButtons = mutableMapOf<Card, Button>()
    
    init {
        style = "-fx-background-color: #1e272e;"
        padding = Insets(10.0)
        
        // Partie supérieure: informations des autres joueurs
        playersInfoPane = VBox(10.0)
        playersInfoPane.padding = Insets(10.0)
        playersInfoPane.alignment = Pos.TOP_CENTER
        playersInfoPane.style = "-fx-background-color: #2d3436; -fx-background-radius: 10;"
        
        // Centre: informations de la partie
        centerInfoPane = VBox(15.0)
        centerInfoPane.alignment = Pos.CENTER
        centerInfoPane.padding = Insets(20.0)
        
        messageLabel = Label("En attente...")
        messageLabel.font = Font.font("Arial", FontWeight.BOLD, 18.0)
        messageLabel.style = "-fx-text-fill: #dfe6e9;"
        messageLabel.isWrapText = true
        
        lastPlayedLabel = Label("Aucun coup joué")
        lastPlayedLabel.font = Font.font("Arial", FontWeight.NORMAL, 14.0)
        lastPlayedLabel.style = "-fx-text-fill: #b2bec3;"
        lastPlayedLabel.isWrapText = true
        
        centerInfoPane.children.addAll(messageLabel, lastPlayedLabel)
        
        // Partie inférieure: main du joueur et boutons
        val bottomPane = VBox(15.0)
        bottomPane.alignment = Pos.CENTER
        bottomPane.padding = Insets(10.0)
        
        playerHandPane = FlowPane()
        playerHandPane.hgap = 10.0
        playerHandPane.vgap = 10.0
        playerHandPane.alignment = Pos.CENTER
        playerHandPane.padding = Insets(15.0)
        playerHandPane.style = "-fx-background-color: #2d3436; -fx-background-radius: 10;"
        
        // Boutons d'action
        val buttonPane = HBox(15.0)
        buttonPane.alignment = Pos.CENTER
        buttonPane.padding = Insets(10.0)
        
        playButton = Button("Jouer les cartes sélectionnées")
        playButton.font = Font.font("Arial", FontWeight.BOLD, 14.0)
        playButton.style = """
            -fx-background-color: #00b894;
            -fx-text-fill: white;
            -fx-padding: 12 25 12 25;
            -fx-background-radius: 5;
            -fx-cursor: hand;
        """.trimIndent()
        playButton.isDisable = true
        playButton.setOnAction { handlePlayCards() }
        
        passButton = Button("Passer mon tour")
        passButton.font = Font.font("Arial", FontWeight.BOLD, 14.0)
        passButton.style = """
            -fx-background-color: #d63031;
            -fx-text-fill: white;
            -fx-padding: 12 25 12 25;
            -fx-background-radius: 5;
            -fx-cursor: hand;
        """.trimIndent()
        passButton.isDisable = true
        passButton.setOnAction { handlePass() }
        
        newGameButton = Button("Nouvelle partie")
        newGameButton.font = Font.font("Arial", FontWeight.BOLD, 14.0)
        newGameButton.style = """
            -fx-background-color: #0984e3;
            -fx-text-fill: white;
            -fx-padding: 12 25 12 25;
            -fx-background-radius: 5;
            -fx-cursor: hand;
        """.trimIndent()
        newGameButton.setOnAction { handleNewGame() }
        
        buttonPane.children.addAll(playButton, passButton, newGameButton)
        
        bottomPane.children.addAll(playerHandPane, buttonPane)
        
        // Assembler la vue
        top = playersInfoPane
        center = centerInfoPane
        bottom = bottomPane
        
        // Écouter les changements du contrôleur
        setupBindings()
    }
    
    /**
     * Configure les bindings avec le contrôleur.
     */
    private fun setupBindings() {
        controller.humanPlayerHandProperty.addListener { _, _, newHand ->
            updatePlayerHand(newHand)
        }
        
        controller.lastPlayedMoveProperty.addListener { _, _, newMove ->
            updateLastPlayedMove(newMove)
        }
        
        controller.gameMessageProperty.addListener { _, _, newMessage ->
            messageLabel.text = newMessage
        }
        
        controller.canPlayProperty.addListener { _, _, canPlay ->
            playButton.isDisable = !canPlay || selectedCards.isEmpty()
            passButton.isDisable = !canPlay
        }
        
        controller.playersInfoProperty.addListener { _, _, newPlayers ->
            updatePlayersInfo(newPlayers)
        }
    }
    
    /**
     * Met à jour l'affichage de la main du joueur.
     */
    private fun updatePlayerHand(hand: List<Card>) {
        playerHandPane.children.clear()
        cardButtons.clear()
        selectedCards.clear()
        
        if (hand.isEmpty()) {
            val emptyLabel = Label("Aucune carte")
            emptyLabel.style = "-fx-text-fill: #b2bec3; -fx-font-size: 14px;"
            playerHandPane.children.add(emptyLabel)
            return
        }
        
        hand.forEach { card ->
            val cardButton = createCardButton(card)
            cardButtons[card] = cardButton
            playerHandPane.children.add(cardButton)
        }
    }
    
    /**
     * Crée un bouton représentant une carte.
     */
    private fun createCardButton(card: Card): Button {
        val button = Button(formatCard(card))
        button.font = Font.font("Monospace", FontWeight.BOLD, 14.0)
        
        val cardColor = getCardColor(card.suit)
        button.style = """
            -fx-background-color: white;
            -fx-text-fill: $cardColor;
            -fx-padding: 15 20 15 20;
            -fx-background-radius: 8;
            -fx-border-color: #636e72;
            -fx-border-width: 2;
            -fx-border-radius: 8;
            -fx-cursor: hand;
        """.trimIndent()
        
        button.setOnAction {
            toggleCardSelection(card, button)
        }
        
        return button
    }
    
    /**
     * Formate l'affichage d'une carte.
     */
    private fun formatCard(card: Card): String {
        return "${card.rank.displayName}\n${card.suit.icon}"
    }
    
    /**
     * Retourne la couleur CSS pour une couleur de carte.
     */
    private fun getCardColor(suit: Card.Suit): String {
        return when (suit) {
            Card.Suit.HEARTS, Card.Suit.DIAMONDS -> "#d63031"
            Card.Suit.CLUBS, Card.Suit.SPADES -> "#2d3436"
        }
    }
    
    /**
     * Gère la sélection/désélection d'une carte.
     */
    private fun toggleCardSelection(card: Card, button: Button) {
        if (!controller.canPlayProperty.get()) return
        
        if (selectedCards.contains(card)) {
            selectedCards.remove(card)
            // Réappliquer le style par défaut
            val cardColor = getCardColor(card.suit)
            button.style = """
                -fx-background-color: white;
                -fx-text-fill: $cardColor;
                -fx-padding: 15 20 15 20;
                -fx-background-radius: 8;
                -fx-border-color: #636e72;
                -fx-border-width: 2;
                -fx-border-radius: 8;
                -fx-cursor: hand;
            """.trimIndent()
        } else {
            selectedCards.add(card)
            // Appliquer le style de sélection
            val cardColor = getCardColor(card.suit)
            button.style = """
                -fx-background-color: #74b9ff;
                -fx-text-fill: $cardColor;
                -fx-padding: 15 20 15 20;
                -fx-background-radius: 8;
                -fx-border-color: #0984e3;
                -fx-border-width: 3;
                -fx-border-radius: 8;
                -fx-cursor: hand;
            """.trimIndent()
        }
        
        playButton.isDisable = selectedCards.isEmpty() || !controller.canPlayProperty.get()
    }
    
    /**
     * Met à jour l'affichage du dernier coup joué.
     */
    private fun updateLastPlayedMove(move: PlayerMove?) {
        lastPlayedLabel.text = if (move != null) {
            "Dernier coup: $move"
        } else {
            "Aucun coup joué - Vous commencez"
        }
    }
    
    /**
     * Met à jour les informations des joueurs.
     */
    private fun updatePlayersInfo(players: List<GameController.PlayerInfo>) {
        playersInfoPane.children.clear()
        
        val titleLabel = Label("Joueurs")
        titleLabel.font = Font.font("Arial", FontWeight.BOLD, 16.0)
        titleLabel.style = "-fx-text-fill: #dfe6e9;"
        playersInfoPane.children.add(titleLabel)
        
        players.forEach { playerInfo ->
            val playerLabel = Label(
                "${playerInfo.name} - ${playerInfo.role.displayName} (${playerInfo.cardCount} cartes)"
            )
            playerLabel.font = Font.font("Arial", FontWeight.NORMAL, 13.0)
            playerLabel.style = if (playerInfo.isCurrentPlayer) {
                "-fx-text-fill: #00b894; -fx-font-weight: bold;"
            } else {
                "-fx-text-fill: #b2bec3;"
            }
            playersInfoPane.children.add(playerLabel)
        }
    }
    
    /**
     * Gère l'action de jouer les cartes sélectionnées.
     */
    private fun handlePlayCards() {
        if (selectedCards.isEmpty()) return
        
        val humanPlayer = controller.humanPlayerHandProperty.get()
        if (humanPlayer.isEmpty()) return
        
        // Trouver le coup correspondant aux cartes sélectionnées
        val sortedSelected = selectedCards.sortedBy { it.rank }
        
        // Déterminer le type de coup
        val playType = when (sortedSelected.size) {
            1 -> PlayerMove.PlayType.SINGLE
            2 -> PlayerMove.PlayType.PAIR
            3 -> PlayerMove.PlayType.THREE_OF_A_KIND
            4 -> PlayerMove.PlayType.FOUR_OF_A_KIND
            else -> {
                messageLabel.text = "Nombre de cartes invalide"
                return
            }
        }
        
        // Vérifier que toutes les cartes ont le même rang pour les combinaisons
        if (sortedSelected.size > 1 && !sortedSelected.all { it.rank == sortedSelected[0].rank }) {
            messageLabel.text = "Les cartes doivent avoir le même rang"
            return
        }
        
        try {
            val move = PlayerMove(sortedSelected, playType)
            submitMove(move)
        } catch (e: Exception) {
            messageLabel.text = "Coup invalide: ${e.message}"
        }
    }
    
    /**
     * Gère l'action de passer son tour.
     */
    private fun handlePass() {
        submitMove(null)
    }
    
    /**
     * Soumet le coup au contrôleur.
     */
    private fun submitMove(move: PlayerMove?) {
        selectedCards.clear()
        playButton.isDisable = true
        passButton.isDisable = true
        
        // Soumettre le move via le contrôleur
        controller.submitPlayerMove(move)
    }
    
    /**
     * Gère la demande de nouvelle partie.
     */
    private fun handleNewGame() {
        controller.stopGame()
        (scene?.window as? javafx.stage.Stage)?.let { stage ->
            // Retourner au menu
            val menuView = MenuView(controller) {
                stage.scene.root = GameBoardView(controller)
                controller.runGame()
            }
            stage.scene.root = menuView
        }
    }
}
