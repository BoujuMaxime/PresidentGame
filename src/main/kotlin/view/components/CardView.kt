package view.components

import javafx.animation.*
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.util.Duration
import model.Card

/**
 * Vue représentant une carte à jouer dans l'interface.
 *
 * @property card La carte du modèle associée à cette vue.
 * @property onSelect Callback optionnel invoqué lorsque la carte est cliquée.
 * @constructor Crée une vue de carte avec la carte spécifiée et un gestionnaire de sélection optionnel.
 */
class CardView(
    val card: Card,
    private val onSelect: ((CardView) -> Unit)? = null
) : StackPane() {

    private var selected = false
    private val cardContainer: VBox

    /**
     * Indique si la carte est de couleur rouge (cœur ou carreau).
     */
    val isRed: Boolean get() = card.suit == Card.Suit.HEARTS || card.suit == Card.Suit.DIAMONDS

    companion object {
        /** Largeur standard d'une carte. */
        const val WIDTH = 70.0
        /** Hauteur standard d'une carte. */
        const val HEIGHT = 100.0
        /** Largeur réduite pour les petites cartes. */
        const val SMALL_WIDTH = 50.0
        /** Hauteur réduite pour les petites cartes. */
        const val SMALL_HEIGHT = 72.0
    }

    init {
        styleClass.add("card-button")
        prefWidth = WIDTH
        prefHeight = HEIGHT

        val colorClass = if (isRed) "card-red" else "card-black"

        cardContainer = VBox(2.0).apply {
            styleClass.add("card-container")
            alignment = Pos.CENTER
            prefWidth = WIDTH
            prefHeight = HEIGHT
            children.addAll(
                Label(card.rank.displayName).apply { styleClass.addAll("card-rank-label", colorClass) },
                Label(card.suit.icon).apply { styleClass.addAll("card-suit-label", colorClass) }
            )
        }

        children.add(cardContainer)

        setOnMouseClicked { onSelect?.invoke(this) }
        setOnMouseEntered { if (!selected) playHoverAnimation(true) }
        setOnMouseExited { if (!selected) playHoverAnimation(false) }
    }

    /**
     * Définit l'état de sélection de la carte.
     *
     * @param value `true` pour sélectionner la carte, `false` pour la désélectionner.
     */
    fun setSelected(value: Boolean) {
        selected = value
        cardContainer.styleClass.apply {
            remove(if (value) "card-container" else "card-container-selected")
            add(if (value) "card-container-selected" else "card-container")
        }
        translateY = if (value) -15.0 else 0.0
    }

    /**
     * Retourne l'état de sélection actuel de la carte.
     *
     * @return `true` si la carte est sélectionnée, `false` sinon.
     */
    fun isSelected(): Boolean = selected

    /**
     * Active ou désactive l'interactivité de la carte.
     *
     * @param interactive `true` pour rendre la carte interactive, `false` pour la désactiver.
     */
    fun setInteractive(interactive: Boolean) {
        isDisable = !interactive
        opacity = if (interactive) 1.0 else 0.7
    }

    /**
     * Joue l'animation de survol de la carte.
     *
     * @param hovering `true` pour l'animation d'entrée, `false` pour l'animation de sortie.
     */
    private fun playHoverAnimation(hovering: Boolean) {
        val (scale, translate) = if (hovering) 1.05 to -5.0 else 1.0 to 0.0
        ScaleTransition(Duration.millis(100.0), this).apply { toX = scale; toY = scale }.play()
        TranslateTransition(Duration.millis(100.0), this).apply { toY = translate }.play()
    }

    /**
     * Joue l'animation de jeu d'une carte vers une position cible.
     *
     * @param targetX Position X cible en pixels.
     * @param targetY Position Y cible en pixels.
     * @param onFinished Callback exécuté à la fin de l'animation.
     */
    fun playCardAnimation(targetX: Double, targetY: Double, onFinished: () -> Unit) {
        ParallelTransition(
            TranslateTransition(Duration.millis(300.0), this).apply {
                toX = targetX - layoutX
                toY = targetY - layoutY
            },
            RotateTransition(Duration.millis(300.0), this).apply { byAngle = 15.0 },
            FadeTransition(Duration.millis(300.0), this).apply { toValue = 0.0 }
        ).apply { setOnFinished { onFinished() } }.play()
    }

    /**
     * Joue l'animation de distribution d'une carte depuis une position source.
     *
     * @param delay Délai avant le début de l'animation en millisecondes.
     * @param fromX Position X de départ en pixels.
     * @param fromY Position Y de départ en pixels.
     */
    fun playDealAnimation(delay: Double, fromX: Double, fromY: Double) {
        translateX = fromX - layoutX
        translateY = fromY - layoutY
        opacity = 0.0
        scaleX = 0.5
        scaleY = 0.5

        Timeline(
            KeyFrame(
                Duration.millis(delay),
                KeyValue(translateXProperty(), 0.0, Interpolator.EASE_OUT),
                KeyValue(translateYProperty(), 0.0, Interpolator.EASE_OUT),
                KeyValue(opacityProperty(), 1.0),
                KeyValue(scaleXProperty(), 1.0),
                KeyValue(scaleYProperty(), 1.0)
            )
        ).apply { this.delay = Duration.millis(delay) }.play()
    }
}

/**
 * Vue représentant le dos d'une carte.
 *
 * @param small `true` pour une taille très réduite (22x32), `false` pour une taille réduite standard.
 * @constructor Crée une vue de dos de carte avec la taille spécifiée.
 */
class CardBackView(small: Boolean = true) : Region() {
    init {
        val (width, height) = if (small) 22.0 to 32.0 else CardView.SMALL_WIDTH to CardView.SMALL_HEIGHT
        prefWidth = width
        prefHeight = height
        minWidth = width
        minHeight = height
        maxWidth = width
        maxHeight = height
        styleClass.add("opponent-card-back")
    }
}