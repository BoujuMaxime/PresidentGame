package view

import controller.GameController
import javafx.animation.*
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.util.Duration
import model.Card
import model.player.Player
import view.components.*
import view.dialogs.CardExchangeDialog
import view.dialogs.InGameMenuView
import view.dialogs.RoundFinishedDialog
import view.themes.ThemeManager

/**
 * Vue principale du plateau de jeu.
 *
 * @property controller Contrôleur du jeu
 * @property onBackToMenu Callback retour menu
 * @property themeManager Gestionnaire de thèmes
 */
class GameBoardView(
    private val controller: GameController,
    private val onBackToMenu: () -> Unit,
    private val themeManager: ThemeManager
) : StackPane() {

    private val gamePane = BorderPane()
    private val eventLog = EventLog()
    private val pileView = PileView()
    private val playerHandView = PlayerHandView { onCardSelectionChanged() }
    private val actionBar = ActionBar(
        onPlay = { handlePlayCards() },
        onPass = { handlePass() },
        onSort = { handleSort() }
    )

    private val opponentPanels = mutableMapOf<String, PlayerPanel>()
    private val selectedCards = mutableListOf<Card>()
    private var messageLabel: Label? = null
    private var opponentsContainer: HBox? = null

    private val dialogOverlay = StackPane().apply {
        isVisible = false
        styleClass.add("dialog-overlay")
        // S'assurer que l'overlay couvre tout et bloque les interactions
        setOnMouseClicked { it.consume() }
    }

    init {
        styleClass.add("game-board")
        setupLayout()
        setupBindings()
    }

    private fun setupLayout() {
        // Configurer le gamePane (BorderPane interne)
        gamePane.styleClass.add("game-pane")

        // Zone supérieure : adversaires + menu
        gamePane.top = createTopArea()

        // Zone centrale : pile de cartes + log d'événements
        gamePane.center = createCenterArea()

        // Zone inférieure : main du joueur + boutons d'action
        gamePane.bottom = createBottomArea()

        // Ajouter le gamePane et l'overlay dans ce StackPane
        // L'overlay est ajouté après, donc il sera par-dessus
        children.addAll(gamePane, dialogOverlay)
    }

    private fun createTopArea(): HBox = HBox(20.0).apply {
        styleClass.add("top-area")
        alignment = Pos.CENTER
        padding = Insets(15.0)

        val menuButton = Button("☰").apply {
            styleClass.addAll("action-button", "menu-button")
            setOnAction { showInGameMenu() }
        }

        opponentsContainer = HBox(30.0).apply {
            styleClass.add("opponents-container")
            alignment = Pos.CENTER
            HBox.setHgrow(this, Priority.ALWAYS)
        }

        children.addAll(menuButton, opponentsContainer, createStatusBox())
    }

    private fun createStatusBox(): VBox = VBox(5.0).apply {
        styleClass.add("status-box")
        alignment = Pos.CENTER_RIGHT
        padding = Insets(10.0)

        children.addAll(
            Label().apply {
                styleClass.add("state-label")
                textProperty().bind(controller.gameStateProperty)
            },
            Label().apply {
                styleClass.add("current-player-label")
                textProperty().bind(controller.currentPlayerProperty)
            }
        )
    }

    private fun createCenterArea(): HBox = HBox(15.0).apply {
        styleClass.add("center-area")
        alignment = Pos.CENTER
        padding = Insets(15.0)

        val pileContainer = VBox(10.0).apply {
            styleClass.add("pile-container")
            alignment = Pos.CENTER
            HBox.setHgrow(this, Priority.ALWAYS)
            minWidth = 350.0

            messageLabel = Label().apply {
                styleClass.add("message-label")
                isWrapText = true
                maxWidth = 420.0
                alignment = Pos.CENTER
            }

            children.addAll(
                Label("🎴 Pile de jeu").apply { styleClass.add("section-title") },
                pileView,
                VBox(8.0).apply {
                    styleClass.add("message-box")
                    alignment = Pos.CENTER
                    padding = Insets(12.0)
                    maxWidth = 450.0
                    children.add(messageLabel)
                }
            )
        }

        val logContainer = VBox(8.0).apply {
            styleClass.add("log-container")
            prefWidth = 320.0
            minWidth = 280.0
            maxWidth = 380.0
            children.addAll(
                Label("📜 Historique").apply { styleClass.add("section-title") },
                eventLog
            )
            VBox.setVgrow(eventLog, Priority.ALWAYS)
        }

        children.addAll(pileContainer, logContainer)
    }

    private fun createBottomArea(): VBox = VBox(15.0).apply {
        styleClass.add("player-pane")
        alignment = Pos.CENTER
        padding = Insets(20.0)

        val playerInfoBox = HBox(20.0).apply {
            alignment = Pos.CENTER_LEFT
            children.addAll(
                Label("Vous").apply { styleClass.add("player-name-label") },
                Label().apply {
                    styleClass.add("player-role-badge")
                    textProperty().bind(controller.playerRoleProperty)
                }
            )
        }

        val handScrollPane = ScrollPane(playerHandView).apply {
            styleClass.add("hand-scroll-pane")
            isFitToHeight = true
            vbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            hbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
            maxHeight = 180.0
        }

        actionBar.disableProperty().bind(controller.canPlayProperty.not())
        children.addAll(playerInfoBox, handScrollPane, actionBar)
    }

    private fun setupBindings() {
        controller.humanPlayerHandProperty.addListener { _, _, newHand ->
            Platform.runLater {
                playerHandView.updateHand(newHand ?: emptyList())
                selectedCards.clear()
                updateActionBarState()
            }
        }

        controller.currentPileProperty.addListener { _, _, newPile ->
            Platform.runLater { pileView.updatePile(newPile ?: emptyList()) }
        }

        controller.playersInfoProperty.addListener { _, oldInfos, newInfos ->
            Platform.runLater {
                val infos = newInfos ?: emptyList()
                // Ne mettre à jour que si c'est vraiment nécessaire
                if (oldInfos?.size != infos.size || opponentPanels.isEmpty()) {
                    updateOpponentPanels(infos)
                } else {
                    // Mise à jour partielle des panneaux existants
                    infos.filter { it.name != "Vous" }.forEach { info ->
                        opponentPanels[info.name]?.update(info.role, info.cardCount, info.isCurrentPlayer)
                    }
                }
            }
        }

        controller.canPlayProperty.addListener { _, _, canPlay ->
            Platform.runLater {
                playerHandView.setInteractive(canPlay)
                updateActionBarState()
            }
        }

        controller.gameMessageProperty.addListener { _, oldMessage, newMessage ->
            Platform.runLater {
                if (!newMessage.isNullOrBlank()) {
                    // Mettre à jour le label principal
                    messageLabel?.text = newMessage

                    // Ajouter au journal seulement certains types de messages
                    val eventType = categorizeMessage(newMessage)
                    if (eventType != null && newMessage != oldMessage) {
                        eventLog.addEvent(newMessage, eventType)
                    }
                }
            }
        }

        controller.currentPlayerProperty.addListener { _, _, player ->
            Platform.runLater {
                if (!player.isNullOrBlank()) {
                    if (player == "À vous de jouer!") {
                        highlightCurrentPlayer("Vous")
                    } else {
                        highlightCurrentPlayer(player)
                    }
                }
            }
        }
    }

    private fun categorizeMessage(message: String): EventLog.EventType? = when {
        // Messages des tours des joueurs
        message.contains("joue:") || message.contains("joue ") -> EventLog.EventType.AI_ACTION
        message.contains("passe son tour") -> EventLog.EventType.AI_ACTION
        // Messages importants du jeu
        message.contains("terminée") || message.contains("terminé") -> EventLog.EventType.ROUND
        message.contains("commence") -> EventLog.EventType.SYSTEM
        message.contains("Carré") -> EventLog.EventType.SUCCESS
        // Messages d'instruction pour le joueur - ne pas ajouter au journal
        message.contains("Choisissez") || message.contains("À vous") || message.contains("Sélectionnez") -> null
        // Autres messages - ajouter comme info
        else -> null
    }

    private fun updateOpponentPanels(infos: List<GameController.PlayerInfo>) {
        opponentsContainer?.children?.clear()
        opponentPanels.clear()

        infos.filter { it.name != "Vous" }.forEach { info ->
            val panel = PlayerPanel(info.name, info.role, info.cardCount, info.isCurrentPlayer)
            opponentPanels[info.name] = panel
            opponentsContainer?.children?.add(panel)
        }
    }

    private fun highlightCurrentPlayer(playerName: String) {
        opponentPanels.values.forEach { it.setHighlighted(false) }
        opponentPanels[playerName]?.setHighlighted(true)
    }

    private fun onCardSelectionChanged() {
        selectedCards.clear()
        selectedCards.addAll(playerHandView.getSelectedCards())
        updateActionBarState()
    }

    private fun updateActionBarState() {
        val canPlay = controller.canPlayProperty.get()
        val isValidMove = selectedCards.isNotEmpty() && controller.getPossibleMoves().any { move ->
            move.size == selectedCards.size &&
            move.all { card -> selectedCards.any { it.rank == card.rank && it.suit == card.suit } }
        }

        actionBar.setPlayEnabled(canPlay && isValidMove)
        actionBar.setPassEnabled(canPlay)
    }

    private fun handlePlayCards() {
        if (selectedCards.isEmpty()) return

        val matchingMove = controller.getPossibleMoves().find { move ->
            move.size == selectedCards.size &&
            move.all { card -> selectedCards.any { it.rank == card.rank && it.suit == card.suit } }
        }

        matchingMove?.let { move ->
            // Animation de jeu
            animatePlayCards(selectedCards.toList())

            eventLog.addEvent("Vous jouez: $move", EventLog.EventType.PLAYER_ACTION)
            controller.submitPlayerMove(move)
            controller.setCanPlay(false)
        }
    }

    private fun handlePass() {
        eventLog.addEvent("Vous passez votre tour", EventLog.EventType.PLAYER_ACTION)
        controller.submitPlayerMove(null)
        controller.setCanPlay(false)
    }

    private fun handleSort() {
        controller.sortHumanPlayerHand()
        controller.updateHumanPlayerHand()
    }

    private fun animatePlayCards(cards: List<Card>) {
        val selectedViews = playerHandView.getSelectedCardViews()
        val pileCenter = pileView.localToScene(pileView.width / 2, pileView.height / 2)

        var completedAnimations = 0
        selectedViews.forEachIndexed { index, cardView ->
            val cardCenter = cardView.localToScene(cardView.width / 2, cardView.height / 2)
            val targetX = pileCenter.x - cardCenter.x + (index * 10)
            val targetY = pileCenter.y - cardCenter.y

            cardView.playCardAnimation(targetX, targetY) {
                completedAnimations++
                if (completedAnimations == selectedViews.size) {
                    SequentialTransition(
                        ScaleTransition(Duration.millis(150.0), pileView).apply { toX = 1.1; toY = 1.1 },
                        ScaleTransition(Duration.millis(150.0), pileView).apply { toX = 1.0; toY = 1.0 }
                    ).play()
                }
            }
        }
    }

    fun animateDealCards(cardCounts: Map<String, Int>, onComplete: () -> Unit) {
        Platform.runLater {
            eventLog.addEvent("🎴 Distribution des cartes...", EventLog.EventType.SYSTEM)
            messageLabel?.text = "Distribution des cartes..."

            ensureOpponentPanelsExist(cardCounts)
            val humanCards = controller.humanPlayerHandProperty.get() ?: emptyList()

            var totalDelay = 0.0
            opponentPanels.forEach { (name, panel) ->
                val count = cardCounts[name] ?: 0
                if (count > 0) {
                    panel.dealCards(count, totalDelay)
                    totalDelay += minOf(count, 8) * 100.0 + 200.0
                }
            }

            if (humanCards.isNotEmpty()) {
                Timeline(KeyFrame(Duration.millis(totalDelay + 300.0), {
                    playerHandView.dealCards(humanCards) {
                        eventLog.addEvent("Distribution terminée!", EventLog.EventType.SUCCESS)
                        messageLabel?.text = "La partie peut commencer!"
                        onComplete()
                    }
                })).play()
            } else {
                Timeline(KeyFrame(Duration.millis(totalDelay + 500.0), { onComplete() })).play()
            }
        }
    }

    private fun ensureOpponentPanelsExist(cardCounts: Map<String, Int>) {
        cardCounts.keys.filter { it != "Vous" }.forEach { playerName ->
            if (!opponentPanels.containsKey(playerName)) {
                val panel = PlayerPanel(playerName, Player.Role.NEUTRAL, 0, false)
                opponentPanels[playerName] = panel
                opponentsContainer?.children?.add(panel)
            }
        }
    }

    private fun showInGameMenu() {
        showDialog(InGameMenuView(
            onResume = { hideDialog() },
            onNewGame = { hideDialog(); onBackToMenu() },
            onQuit = { Platform.exit() }
        ))
    }

    fun showRoundFinishedDialog() {
        showDialog(RoundFinishedDialog(
            playersInfo = controller.playersInfoProperty.get() ?: emptyList(),
            onContinue = { hideDialog(); controller.startNewRound() },
            onBackToMenu = { hideDialog(); onBackToMenu() }
        ))
    }

    fun showCardExchangeDialog(count: Int, highest: Boolean) {
        showDialog(CardExchangeDialog(
            hand = controller.humanPlayerHandProperty.get() ?: emptyList(),
            count = count,
            mustSelectHighest = highest,
            onConfirm = { cards -> hideDialog(); controller.submitExchangeCards(cards) }
        ))
    }

    private fun showDialog(content: Region) {
        dialogOverlay.children.setAll(content)
        dialogOverlay.isVisible = true
        FadeTransition(Duration.millis(200.0), dialogOverlay).apply {
            fromValue = 0.0
            toValue = 1.0
        }.play()
    }

    private fun hideDialog() {
        FadeTransition(Duration.millis(200.0), dialogOverlay).apply {
            fromValue = 1.0
            toValue = 0.0
            setOnFinished {
                dialogOverlay.isVisible = false
                dialogOverlay.children.clear()
            }
        }.play()
    }
}
