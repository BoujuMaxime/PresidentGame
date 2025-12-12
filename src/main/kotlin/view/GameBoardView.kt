package view

import controller.GameController
import javafx.animation.FadeTransition
import javafx.animation.ParallelTransition
import javafx.animation.TranslateTransition
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.effect.DropShadow
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.util.Duration
import model.Card
import model.PlayerMove

class GameBoardView(private val controller: GameController) : BorderPane() {

    private val playerHandPane: FlowPane
    private val centerPane: StackPane
    private val messageLabel: Label
    private val lastPlayedLabel: Label
    private val pileStack: StackPane
    private val playButton: Button
    private val passButton: Button
    private val newGameButton: Button
    
    // Panneaux pour les autres joueurs (autour du plateau)
    private val topPlayerPane: VBox
    private val leftPlayerPane: VBox
    private val rightPlayerPane: VBox

    private val selectedCards = mutableSetOf<Card>()
    private val cardButtons = mutableMapOf<Card, Button>()

    // Tailles réduites
    private val cardWidth = 90.0 * 0.75
    private val cardHeight = 120.0 * 0.75
    private val pileCardWidth = cardWidth * 1.2
    private val pileCardHeight = cardHeight * 1.2
    private val pileAnimationDuration = Duration.millis(280.0)
    private var lastPileSnapshot: List<Card> = emptyList()


    init {
        style = "-fx-background-color: #1e272e;"
        padding = Insets(15.0)

        // === Zone centrale (pile de cartes) ===
        pileStack = StackPane()
        pileStack.prefWidth = pileCardWidth * 3.5
        pileStack.prefHeight = pileCardHeight * 2.2
        pileStack.padding = Insets(15.0)
        pileStack.style = "-fx-background-color: rgba(0,0,0,0.3); -fx-background-radius: 15;"

        messageLabel = Label("En attente...")
        messageLabel.font = Font.font("Arial", FontWeight.BOLD, 16.0)
        messageLabel.style = "-fx-text-fill: #dfe6e9;"
        messageLabel.isWrapText = true

        lastPlayedLabel = Label("Aucun coup joué")
        lastPlayedLabel.font = Font.font("Arial", FontWeight.NORMAL, 13.0)
        lastPlayedLabel.style = "-fx-text-fill: #b2bec3;"
        lastPlayedLabel.isWrapText = true

        val centerInfo = VBox(10.0)
        centerInfo.alignment = Pos.CENTER
        centerInfo.children.addAll(messageLabel, lastPlayedLabel, pileStack)
        
        centerPane = StackPane(centerInfo)
        centerPane.alignment = Pos.CENTER

        // === Joueurs autour du plateau ===
        topPlayerPane = VBox(8.0)
        topPlayerPane.alignment = Pos.CENTER
        topPlayerPane.padding = Insets(10.0)
        topPlayerPane.style = "-fx-background-color: #2d3436; -fx-background-radius: 10;"
        topPlayerPane.minHeight = 80.0

        leftPlayerPane = VBox(8.0)
        leftPlayerPane.alignment = Pos.CENTER
        leftPlayerPane.padding = Insets(10.0)
        leftPlayerPane.style = "-fx-background-color: #2d3436; -fx-background-radius: 10;"
        leftPlayerPane.minWidth = 150.0

        rightPlayerPane = VBox(8.0)
        rightPlayerPane.alignment = Pos.CENTER
        rightPlayerPane.padding = Insets(10.0)
        rightPlayerPane.style = "-fx-background-color: #2d3436; -fx-background-radius: 10;"
        rightPlayerPane.minWidth = 150.0

        // === Main du joueur (en bas) ===
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
        
        val bottomPane = VBox(15.0)
        bottomPane.alignment = Pos.CENTER
        bottomPane.padding = Insets(10.0)
        bottomPane.children.addAll(playerHandPane, buttonPane)

        // === Disposition finale ===
        top = topPlayerPane
        left = leftPlayerPane
        center = centerPane
        right = rightPlayerPane
        bottom = bottomPane

        setupBindings()
        updateCurrentPile(emptyList())
    }

    private fun setupBindings() {
        controller.humanPlayerHandProperty.addListener { _, _, newHand ->
            updatePlayerHand(newHand)
        }

        controller.lastPlayedMoveProperty.addListener { _, _, newMove ->
            updateLastPlayedMove(newMove)
        }

        controller.currentPileProperty.addListener { _, _, newPile ->
            updateCurrentPile(newPile)
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

    private fun createPileCard(card: Card): StackPane {
        val container = StackPane()
        container.prefWidth = pileCardWidth
        container.prefHeight = pileCardHeight
        container.style = baseCardStyle(getCardColor(card.suit))

        val rankLabel = Label(card.rank.displayName)
        rankLabel.font = Font.font("Arial", FontWeight.BOLD, 11.0)
        rankLabel.style = "-fx-text-fill: ${getCardColor(card.suit)};"
        StackPane.setAlignment(rankLabel, Pos.TOP_LEFT)
        StackPane.setMargin(rankLabel, Insets(6.0, 0.0, 0.0, 7.0))

        val suitLabel = Label(card.suit.icon)
        suitLabel.font = Font.font("Monospace", FontWeight.BOLD, 22.0)
        suitLabel.style = "-fx-text-fill: ${getCardColor(card.suit)};"
        StackPane.setAlignment(suitLabel, Pos.CENTER)

        val shadow = DropShadow(8.0, Color.web("#000000", 0.28))
        container.effect = shadow

        container.children.addAll(suitLabel, rankLabel)
        return container
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

    private fun updateCurrentPile(pile: List<Card>) {
        val previousSize = lastPileSnapshot.size
        lastPileSnapshot = pile.toList()

        pileStack.children.clear()

        if (pile.isEmpty()) {
            val emptyLabel = Label("Pile vide")
            emptyLabel.style = "-fx-text-fill: #b2bec3;"
            pileStack.children.add(emptyLabel)
            return
        }

        val addedCount = (pile.size - previousSize).coerceAtLeast(0)
        val animateFromIndex = if (addedCount > 0) pile.size - addedCount else null

        pile.forEachIndexed { index, card ->
            val cardNode = createPileCard(card)
            cardNode.translateX = index * 14.0
            cardNode.translateY = -index * 2.0
            pileStack.children.add(cardNode)

            if (animateFromIndex != null && index >= animateFromIndex) {
                cardNode.opacity = 0.0
                val fade = FadeTransition(pileAnimationDuration, cardNode)
                fade.fromValue = 0.0
                fade.toValue = 1.0

                val slide = TranslateTransition(pileAnimationDuration, cardNode)
                slide.fromY = cardNode.translateY - 18.0
                slide.toY = cardNode.translateY

                ParallelTransition(fade, slide).play()
            }
        }
    }

    private fun updatePlayersInfo(players: List<GameController.PlayerInfo>) {
        topPlayerPane.children.clear()
        leftPlayerPane.children.clear()
        rightPlayerPane.children.clear()

        // Séparer le joueur humain des autres joueurs
        val humanPlayer = players.find { it.isCurrentPlayer }
        val otherPlayers = players.filter { !it.isCurrentPlayer }

        // Distribuer les autres joueurs autour du plateau
        when (otherPlayers.size) {
            1 -> {
                // 1 adversaire: en haut
                addPlayerToPane(topPlayerPane, otherPlayers[0])
            }
            2 -> {
                // 2 adversaires: en haut à gauche et en haut à droite
                addPlayerToPane(leftPlayerPane, otherPlayers[0])
                addPlayerToPane(rightPlayerPane, otherPlayers[1])
            }
            3 -> {
                // 3 adversaires: en haut, à gauche, à droite
                addPlayerToPane(topPlayerPane, otherPlayers[0])
                addPlayerToPane(leftPlayerPane, otherPlayers[1])
                addPlayerToPane(rightPlayerPane, otherPlayers[2])
            }
            else -> {
                // Plus de 3 adversaires: répartir autour
                val third = otherPlayers.size / 3
                val remainder = otherPlayers.size % 3
                
                var index = 0
                // Top players
                val topCount = third + if (remainder > 0) 1 else 0
                for (i in 0 until topCount) {
                    if (index < otherPlayers.size) {
                        addPlayerToPane(topPlayerPane, otherPlayers[index++])
                    }
                }
                
                // Left players
                val leftCount = third + if (remainder > 1) 1 else 0
                for (i in 0 until leftCount) {
                    if (index < otherPlayers.size) {
                        addPlayerToPane(leftPlayerPane, otherPlayers[index++])
                    }
                }
                
                // Right players (remaining)
                while (index < otherPlayers.size) {
                    addPlayerToPane(rightPlayerPane, otherPlayers[index++])
                }
            }
        }
    }
    
    private fun addPlayerToPane(pane: VBox, playerInfo: GameController.PlayerInfo) {
        val nameLabel = Label(playerInfo.name)
        nameLabel.font = Font.font("Arial", FontWeight.BOLD, 14.0)
        nameLabel.style = "-fx-text-fill: #dfe6e9;"
        
        val infoLabel = Label("${playerInfo.role.displayName}\n${playerInfo.cardCount} cartes")
        infoLabel.font = Font.font("Arial", FontWeight.NORMAL, 12.0)
        infoLabel.style = "-fx-text-fill: #b2bec3;"
        infoLabel.alignment = Pos.CENTER
        infoLabel.textAlignment = javafx.scene.text.TextAlignment.CENTER
        
        val playerBox = VBox(5.0)
        playerBox.alignment = Pos.CENTER
        playerBox.padding = Insets(8.0)
        playerBox.style = "-fx-background-color: rgba(0,0,0,0.2); -fx-background-radius: 8;"
        playerBox.children.addAll(nameLabel, infoLabel)
        
        pane.children.add(playerBox)
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
