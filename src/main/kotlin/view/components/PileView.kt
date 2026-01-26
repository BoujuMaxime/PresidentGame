package view.components

import javafx.animation.*
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.util.Duration
import model.Card

/**
 * Vue de la pile centrale de cartes jouées.
 *
 * Affiche les cartes du dernier coup avec animations de mise à jour.
 *
 * @property cardsContainer Conteneur des cartes affichées
 * @property emptyLabel Label "Pile vide"
 *
 * @author BOUJU Maxime
 */
class PileView : StackPane() {

    private val cardsContainer: HBox
    private val emptyLabel: Label

    init {
        styleClass.add("pile-stack")
        prefWidth = 300.0
        prefHeight = 180.0
        minWidth = 250.0
        minHeight = 150.0

        cardsContainer = HBox(-30.0).apply {
            alignment = Pos.CENTER
            padding = Insets(20.0)
        }

        emptyLabel = Label("Pile vide").apply {
            styleClass.add("empty-label")
        }

        children.addAll(emptyLabel, cardsContainer)
    }

    /**
     * Met à jour la pile avec les nouvelles cartes.
     *
     * @param cards Liste complète des cartes sur la pile
     */
    fun updatePile(cards: List<Card>) {
        if (cards.isEmpty()) {
            showEmpty()
            return
        }
        emptyLabel.isVisible = false
        animatePileUpdate(getLastMoveCards(cards))
    }

    /**
     * Extrait les cartes du dernier coup (même rang).
     *
     * @param cards Liste des cartes sur la pile
     * @return Liste des cartes du dernier coup
     */
    private fun getLastMoveCards(cards: List<Card>): List<Card> {
        if (cards.isEmpty()) return emptyList()
        val lastRank = cards.last().rank
        return cards.takeLastWhile { it.rank == lastRank }
    }

    /**
     * Anime la mise à jour de la pile.
     *
     * @param newCards Nouvelles cartes à afficher
     */
    private fun animatePileUpdate(newCards: List<Card>) {
        FadeTransition(Duration.millis(100.0), cardsContainer).apply {
            toValue = 0.0
            setOnFinished {
                cardsContainer.children.clear()
                newCards.forEachIndexed { index, card ->
                    val cardView = createPileCard(card).apply {
                        rotate = (index - newCards.size / 2.0) * 5.0
                    }
                    cardsContainer.children.add(cardView)
                }

                FadeTransition(Duration.millis(200.0), cardsContainer).apply { toValue = 1.0 }.play()

                cardsContainer.children.forEach { card ->
                    card.translateY = -30.0
                    TranslateTransition(Duration.millis(200.0), card).apply {
                        toY = 0.0
                        interpolator = Interpolator.EASE_OUT
                    }.play()
                }
            }
        }.play()
    }

    /**
     * Crée une vue de carte pour la pile.
     *
     * @param card Carte à afficher
     * @return StackPane représentant la carte
     */
    private fun createPileCard(card: Card): StackPane {
        val colorClass = if (card.suit == Card.Suit.HEARTS || card.suit == Card.Suit.DIAMONDS) "card-red" else "card-black"

        return StackPane().apply {
            styleClass.add("card-container")
            prefWidth = 60.0
            prefHeight = 85.0
            children.add(VBox(0.0).apply {
                alignment = Pos.CENTER
                children.addAll(
                    Label(card.rank.displayName).apply { styleClass.addAll("pile-card-rank-label", colorClass) },
                    Label(card.suit.icon).apply { styleClass.addAll("pile-card-suit-label", colorClass) }
                )
            })
        }
    }

    /**
     * Affiche l'état "pile vide".
     */
    private fun showEmpty() {
        cardsContainer.children.clear()
        emptyLabel.isVisible = true
    }

    /**
     * Joue une animation de victoire.
     */
    fun playVictoryAnimation() {
        ParallelTransition(
            RotateTransition(Duration.millis(500.0), this).apply {
                byAngle = 360.0
                cycleCount = 1
            },
            ScaleTransition(Duration.millis(250.0), this).apply {
                toX = 1.2
                toY = 1.2
                isAutoReverse = true
                cycleCount = 2
            }
        ).play()
    }
}
