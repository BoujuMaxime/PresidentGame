package view

import controller.GameController
import javafx.animation.FadeTransition
import javafx.animation.ParallelTransition
import javafx.animation.PauseTransition
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

    // Notifications et bulles de discussion
    private val playerNotificationLabel: Label

    private val selectedCards = mutableSetOf<Card>()
    private val cardButtons = mutableMapOf<Card, Button>()

    // Dialog de fin de manche - container réutilisable pour éviter les fuites mémoire
    private var roundFinishedDialog: RoundFinishedDialog? = null
    private val dialogOverlayContainer: StackPane = StackPane()

    // Tailles réduites
    private val cardWidth = 90.0 * 0.75
    private val cardHeight = 120.0 * 0.75
    private val pileCardWidth = cardWidth * 1.2
    private val pileCardHeight = cardHeight * 1.2
    private val pileAnimationDuration = Duration.millis(280.0)
    private var lastPileSnapshot: List<Card> = emptyList()

    // Constante pour le nombre maximum de cartes affichées dans les panneaux adversaires
    private val maxDisplayedCards = 10


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
        topPlayerPane = HBox(20.0)  // Augmenté de 8.0 à 20.0 pour plus d'espacement
        topPlayerPane.styleClass.add("opponent-pane")
        topPlayerPane.alignment = Pos.CENTER
        topPlayerPane.padding = Insets(10.0)
        topPlayerPane.minHeight = 80.0

        leftPlayerPane = VBox(20.0)  // Augmenté de 8.0 à 20.0 pour plus d'espacement
        leftPlayerPane.styleClass.add("opponent-pane")
        leftPlayerPane.alignment = Pos.CENTER
        leftPlayerPane.padding = Insets(10.0)
        leftPlayerPane.minWidth = 150.0

        rightPlayerPane = VBox(20.0)  // Augmenté de 8.0 à 20.0 pour plus d'espacement
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

        // Notification du joueur (messages personnels)
        playerNotificationLabel = Label("")
        playerNotificationLabel.styleClass.add("player-notification")
        playerNotificationLabel.isWrapText = true
        playerNotificationLabel.maxWidth = 600.0
        playerNotificationLabel.isVisible = false

        val notificationContainer = StackPane(playerNotificationLabel)
        notificationContainer.alignment = Pos.CENTER
        notificationContainer.padding = Insets(5.0)

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
        
        val bottomPane = VBox(10.0)  // Réduit de 15.0 à 10.0 pour moins d'espace vide
        bottomPane.alignment = Pos.CENTER
        bottomPane.padding = Insets(5.0, 10.0, 10.0, 10.0)  // Moins d'espace en haut
        bottomPane.children.addAll(notificationContainer, playerHandPane, actionButtonPane)

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
        
        // Conteneur pour le bouton de menu et le menu lui-même dans le coin supérieur droit du BorderPane
        val menuBox = VBox(10.0)
        menuBox.alignment = Pos.TOP_RIGHT
        menuBox.padding = Insets(10.0)
        menuBox.children.addAll(menuButton, inGameMenu)

        // === Disposition finale ===
        // Créer un conteneur pour le panneau du haut avec le menu superposé
        val topContainer = StackPane()
        topContainer.children.addAll(topPlayerPane, menuBox)
        StackPane.setAlignment(menuBox, Pos.TOP_RIGHT)
        
        top = topContainer
        left = leftPlayerPane
        right = rightPlayerPane
        bottom = bottomPane
        center = centerPane

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
            updateGameMessage(newMessage)
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

    /**
     * Met à jour l'affichage des messages du jeu en séparant les messages généraux
     * des notifications personnelles au joueur.
     */
    private fun updateGameMessage(message: String) {
        // Séparer les messages destinés au joueur des messages généraux
        when {
            message.contains("C'est votre tour", ignoreCase = true) ||
            message.contains("Vous devez", ignoreCase = true) ||
            message.contains("Vous pouvez", ignoreCase = true) ||
            message.contains("Veuillez", ignoreCase = true) -> {
                // Message pour le joueur
                playerNotificationLabel.text = message
                playerNotificationLabel.isVisible = true
                animateNotification()
                messageLabel.text = "À votre tour de jouer"
            }
            message.contains("gagne", ignoreCase = true) ||
            message.contains("termine", ignoreCase = true) -> {
                // Annonce générale importante
                messageLabel.text = message
                playerNotificationLabel.isVisible = false
            }
            else -> {
                // Message général
                messageLabel.text = message
                // Masquer la notification après un court délai
                PauseTransition(Duration.seconds(3.0)).apply {
                    setOnFinished { playerNotificationLabel.isVisible = false }
                    play()
                }
            }
        }
    }

    /**
     * Anime l'apparition de la notification du joueur.
     */
    private fun animateNotification() {
        playerNotificationLabel.opacity = 0.0
        val fadeIn = FadeTransition(Duration.millis(300.0), playerNotificationLabel)
        fadeIn.fromValue = 0.0
        fadeIn.toValue = 1.0
        fadeIn.play()
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

        // Créer le label de rôle avec une icône
        val roleIcon = getRoleIcon(playerInfo.role)
        val roleText = "$roleIcon ${playerInfo.role.displayName}"
        
        val infoLabel = Label("$roleText\n${playerInfo.cardCount} cartes")
        infoLabel.styleClass.add("player-info-label")
        infoLabel.alignment = Pos.CENTER
        infoLabel.textAlignment = javafx.scene.text.TextAlignment.CENTER

        // Créer une représentation visuelle des cartes
        val cardsContainer = createOpponentCardsVisual(playerInfo.cardCount)

        val playerBox = VBox(8.0).apply {
            styleClass.add("player-box")
            alignment = Pos.CENTER
            padding = Insets(10.0)
            // tailles préférentielles pour occuper plus d'espace
            prefWidth = 160.0
            minWidth = 120.0
            maxWidth = Double.MAX_VALUE
            prefHeight = 120.0
            minHeight = 90.0
            // permettre à la box de croître dans son conteneur
        }

        playerBox.children.addAll(nameLabel, infoLabel, cardsContainer)
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

    /**
     * Crée une représentation visuelle des cartes d'un adversaire
     */
    private fun createOpponentCardsVisual(cardCount: Int): HBox {
        val container = HBox(-8.0)  // Espacement négatif pour superposition
        container.styleClass.add("opponent-cards-container")
        container.alignment = Pos.CENTER
        container.padding = Insets(5.0, 0.0, 0.0, 0.0)

        // Limiter l'affichage à maxDisplayedCards cartes maximum pour éviter de déborder
        val displayCount = minOf(cardCount, maxDisplayedCards)
        
        for (i in 0 until displayCount) {
            val cardBack = Region()
            cardBack.styleClass.add("opponent-card-back")
            container.children.add(cardBack)
        }

        return container
    }

    /**
     * Retourne une icône pour le rôle du joueur
     */
    private fun getRoleIcon(role: model.player.Player.Role): String {
        return when (role) {
            model.player.Player.Role.PRESIDENT -> "👑"
            model.player.Player.Role.VICE_PRESIDENT -> "🥈"
            model.player.Player.Role.NEUTRAL -> "👤"
            model.player.Player.Role.VICE_ASSHOLE -> "🥉"
            model.player.Player.Role.ASSHOLE -> "💩"
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
        
        // Configurer le container réutilisable
        dialogOverlayContainer.style = "-fx-background-color: rgba(0, 0, 0, 0.7);"
        dialogOverlayContainer.children.setAll(roundFinishedDialog)
        
        // Créer un StackPane pour l'overlay au dessus du centerPane
        val overlayStack = StackPane()
        overlayStack.children.addAll(centerPane, dialogOverlayContainer)
        center = overlayStack
    }
    
    /**
     * Cache et nettoie le dialog de fin de manche.
     */
    private fun hideRoundFinishedDialog() {
        // Restaurer le centerPane original
        center = centerPane
        // Nettoyer le container
        dialogOverlayContainer.children.clear()
        roundFinishedDialog = null
    }
}
