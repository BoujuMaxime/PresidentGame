package view

import controller.GameController
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.effect.DropShadow
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import model.Card
import model.PlayerMove

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

    // Tailles réduites
    private val cardWidth = 90.0 * 0.75
    private val cardHeight = 120.0 * 0.75


    init {
        style = "-fx-background-color: #1e272e;"
        padding = Insets(10.0)

        playersInfoPane = VBox(10.0)
        playersInfoPane.padding = Insets(10.0)
        playersInfoPane.alignment = Pos.TOP_CENTER
        playersInfoPane.style = "-fx-background-color: #2d3436; -fx-background-radius: 10;"

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

        val bottomPane = VBox(15.0)
        bottomPane.alignment = Pos.CENTER
        bottomPane.padding = Insets(10.0)

        playerHandPane = FlowPane()
        playerHandPane.hgap = 12.0
        playerHandPane.vgap = 12.0
        playerHandPane.alignment = Pos.CENTER
        playerHandPane.padding = Insets(15.0)
        playerHandPane.style = "-fx-background-color: #2d3436; -fx-background-radius: 10;"

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

        top = playersInfoPane
        center = centerInfoPane
        bottom = bottomPane

        setupBindings()
    }

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

    private fun createCardButton(card: Card): Button {
        val button = Button()
        button.style = "-fx-background-color: transparent; -fx-padding: 0; -fx-cursor: hand;"
        button.prefWidth = cardWidth
        button.prefHeight = cardHeight
        button.minWidth = cardWidth
        button.minHeight = cardHeight

        // Conteneur visuel de la carte
        val container = StackPane()
        container.prefWidth = cardWidth
        container.prefHeight = cardHeight
        container.style = baseCardStyle(getCardColor(card.suit))

        // Rang en haut-gauche (police réduite)
        val rankLabel = Label(card.rank.displayName)
        rankLabel.font = Font.font("Arial", FontWeight.BOLD, 12.0)
        rankLabel.style = "-fx-text-fill: ${getCardColor(card.suit)};"
        StackPane.setAlignment(rankLabel, Pos.TOP_LEFT)
        StackPane.setMargin(rankLabel, Insets(6.0, 0.0, 0.0, 8.0))

        // Symbole centré, taille réduite
        val suitLabel = Label(card.suit.icon)
        suitLabel.font = Font.font("Monospace", FontWeight.BOLD, 28.0)
        suitLabel.style = "-fx-text-fill: ${getCardColor(card.suit)};"
        StackPane.setAlignment(suitLabel, Pos.CENTER)

        container.children.addAll(suitLabel, rankLabel)
        button.graphic = container

        // Ombre et effet hover (atténué)
        val hoverShadow = DropShadow(10.0, Color.web("#000000", 0.22))
        button.setOnMouseEntered {
            container.scaleX = 1.03
            container.scaleY = 1.03
            container.effect = hoverShadow
        }
        button.setOnMouseExited {
            container.scaleX = 1.0
            container.scaleY = 1.0
            container.effect = null
        }

        button.setOnAction {
            toggleCardSelection(card, button)
        }

        return button
    }

    private fun baseCardStyle(cardColor: String): String {
        return """
                     -fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #ffffff, #f4f6fa);
                     -fx-background-radius: 8;
                     -fx-border-radius: 8;
                     -fx-border-color: #c8d0d8;
                     -fx-border-width: 1.2;
                     -fx-padding: 6;
                 """.trimIndent()
    }

    private fun selectedCardStyle(): String {
        return """
                     -fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #d7f1ff, #74b9ff);
                     -fx-background-radius: 8;
                     -fx-border-radius: 8;
                     -fx-border-color: #0984e3;
                     -fx-border-width: 2.5;
                     -fx-padding: 6;
                 """.trimIndent()
    }

    private fun formatCard(card: Card): String {
        return "${card.rank.displayName}\n${card.suit.icon}"
    }

    private fun getCardColor(suit: Card.Suit): String {
        return when (suit) {
            Card.Suit.HEARTS, Card.Suit.DIAMONDS -> "#d63031"
            Card.Suit.CLUBS, Card.Suit.SPADES -> "#2d3436"
        }
    }

    private fun toggleCardSelection(card: Card, button: Button) {
        if (!controller.canPlayProperty.get()) return

        val container = button.graphic as? StackPane ?: return

        if (selectedCards.contains(card)) {
            selectedCards.remove(card)
            container.style = baseCardStyle(getCardColor(card.suit))
        } else {
            selectedCards.add(card)
            container.style = selectedCardStyle()
        }

        playButton.isDisable = selectedCards.isEmpty() || !controller.canPlayProperty.get()
    }

    private fun updateLastPlayedMove(move: PlayerMove?) {
        lastPlayedLabel.text = if (move != null) {
            "Dernier coup: $move"
        } else {
            "Aucun coup joué - Vous commencez"
        }
    }

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

    private fun handlePlayCards() {
        if (selectedCards.isEmpty()) return

        val humanPlayer = controller.humanPlayerHandProperty.get()
        if (humanPlayer.isEmpty()) return

        val sortedSelected = selectedCards.sortedBy { it.rank }

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

    private fun handlePass() {
        submitMove(null)
    }

    private fun submitMove(move: PlayerMove?) {
        selectedCards.clear()
        playButton.isDisable = true
        passButton.isDisable = true
        controller.submitPlayerMove(move)
    }

    private fun handleNewGame() {
        controller.stopGame()
        (scene?.window as? javafx.stage.Stage)?.let { stage ->
            val menuView = MenuView(controller) {
                stage.scene.root = GameBoardView(controller)
                controller.runGame()
            }
            stage.scene.root = menuView
        }
    }
}