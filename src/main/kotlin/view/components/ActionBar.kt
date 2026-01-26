package view.components

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.layout.HBox

/**
 * Barre d'actions du joueur humain.
 *
 * Contient les boutons Jouer, Passer et Trier.
 *
 * @property onPlay Callback pour jouer les cartes sélectionnées
 * @property onPass Callback pour passer son tour
 * @property onSort Callback pour trier sa main
 * @property playButton Bouton Jouer
 * @property passButton Bouton Passer
 * @property sortButton Bouton Trier
 *
 * @author BOUJU Maxime
 */
class ActionBar(
    private val onPlay: () -> Unit,
    private val onPass: () -> Unit,
    private val onSort: () -> Unit
) : HBox(15.0) {

    private val playButton: Button
    private val passButton: Button
    private val sortButton: Button

    init {
        styleClass.add("button-pane")
        alignment = Pos.CENTER
        padding = Insets(10.0)

        playButton = Button("▶ Jouer").apply {
            styleClass.addAll("action-button", "play-button")
            setOnAction { onPlay() }
            isDisable = true
        }

        passButton = Button("⏭ Passer").apply {
            styleClass.addAll("action-button", "pass-button")
            setOnAction { onPass() }
            isDisable = true
        }

        sortButton = Button("🔄 Trier").apply {
            styleClass.addAll("action-button", "sort-button")
            setOnAction { onSort() }
        }

        children.addAll(playButton, passButton, sortButton)
    }

    /**
     * Active ou désactive le bouton Jouer.
     *
     * @param enabled Vrai pour activer
     */
    fun setPlayEnabled(enabled: Boolean) { playButton.isDisable = !enabled }

    /**
     * Active ou désactive le bouton Passer.
     *
     * @param enabled Vrai pour activer
     */
    fun setPassEnabled(enabled: Boolean) { passButton.isDisable = !enabled }

    /**
     * Active ou désactive le bouton Trier.
     *
     * @param enabled Vrai pour activer
     */
    fun setSortEnabled(enabled: Boolean) { sortButton.isDisable = !enabled }
}
