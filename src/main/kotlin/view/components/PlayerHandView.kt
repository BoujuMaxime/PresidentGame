package view.components

import javafx.animation.*
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.util.Duration
import model.Card

/**
 * Vue de la main du joueur humain avec gestion de sélection.
 *
 * Affiche les cartes du joueur avec possibilité de sélectionner
 * des cartes de même rang pour construire un coup.
 *
 * @property onSelectionChanged Callback appelé à chaque changement de sélection
 * @property cardViews Liste des vues de cartes affichées
 * @property interactive État d'interactivité (tour du joueur)
 * @property dealing Vrai si une animation de distribution est en cours
 *
 * @author BOUJU Maxime
 */
class PlayerHandView(
    private val onSelectionChanged: () -> Unit
) : HBox(-15.0) {

    private val cardViews = mutableListOf<CardView>()
    private var interactive = false
    private var dealing = false

    init {
        styleClass.add("player-hand")
        alignment = Pos.CENTER
        padding = Insets(10.0)
    }

    /**
     * Met à jour les cartes affichées sans animation.
     *
     * @param cards Liste des cartes à afficher
     */
    fun updateHand(cards: List<Card>) {
        if (dealing) return

        children.clear()
        cardViews.clear()

        cards.forEach { card ->
            val cardView = CardView(card) { if (interactive) toggleSelection(it) }
            cardView.setInteractive(interactive)
            cardViews.add(cardView)
            children.add(cardView)
        }
    }

    /**
     * Distribue les cartes avec animation depuis le haut.
     *
     * @param cards Liste des cartes à distribuer
     * @param onComplete Callback de fin d'animation
     */
    fun dealCards(cards: List<Card>, onComplete: () -> Unit = {}) {
        children.clear()
        cardViews.clear()
        dealing = true

        if (cards.isEmpty()) {
            dealing = false
            onComplete()
            return
        }

        val delayPerCard = 100.0
        val animDuration = 350.0

        cards.forEachIndexed { index, card ->
            val cardView = CardView(card) { if (interactive) toggleSelection(it) }
            cardView.setInteractive(false)
            cardView.opacity = 0.0
            cardView.scaleX = 0.3
            cardView.scaleY = 0.3
            cardView.translateY = -250.0

            cardViews.add(cardView)
            children.add(cardView)

            val delay = index * delayPerCard
            Timeline(
                KeyFrame(Duration.millis(delay),
                    KeyValue(cardView.opacityProperty(), 0.0),
                    KeyValue(cardView.translateYProperty(), -250.0),
                    KeyValue(cardView.scaleXProperty(), 0.3),
                    KeyValue(cardView.scaleYProperty(), 0.3),
                    KeyValue(cardView.rotateProperty(), -180.0)
                ),
                KeyFrame(Duration.millis(delay + animDuration),
                    KeyValue(cardView.opacityProperty(), 1.0, Interpolator.EASE_OUT),
                    KeyValue(cardView.translateYProperty(), 0.0, Interpolator.EASE_OUT),
                    KeyValue(cardView.scaleXProperty(), 1.0, Interpolator.EASE_OUT),
                    KeyValue(cardView.scaleYProperty(), 1.0, Interpolator.EASE_OUT),
                    KeyValue(cardView.rotateProperty(), 0.0, Interpolator.EASE_OUT)
                )
            ).apply {
                if (index == cards.lastIndex) {
                    setOnFinished {
                        dealing = false
                        cardViews.forEach { it.setInteractive(interactive) }
                        onComplete()
                    }
                }
            }.play()
        }
    }

    /**
     * Bascule la sélection d'une carte.
     *
     * @param cardView Carte cliquée
     */
    private fun toggleSelection(cardView: CardView) {
        if (!cardView.isSelected()) {
            val selectedRanks = getSelectedCards().map { it.rank }.distinct()
            if (selectedRanks.isNotEmpty() && cardView.card.rank !in selectedRanks) {
                cardViews.forEach { it.setSelected(false) }
            }
        }
        cardView.setSelected(!cardView.isSelected())
        onSelectionChanged()
    }

    /**
     * Retourne les cartes actuellement sélectionnées.
     *
     * @return Liste des cartes sélectionnées
     */
    fun getSelectedCards(): List<Card> = cardViews.filter { it.isSelected() }.map { it.card }

    /**
     * Retourne les vues de cartes sélectionnées.
     *
     * @return Liste des CardView sélectionnées
     */
    fun getSelectedCardViews(): List<CardView> = cardViews.filter { it.isSelected() }

    /**
     * Désélectionne toutes les cartes.
     */
    fun clearSelection() {
        cardViews.forEach { it.setSelected(false) }
        onSelectionChanged()
    }

    /**
     * Active ou désactive l'interactivité.
     *
     * @param value Vrai pour activer les clics
     */
    fun setInteractive(value: Boolean) {
        interactive = value
        if (!dealing) cardViews.forEach { it.setInteractive(value) }
        if (!value) clearSelection()
    }

    /**
     * Sélectionne automatiquement les cartes spécifiées.
     *
     * @param cards Liste des cartes à sélectionner
     */
    fun selectCards(cards: List<Card>) {
        clearSelection()
        cards.forEach { card ->
            cardViews.find { it.card.rank == card.rank && it.card.suit == card.suit }?.setSelected(true)
        }
        onSelectionChanged()
    }
}
