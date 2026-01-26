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
 * Panneau d'affichage d'un joueur dans l'interface du jeu.
 *
 * Ce composant affiche les informations d'un joueur incluant son nom, son rôle,
 * le nombre de cartes qu'il possède et une représentation visuelle de ses cartes.
 * Il gère également les animations de distribution de cartes, la mise en surbrillance
 * du joueur actif et l'affichage de bulles de dialogue.
 *
 * @property playerName Le nom du joueur à afficher.
 * @property role Le rôle actuel du joueur (Président, Vice-Président, etc.).
 * @property cardCount Le nombre de cartes que le joueur possède.
 * @property isCurrentPlayer Indique si ce joueur est le joueur actif.
 *
 * @constructor Crée un panneau joueur avec les informations spécifiées et initialise
 *              l'affichage avec la mise en surbrillance si le joueur est actif.
 */
class PlayerPanel(
    private val playerName: String,
    private var role: Player.Role,
    private var cardCount: Int,
    private var isCurrentPlayer: Boolean
) : VBox(8.0) {

    private val nameLabel = createLabel(playerName, "player-name-label")
    private val roleLabel = createLabel(role.displayName, "player-role-badge", getRoleBadgeClass())
    private val cardCountLabel = createLabel("$cardCount cartes", "player-info-label")
    private val cardsContainer = HBox(2.0).apply {
        styleClass.add("opponent-cards-container")
        alignment = Pos.CENTER
        maxWidth = 140.0
    }
    private val speechBubble = SpeechBubble().apply { isVisible = false }
    private var highlightAnimation: Timeline? = null

    init {
        styleClass.add("player-box")
        alignment = Pos.CENTER
        padding = Insets(12.0)
        prefWidth = 160.0
        children.addAll(speechBubble, nameLabel, roleLabel, cardsContainer, cardCountLabel)
        updateCardDisplay()
        if (isCurrentPlayer) setHighlighted(true)
    }

    /**
     * Crée un label stylisé avec le texte et les classes CSS spécifiés.
     *
     * @param text Le texte à afficher dans le label.
     * @param styleClasses Les classes CSS à appliquer au label.
     * @return Un [Label] configuré et centré.
     */
    private fun createLabel(text: String, vararg styleClasses: String) = Label(text).apply {
        styleClass.addAll(*styleClasses)
        maxWidth = Double.MAX_VALUE
        alignment = Pos.CENTER
    }

    /**
     * Retourne la classe CSS correspondant au rôle du joueur.
     *
     * @return Le nom de la classe CSS pour le badge de rôle.
     */
    private fun getRoleBadgeClass() = when (role) {
        Player.Role.PRESIDENT -> "role-president"
        Player.Role.VICE_PRESIDENT -> "role-vice-president"
        Player.Role.VICE_ASSHOLE -> "role-vice-asshole"
        Player.Role.ASSHOLE -> "role-asshole"
        else -> "role-neutral"
    }

    /**
     * Met à jour l'affichage visuel des cartes du joueur.
     *
     * Affiche jusqu'à 8 cartes face cachée avec un indicateur "+N" si le joueur
     * possède plus de 8 cartes. L'espacement entre les cartes est ajusté
     * automatiquement selon le nombre de cartes affichées.
     */
    private fun updateCardDisplay() {
        cardsContainer.children.clear()
        val displayCount = minOf(cardCount, 8)
        cardsContainer.spacing = if (displayCount > 5) -5.0 else 2.0
        repeat(displayCount) { cardsContainer.children.add(CardBackView()) }
        if (cardCount > 8) {
            cardsContainer.children.add(createLabel("+${cardCount - 8}", "more-cards-label"))
        }
    }

    /**
     * Anime la distribution de cartes au joueur.
     *
     * Chaque carte apparaît avec une animation de fondu et de glissement depuis le bas,
     * avec un délai progressif entre chaque carte pour créer un effet de cascade.
     *
     * @param count Le nombre de cartes à distribuer.
     * @param delayOffset Le délai initial avant de commencer l'animation (en millisecondes).
     * @param onComplete Callback appelé lorsque toutes les animations sont terminées.
     */
    fun dealCards(count: Int, delayOffset: Double = 0.0, onComplete: () -> Unit = {}) {
        cardsContainer.children.clear()
        cardCount = count
        cardCountLabel.text = "$cardCount cartes"
        val displayCount = minOf(count, 8)
        if (displayCount == 0) return onComplete()

        repeat(displayCount) { index ->
            val cardBack = CardBackView().apply {
                opacity = 0.0
                scaleX = 0.3
                scaleY = 0.3
                translateY = 150.0
            }
            cardsContainer.children.add(cardBack)
            val delay = delayOffset + index * 100.0
            Timeline(
                KeyFrame(Duration.millis(delay)),
                KeyFrame(
                    Duration.millis(delay + 300.0),
                    KeyValue(cardBack.opacityProperty(), 1.0, Interpolator.EASE_OUT),
                    KeyValue(cardBack.translateYProperty(), 0.0, Interpolator.EASE_OUT),
                    KeyValue(cardBack.scaleXProperty(), 1.0, Interpolator.EASE_OUT),
                    KeyValue(cardBack.scaleYProperty(), 1.0, Interpolator.EASE_OUT)
                )
            ).apply {
                if (index == displayCount - 1) setOnFinished {
                    if (count > 8) cardsContainer.children.add(createLabel("+${count - 8}", "more-cards-label"))
                    onComplete()
                }
            }.play()
        }
    }

    /**
     * Met à jour les informations du joueur si elles ont changé.
     *
     * @param newRole Le nouveau rôle du joueur.
     * @param newCardCount Le nouveau nombre de cartes.
     * @param newIsCurrentPlayer Indique si le joueur devient le joueur actif.
     */
    fun update(newRole: Player.Role, newCardCount: Int, newIsCurrentPlayer: Boolean) {
        if (role != newRole) {
            role = newRole
            roleLabel.text = role.displayName
            roleLabel.styleClass.setAll("player-role-badge", getRoleBadgeClass())
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
     * Active ou désactive la mise en surbrillance du panneau joueur.
     *
     * Lorsque activée, une animation de pulsation d'opacité est jouée en boucle.
     *
     * @param highlighted `true` pour activer la surbrillance, `false` pour la désactiver.
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
     * Affiche un message dans une bulle de dialogue au-dessus du joueur.
     *
     * @param message Le texte du message à afficher.
     * @param duration La durée d'affichage du message en millisecondes.
     */
    fun showMessage(message: String, duration: Double = 2000.0) {
        speechBubble.show(message, duration)
    }

    /**
     * Joue une animation de réduction/agrandissement sur le conteneur de cartes.
     *
     * Utilisée pour indiquer visuellement qu'une carte a été jouée.
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
 * Bulle de dialogue animée pour afficher des messages temporaires.
 *
 * La bulle apparaît avec un fondu entrant, reste visible pendant la durée spécifiée,
 * puis disparaît avec un fondu sortant.
 */
class SpeechBubble : StackPane() {
    private val messageLabel = Label().apply {
        styleClass.add("speech-bubble-text")
        isWrapText = true
        maxWidth = 120.0
    }

    init {
        styleClass.add("speech-bubble")
        children.add(messageLabel)
    }

    /**
     * Affiche un message dans la bulle avec une animation de fondu.
     *
     * @param message Le texte du message à afficher.
     * @param duration La durée pendant laquelle le message reste visible (en millisecondes).
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
}