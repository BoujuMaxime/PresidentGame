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
    private val sortButton: Button
    private val menuButton: Button
    private val inGameMenu: InGameMenuView
    
    // Panneaux pour les autres joueurs (autour du plateau)
    private val topPlayerPane: HBox
    private val leftPlayerPane: VBox
    private val rightPlayerPane: VBox

    private val selectedCards = mutableSetOf<Card>()
    private val cardButtons = mutableMapOf<Card, Button>()

    // Dialog de fin de manche
    private var roundFinishedDialog: RoundFinishedDialog? = null
    private var dialogOverlay: StackPane? = null

    // Tailles réduites
    private val cardWidth = 90.0 * 0.75
    private val cardHeight = 120.0 * 0.75
    private val pileCardWidth = cardWidth * 1.2
    private val pileCardHeight = cardHeight * 1.2
    private val pileAnimationDuration = Duration.millis(280.0)
    private var lastPileSnapshot: List<Card> = emptyList()


    init {
        // Charger le fichier CSS
        val cssResource = javaClass.getResource("/game-board.css")
        if (cssResource != null) {
            stylesheets.add(cssResource.toExternalForm())
        }
        
        styleClass.add("game-board")
        padding = Insets(15.0)

        // === Zone centrale (pile de cartes) ===
        pileStack = StackPane()
        pileStack.styleClass.add("pile-stack")
        pileStack.prefWidth = pileCardWidth * 3.5
        pileStack.prefHeight = pileCardHeight * 2.2
        pileStack.padding = Insets(15.0)

        messageLabel = Label("En attente...")
        messageLabel.styleClass.add("message-label")
        messageLabel.isWrapText = true

        lastPlayedLabel = Label("Aucun coup joué")
        lastPlayedLabel.styleClass.add("last-played-label")
        lastPlayedLabel.isWrapText = true

        val centerInfo = VBox(10.0)
        centerInfo.alignment = Pos.CENTER
        centerInfo.children.addAll(messageLabel, lastPlayedLabel, pileStack)
        
        centerPane = StackPane(centerInfo)
        centerPane.styleClass.add("center-pane")
        centerPane.alignment = Pos.CENTER

        // === Joueurs autour du plateau ===
        topPlayerPane = HBox(8.0)
        topPlayerPane.styleClass.add("opponent-pane")
        topPlayerPane.alignment = Pos.CENTER
        topPlayerPane.padding = Insets(10.0)
        topPlayerPane.minHeight = 80.0

        leftPlayerPane = VBox(8.0)
        leftPlayerPane.styleClass.add("opponent-pane")
        leftPlayerPane.alignment = Pos.CENTER
        leftPlayerPane.padding = Insets(10.0)
        leftPlayerPane.minWidth = 150.0

        rightPlayerPane = VBox(8.0)
        rightPlayerPane.styleClass.add("opponent-pane")
        rightPlayerPane.alignment = Pos.CENTER
        rightPlayerPane.padding = Insets(10.0)
        rightPlayerPane.minWidth = 150.0

        // === Main du joueur (en bas) ===
        playerHandPane = FlowPane()
        playerHandPane.styleClass.add("player-pane")
        playerHandPane.hgap = 12.0
        playerHandPane.vgap = 12.0
        playerHandPane.alignment = Pos.CENTER
        playerHandPane.padding = Insets(15.0)

        // Boutons d'action principaux (jouer/passer)
        val actionButtonPane = HBox(15.0)
        actionButtonPane.styleClass.add("button-pane")
        actionButtonPane.alignment = Pos.CENTER
        actionButtonPane.padding = Insets(10.0)

        playButton = Button("Jouer")
        playButton.styleClass.addAll("action-button", "play-button")
        playButton.isDisable = true
        playButton.setOnAction { handlePlayCards() }

        passButton = Button("Passer")
        passButton.styleClass.addAll("action-button", "pass-button")
        passButton.isDisable = true
        passButton.setOnAction { handlePass() }

        sortButton = Button("Trier")
        sortButton.styleClass.addAll("action-button", "sort-button")
        sortButton.setOnAction { handleSortHand() }

        actionButtonPane.children.addAll(playButton, passButton, sortButton)
        
        val bottomPane = VBox(15.0)
        bottomPane.alignment = Pos.CENTER
        bottomPane.padding = Insets(10.0)
        bottomPane.children.addAll(playerHandPane, actionButtonPane)

        // === Menu in-game ===
        inGameMenu = InGameMenuView()
        
        // Ajouter le bouton "Nouvelle partie" dans le menu
        inGameMenu.addMenuItem("⟲ Nouvelle partie", listOf("new-game-button")) {
            handleNewGame()
            inGameMenu.close()
        }
        
        // Bouton pour ouvrir le menu
        menuButton = Button("☰")
        menuButton.styleClass.addAll("action-button", "menu-button")
        menuButton.setOnAction { inGameMenu.toggle() }
        
        // Conteneur pour le bouton de menu et le menu lui-même (coin supérieur droit)
        val menuContainer = StackPane()
        menuContainer.alignment = Pos.TOP_RIGHT
        menuContainer.padding = Insets(10.0)
        
        val menuBox = VBox(10.0)
        menuBox.alignment = Pos.TOP_RIGHT
        menuBox.children.addAll(menuButton, inGameMenu)
        
        menuContainer.children.add(menuBox)

        // === Disposition finale ===
        top = topPlayerPane
        left = leftPlayerPane
        right = rightPlayerPane
        bottom = bottomPane
        
        // Ajouter le menu par-dessus tout dans un StackPane au centre
        val rootStack = StackPane()
        rootStack.children.addAll(centerPane, menuContainer)
        center = rootStack

        setupBindings()
        updateCurrentPile(emptyList())
        
        // Configurer le callback de fin de manche
        controller.onRoundFinished = {
            showRoundFinishedDialog()
        }
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
            emptyLabel.styleClass.add("empty-label")
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
        button.styleClass.add("card-button")
        button.prefWidth = cardWidth
        button.prefHeight = cardHeight
        button.minWidth = cardWidth
        button.minHeight = cardHeight

        // Conteneur visuel de la carte
        val container = StackPane()
        container.styleClass.add("card-container")
        container.prefWidth = cardWidth
        container.prefHeight = cardHeight

        // Rang en haut-gauche
        val rankLabel = Label(card.rank.displayName)
        rankLabel.styleClass.addAll("card-rank-label", getCardColorClass(card.suit))
        StackPane.setAlignment(rankLabel, Pos.TOP_LEFT)
        StackPane.setMargin(rankLabel, Insets(6.0, 0.0, 0.0, 8.0))

        // Symbole centré
        val suitLabel = Label(card.suit.icon)
        suitLabel.styleClass.addAll("card-suit-label", getCardColorClass(card.suit))
        StackPane.setAlignment(suitLabel, Pos.CENTER)

        container.children.addAll(suitLabel, rankLabel)
        button.graphic = container

        // Ombre et effet hover
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
        val container = StackPane().apply {
            styleClass.add("card-container")
            prefWidth = pileCardWidth
            prefHeight = pileCardHeight
            minWidth = pileCardWidth
            minHeight = pileCardHeight
            maxWidth = pileCardWidth
            maxHeight = pileCardHeight
        }

        val rankLabel = Label(card.rank.displayName).apply {
            styleClass.addAll("pile-card-rank-label", getCardColorClass(card.suit))
            StackPane.setAlignment(this, Pos.TOP_LEFT)
            StackPane.setMargin(this, Insets(6.0, 0.0, 0.0, 7.0))
        }

        val suitLabel = Label(card.suit.icon).apply {
            styleClass.addAll("pile-card-suit-label", getCardColorClass(card.suit))
            StackPane.setAlignment(this, Pos.CENTER)
        }

        container.effect = DropShadow(8.0, Color.web("#000000", 0.28))
        container.children.addAll(suitLabel, rankLabel)
        return container
    }

    private fun getCardColorClass(suit: Card.Suit): String {
        return when (suit) {
            Card.Suit.HEARTS, Card.Suit.DIAMONDS -> "card-red"
            Card.Suit.CLUBS, Card.Suit.SPADES -> "card-black"
        }
    }

    private fun toggleCardSelection(card: Card, button: Button) {
        if (!controller.canPlayProperty.get()) return

        val container = button.graphic as? StackPane ?: return

        if (selectedCards.contains(card)) {
            selectedCards.remove(card)
            container.styleClass.remove("card-container-selected")
            container.styleClass.add("card-container")
        } else {
            selectedCards.add(card)
            container.styleClass.remove("card-container")
            container.styleClass.add("card-container-selected")
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
            emptyLabel.styleClass.add("empty-label")
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
        val otherPlayers = players.filter { !it.isCurrentPlayer }

        // Distribuer les autres joueurs autour du plateau
        when (otherPlayers.size) {
            1 -> {
                // 1 adversaire: en haut
                addPlayerToPane(topPlayerPane, otherPlayers[0])
            }
            2 -> {
                // 2 adversaires: à gauche et à droite
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
                    addPlayerToPane(topPlayerPane, otherPlayers[index++])
                }
                
                // Left players
                val leftCount = third + if (remainder > 1) 1 else 0
                for (i in 0 until leftCount) {
                    addPlayerToPane(leftPlayerPane, otherPlayers[index++])
                }
                
                // Right players (remaining)
                while (index < otherPlayers.size) {
                    addPlayerToPane(rightPlayerPane, otherPlayers[index++])
                }
            }
        }
    }
    private fun addPlayerToPane(pane: Pane, playerInfo: GameController.PlayerInfo) {
        val nameLabel = Label(playerInfo.name)
        nameLabel.styleClass.add("player-name-label")

        val infoLabel = Label("${playerInfo.role.displayName}\n${playerInfo.cardCount} cartes")
        infoLabel.styleClass.add("player-info-label")
        infoLabel.alignment = Pos.CENTER
        infoLabel.textAlignment = javafx.scene.text.TextAlignment.CENTER

        val playerBox = VBox(8.0).apply {
            styleClass.add("player-box")
            alignment = Pos.CENTER
            padding = Insets(10.0)
            // tailles préférentielles pour occuper plus d'espace
            prefWidth = 160.0
            minWidth = 120.0
            maxWidth = Double.MAX_VALUE
            prefHeight = 100.0
            minHeight = 70.0
            // permettre à la box de croître dans son conteneur
        }

        playerBox.children.addAll(nameLabel, infoLabel)
        pane.children.add(playerBox)

        // Autoriser l'expansion selon le type de pane
        when (pane) {
            is HBox -> {
                HBox.setHgrow(playerBox, Priority.ALWAYS)
            }
            is VBox -> {
                VBox.setVgrow(playerBox, Priority.ALWAYS)
            }
            else -> {
                // pas d'action
            }
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
            
            // Vérifier si le coup est dans les coups possibles
            val possibleMoves = controller.getPossibleMoves()
            val isValidMove = possibleMoves.any { possibleMove ->
                // Comparer par type de jeu et par le fait que le coup peut être joué
                possibleMove.playType == move.playType && 
                possibleMove.getRank() == move.getRank()
            }
            
            if (!isValidMove) {
                messageLabel.text = "Coup illégal: vous ne pouvez pas jouer ces cartes maintenant"
                return
            }
            
            submitMove(move)
        } catch (e: Exception) {
            messageLabel.text = "Coup invalide: ${e.message}"
        }
    }

    private fun handlePass() {
        submitMove(null)
    }

    private fun handleSortHand() {
        controller.sortHumanPlayerHand()
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
    
    /**
     * Affiche le dialog de fin de manche avec les options pour continuer ou quitter.
     */
    private fun showRoundFinishedDialog() {
        // Créer l'overlay semi-transparent
        dialogOverlay = StackPane()
        dialogOverlay?.style = "-fx-background-color: rgba(0, 0, 0, 0.7);"
        
        // Créer le dialog
        roundFinishedDialog = RoundFinishedDialog(
            onNewRound = {
                hideRoundFinishedDialog()
                controller.startNewRound()
            },
            onQuit = {
                hideRoundFinishedDialog()
                handleNewGame()
            }
        )
        
        dialogOverlay?.children?.add(roundFinishedDialog)
        
        // Ajouter l'overlay au root stack (qui contient le centre et le menu)
        val rootStack = center as? StackPane
        rootStack?.children?.add(dialogOverlay)
    }
    
    /**
     * Cache et nettoie le dialog de fin de manche.
     */
    private fun hideRoundFinishedDialog() {
        val rootStack = center as? StackPane
        dialogOverlay?.let { overlay ->
            rootStack?.children?.remove(overlay)
        }
        dialogOverlay = null
        roundFinishedDialog = null
    }
}
