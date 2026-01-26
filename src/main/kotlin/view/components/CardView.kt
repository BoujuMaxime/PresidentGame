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
 * Affiche le rang et la couleur de la carte avec gestion
 * de la sélection, du survol et des animations de jeu.
 *
 * @property card Carte du modèle représentée
 * @property onSelect Callback appelé lors d'un clic sur la carte
 * @property selected État de sélection de la carte
 * @property cardContainer Conteneur visuel de la carte
 * @property rankLabel Label affichant le rang
 * @property suitLabel Label affichant le symbole
 * @property isRed Vrai si la carte est rouge (Cœur/Carreau)
 *
 * @author BOUJU Maxime
 */
class CardView(
    val card: Card,
    private val onSelect: ((CardView) -> Unit)? = null
) : StackPane() {

    private var selected = false
    private val cardContainer: VBox
    private val rankLabel: Label
    private val suitLabel: Label

    val isRed: Boolean get() = card.suit == Card.Suit.HEARTS || card.suit == Card.Suit.DIAMONDS

    companion object {
        /** Largeur standard d'une carte */
        const val WIDTH = 70.0
        /** Hauteur standard d'une carte */
        const val HEIGHT = 100.0
        /** Largeur réduite pour la pile */
        const val SMALL_WIDTH = 50.0
        /** Hauteur réduite pour la pile */
        const val SMALL_HEIGHT = 72.0
    }

    init {
        styleClass.add("card-button")
        prefWidth = WIDTH
        prefHeight = HEIGHT

        cardContainer = VBox(2.0).apply {
            styleClass.add("card-container")
            alignment = Pos.CENTER
            prefWidth = WIDTH
            prefHeight = HEIGHT
        }

        val colorClass = if (isRed) "card-red" else "card-black"
        rankLabel = Label(card.rank.displayName).apply {
            styleClass.addAll("card-rank-label", colorClass)
        }
        suitLabel = Label(card.suit.icon).apply {
            styleClass.addAll("card-suit-label", colorClass)
        }

        cardContainer.children.addAll(rankLabel, suitLabel)
        children.add(cardContainer)

        setupEventHandlers()
    }

    /**
     * Configure les gestionnaires d'événements souris.
     */
    private fun setupEventHandlers() {
        setOnMouseClicked { onSelect?.invoke(this) }
        setOnMouseEntered { if (!selected) playHoverAnimation(true) }
        setOnMouseExited { if (!selected) playHoverAnimation(false) }
    }

    /**
     * Définit l'état de sélection de la carte.
     *
     * @param value Vrai pour sélectionner, faux pour désélectionner
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
     * Retourne l'état de sélection.
     *
     * @return Vrai si la carte est sélectionnée
     */
    fun isSelected(): Boolean = selected

    /**
     * Active ou désactive l'interactivité de la carte.
     *
     * @param interactive Vrai pour activer les clics
     */
    fun setInteractive(interactive: Boolean) {
        isDisable = !interactive
        opacity = if (interactive) 1.0 else 0.7
    }

    /**
     * Joue l'animation de survol.
     *
     * @param hovering Vrai pour entrée souris, faux pour sortie
     */
    private fun playHoverAnimation(hovering: Boolean) {
        val scale = if (hovering) 1.05 else 1.0
        val translate = if (hovering) -5.0 else 0.0

        ScaleTransition(Duration.millis(100.0), this).apply { toX = scale; toY = scale }.play()
        TranslateTransition(Duration.millis(100.0), this).apply { toY = translate }.play()
    }

    /**
     * Anime la carte vers une position cible (jeu vers la pile).
     *
     * @param targetX Position X cible relative
     * @param targetY Position Y cible relative
     * @param onFinished Callback de fin d'animation
     */
    fun playCardAnimation(targetX: Double, targetY: Double, onFinished: () -> Unit) {
        ParallelTransition(
            TranslateTransition(Duration.millis(300.0), this).apply {
                toX = targetX - layoutX
                toY = targetY - layoutY
            },
            RotateTransition(Duration.millis(300.0), this).apply { byAngle = 15.0 },
            FadeTransition(Duration.millis(300.0), this).apply { toValue = 0.0 }
        ).apply {
            setOnFinished { onFinished() }
        }.play()
    }

    /**
     * Anime l'apparition de la carte lors de la distribution.
     *
     * @param delay Délai avant le début de l'animation (ms)
     * @param fromX Position X source relative
     * @param fromY Position Y source relative
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
        ).apply {
            this.delay = Duration.millis(delay)
        }.play()
    }
}

/**
 * Vue représentant le dos d'une carte (pour les adversaires).
 *
 * @property small Vrai pour taille réduite (22x32), faux pour normale (50x72)
 *
 * @author BOUJU Maxime
 */
class CardBackView(private val small: Boolean = true) : Region() {
    init {
        val width = if (small) 22.0 else CardView.SMALL_WIDTH
        val height = if (small) 32.0 else CardView.SMALL_HEIGHT

        prefWidth = width
        prefHeight = height
        minWidth = width
        minHeight = height
        maxWidth = width
        maxHeight = height

        styleClass.add("opponent-card-back")
    }
}
