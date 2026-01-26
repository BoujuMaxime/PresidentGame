package view.dialogs

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.scene.layout.VBox

/**
 * Menu pause affiché en overlay pendant la partie.
 *
 * Propose de reprendre, démarrer une nouvelle partie ou quitter.
 *
 * @property onResume Callback pour reprendre la partie
 * @property onNewGame Callback pour retourner au menu
 * @property onQuit Callback pour quitter l'application
 *
 * @author BOUJU Maxime
 */
class InGameMenuView(
    private val onResume: () -> Unit,
    private val onNewGame: () -> Unit,
    private val onQuit: () -> Unit
) : VBox(15.0) {

    init {
        styleClass.add("ig-menu-panel")
        alignment = Pos.CENTER
        padding = Insets(30.0)
        maxWidth = 300.0
        maxHeight = 350.0

        children.addAll(
            Label("⏸ Menu").apply { styleClass.add("ig-menu-title") },
            Separator(),
            Button("▶ Reprendre").apply {
                styleClass.add("ig-menu-button")
                maxWidth = Double.MAX_VALUE
                setOnAction { onResume() }
            },
            Button("🔄 Nouvelle partie").apply {
                styleClass.add("ig-menu-button")
                maxWidth = Double.MAX_VALUE
                setOnAction { onNewGame() }
            },
            Separator(),
            Button("🚪 Quitter").apply {
                styleClass.addAll("ig-menu-button", "quit-button")
                maxWidth = Double.MAX_VALUE
                setOnAction { onQuit() }
            }
        )
    }
}
