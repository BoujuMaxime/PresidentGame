package view.dialogs

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import model.Card
import view.components.CardView

/**
 * Dialogue d'échange de cartes au début d'une manche.
 *
 * @property hand Cartes actuelles du joueur
 * @property count Nombre de cartes à échanger
 * @property mustSelectHighest Vrai si sélection imposée
 * @property onConfirm Callback avec les cartes sélectionnées
 */
class CardExchangeDialog(
    hand: List<Card>,
    private val count: Int,
    private val mustSelectHighest: Boolean,
    private val onConfirm: (List<Card>) -> Unit
) : VBox(20.0) {

    private val cardViews = mutableListOf<CardView>()
    private val selectedCards = mutableListOf<Card>()
    private val confirmButton: Button
    private val selectionLabel: Label

    init {
        styleClass.add("card-exchange-dialog")
        alignment = Pos.CENTER
        padding = Insets(30.0)
        maxWidth = 600.0

        val title = Label("🔄 Échange de cartes").apply { styleClass.add("dialog-title") }
        val instruction = Label(
            if (mustSelectHighest) "Vous devez donner vos $count meilleures cartes"
            else "Sélectionnez $count cartes à donner"
        ).apply { styleClass.add("dialog-instruction") }

        val cardsContainer = HBox(-10.0).apply {
            styleClass.add("exchange-cards-container")
            alignment = Pos.CENTER
            padding = Insets(20.0)
        }

        hand.sortedByDescending { it.rank }.forEach { card ->
            val cardView = CardView(card) { handleCardClick(it) }
            cardViews.add(cardView)
            cardsContainer.children.add(cardView)
        }

        if (mustSelectHighest) preselectHighestCards()

        selectionLabel = Label("Sélectionné: ${selectedCards.size}/$count").apply {
            styleClass.add("selection-counter")
        }

        confirmButton = Button("✓ Confirmer").apply {
            styleClass.addAll("action-button", "play-button")
            isDisable = selectedCards.size != count
            setOnAction { if (selectedCards.size == count) onConfirm(selectedCards.toList()) }
        }

        children.addAll(title, instruction, cardsContainer, selectionLabel, confirmButton)
    }

    private fun handleCardClick(cardView: CardView) {
        if (mustSelectHighest) return

        if (cardView.isSelected()) {
            cardView.setSelected(false)
            selectedCards.remove(cardView.card)
        } else if (selectedCards.size < count) {
            cardView.setSelected(true)
            selectedCards.add(cardView.card)
        }
        updateUI()
    }

    private fun preselectHighestCards() {
        selectedCards.clear()
        cardViews.take(count).forEach { cardView ->
            cardView.setSelected(true)
            selectedCards.add(cardView.card)
        }
        cardViews.drop(count).forEach { it.setInteractive(false) }
        updateUI()
    }

    private fun updateUI() {
        selectionLabel.text = "Sélectionné: ${selectedCards.size}/$count"
        confirmButton.isDisable = selectedCards.size != count
    }
}
