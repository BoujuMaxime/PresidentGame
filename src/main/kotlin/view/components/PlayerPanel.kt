package view.components

import javafx.animation.*
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.util.Duration
import model.player.Player

/**
 * Panneau affichant les informations d'un joueur adverse.
 *
 * Affiche le nom, le rôle, le nombre de cartes et une bulle
 * de dialogue pour les actions. Peut être mis en évidence.
 *
 * @property playerName Nom du joueur affiché
 * @property role Rôle actuel du joueur
 * @property cardCount Nombre de cartes en main
 * @property isCurrentPlayer Vrai si c'est le tour de ce joueur
 * @property nameLabel Label du nom
 * @property roleLabel Badge du rôle
 * @property cardCountLabel Label du compteur de cartes
 * @property cardsContainer Conteneur des dos de cartes
 * @property speechBubble Bulle de dialogue
 * @property highlightAnimation Animation de pulsation
 *
 * @author BOUJU Maxime
 */
class PlayerPanel(
    private val playerName: String,
    private var role: Player.Role,
    private var cardCount: Int,
    private var isCurrentPlayer: Boolean
) : VBox(8.0) {

    private val nameLabel: Label
    private val roleLabel: Label
    private val cardCountLabel: Label
    private val cardsContainer: HBox
    private val speechBubble: SpeechBubble
    private var highlightAnimation: Timeline? = null

    init {
        styleClass.add("player-box")
        alignment = Pos.CENTER
        padding = Insets(12.0)
        prefWidth = 160.0
        minWidth = 140.0
        maxWidth = 200.0

        speechBubble = SpeechBubble().apply { isVisible = false }
        nameLabel = Label(playerName).apply {
            styleClass.add("player-name-label")
            maxWidth = Double.MAX_VALUE
            alignment = Pos.CENTER
        }
        roleLabel = Label(role.displayName).apply {
            styleClass.addAll("player-role-badge", getRoleBadgeClass())
            maxWidth = Double.MAX_VALUE
            alignment = Pos.CENTER
        }
        cardsContainer = HBox(2.0).apply {
            styleClass.add("opponent-cards-container")
            alignment = Pos.CENTER
        }
        cardCountLabel = Label("$cardCount cartes").apply {
            styleClass.add("player-info-label")
        }

        updateCardDisplay()
        children.addAll(speechBubble, nameLabel, roleLabel, cardsContainer, cardCountLabel)
        if (isCurrentPlayer) setHighlighted(true)
    }

    /**
     * Retourne la classe CSS du badge selon le rôle.
     *
     * @return Nom de la classe CSS
     */
    private fun getRoleBadgeClass(): String = when (role) {
        Player.Role.PRESIDENT -> "role-president"
        Player.Role.VICE_PRESIDENT -> "role-vice-president"
        Player.Role.VICE_ASSHOLE -> "role-vice-asshole"
        Player.Role.ASSHOLE -> "role-asshole"
        else -> "role-neutral"
    }

    /**
     * Met à jour l'affichage visuel des cartes.
     */
    private fun updateCardDisplay() {
        cardsContainer.children.clear()
        val displayCount = minOf(cardCount, 8)
        repeat(displayCount) { cardsContainer.children.add(CardBackView()) }
        if (cardCount > 8) {
            cardsContainer.children.add(Label("+${cardCount - 8}").apply {
                styleClass.add("more-cards-label")
            })
        }
    }

    /**
     * Distribue les cartes avec animation.
     *
     * @param count Nombre de cartes à distribuer
     * @param delayOffset Délai initial (ms)
     * @param onComplete Callback de fin
     */
    fun dealCards(count: Int, delayOffset: Double = 0.0, onComplete: () -> Unit = {}) {
        cardsContainer.children.clear()
        cardCount = count
        cardCountLabel.text = "$cardCount cartes"

        val displayCount = minOf(count, 8)
        if (displayCount == 0) { onComplete(); return }

        val delayPerCard = 100.0
        val animDuration = 300.0

        repeat(displayCount) { index ->
            val cardBack = CardBackView().apply {
                opacity = 0.0
                scaleX = 0.3
                scaleY = 0.3
                translateY = 150.0
            }
            cardsContainer.children.add(cardBack)

            val delay = delayOffset + index * delayPerCard
            Timeline(
                KeyFrame(Duration.millis(delay),
                    KeyValue(cardBack.opacityProperty(), 0.0),
                    KeyValue(cardBack.translateYProperty(), 150.0),
                    KeyValue(cardBack.scaleXProperty(), 0.3),
                    KeyValue(cardBack.scaleYProperty(), 0.3)
                ),
                KeyFrame(Duration.millis(delay + animDuration),
                    KeyValue(cardBack.opacityProperty(), 1.0, Interpolator.EASE_OUT),
                    KeyValue(cardBack.translateYProperty(), 0.0, Interpolator.EASE_OUT),
                    KeyValue(cardBack.scaleXProperty(), 1.0, Interpolator.EASE_OUT),
                    KeyValue(cardBack.scaleYProperty(), 1.0, Interpolator.EASE_OUT)
                )
            ).apply {
                if (index == displayCount - 1) {
                    setOnFinished {
                        if (count > 8) {
                            cardsContainer.children.add(Label("+${count - 8}").apply {
                                styleClass.add("more-cards-label")
                            })
                        }
                        onComplete()
                    }
                }
            }.play()
        }
    }

    /**
     * Met à jour les informations affichées.
     *
     * @param newRole Nouveau rôle
     * @param newCardCount Nouveau nombre de cartes
     * @param newIsCurrentPlayer Nouveau statut joueur actif
     */
    fun update(newRole: Player.Role, newCardCount: Int, newIsCurrentPlayer: Boolean) {
        if (role != newRole) {
            role = newRole
            roleLabel.text = role.displayName
            roleLabel.styleClass.removeAll("role-president", "role-vice-president", "role-vice-asshole", "role-asshole", "role-neutral")
            roleLabel.styleClass.add(getRoleBadgeClass())
        }
        if (cardCount != newCardCount) {
            cardCount = newCardCount
            cardCountLabel.text = "$cardCount cartes"
            updateCardDisplay()
        }
        if (isCurrentPlayer != newIsCurrentPlayer) {
            isCurrentPlayer = newIsCurrentPlayer
            setHighlighted(isCurrentPlayer)
        }
    }

    /**
     * Active ou désactive la mise en évidence du panneau.
     *
     * @param highlighted Vrai pour activer
     */
    fun setHighlighted(highlighted: Boolean) {
        highlightAnimation?.stop()
        if (highlighted) {
            styleClass.add("player-box-highlighted")
            highlightAnimation = Timeline(
                KeyFrame(Duration.ZERO, KeyValue(opacityProperty(), 1.0)),
                KeyFrame(Duration.millis(500.0), KeyValue(opacityProperty(), 0.85)),
                KeyFrame(Duration.millis(1000.0), KeyValue(opacityProperty(), 1.0))
            ).apply { cycleCount = Animation.INDEFINITE; play() }
        } else {
            styleClass.remove("player-box-highlighted")
            opacity = 1.0
        }
    }

    /**
     * Affiche un message temporaire dans la bulle.
     *
     * @param message Texte à afficher
     * @param duration Durée d'affichage (ms)
     */
    fun showMessage(message: String, duration: Double = 2000.0) {
        speechBubble.show(message, duration)
    }

    /**
     * Anime le jeu d'une carte (effet bounce).
     */
    fun animateCardPlay() {
        ScaleTransition(Duration.millis(150.0), cardsContainer).apply {
            toX = 0.9
            toY = 0.9
            isAutoReverse = true
            cycleCount = 2
        }.play()
    }
}

/**
 * Bulle de dialogue pour messages temporaires au-dessus d'un joueur.
 *
 * @property messageLabel Label du texte affiché
 *
 * @author BOUJU Maxime
 */
class SpeechBubble : StackPane() {

    private val messageLabel: Label

    init {
        styleClass.add("speech-bubble")
        messageLabel = Label().apply {
            styleClass.add("speech-bubble-text")
            isWrapText = true
            maxWidth = 120.0
        }
        children.add(messageLabel)
    }

    /**
     * Affiche un message pendant une durée déterminée.
     *
     * @param message Texte à afficher
     * @param duration Durée d'affichage (ms)
     */
    fun show(message: String, duration: Double) {
        messageLabel.text = message
        isVisible = true
        opacity = 0.0

        SequentialTransition(
            FadeTransition(Duration.millis(200.0), this).apply { toValue = 1.0 },
            FadeTransition(Duration.millis(200.0), this).apply {
                toValue = 0.0
                delay = Duration.millis(duration)
                setOnFinished { isVisible = false }
            }
        ).play()
    }

    companion object {
        /**
         * Formate une action en texte lisible.
         *
         * @param action Action brute
         * @return Texte formaté avec emoji
         */
        fun formatAction(action: String): String = when {
            action.contains("passe") -> "💤 Passe"
            action.contains("joue") -> "🎴 ${action.substringAfter("joue:")}"
            else -> action
        }
    }
}
