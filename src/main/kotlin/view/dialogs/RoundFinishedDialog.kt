package view.dialogs

import controller.GameController
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import model.player.Player

/**
 * Dialogue de fin de manche affichant le classement.
 *
 * Présente les joueurs triés par rôle avec médailles et badges.
 *
 * @property playersInfo Informations des joueurs à afficher
 * @property onContinue Callback pour passer à la manche suivante
 * @property onBackToMenu Callback pour retourner au menu
 *
 * @author BOUJU Maxime
 */
class RoundFinishedDialog(
    private val playersInfo: List<GameController.PlayerInfo>,
    private val onContinue: () -> Unit,
    private val onBackToMenu: () -> Unit
) : VBox(20.0) {

    init {
        styleClass.add("round-finished-dialog")
        alignment = Pos.CENTER
        padding = Insets(30.0)
        maxWidth = 400.0

        children.addAll(
            Label("🏆 Manche terminée !").apply { styleClass.add("dialog-title") },
            Label("Classement des joueurs").apply { styleClass.add("dialog-subtitle") },
            createRankingBox(),
            HBox(15.0).apply {
                alignment = Pos.CENTER
                children.addAll(
                    Button("▶ Continuer").apply {
                        styleClass.addAll("action-button", "play-button")
                        setOnAction { onContinue() }
                    },
                    Button("🏠 Menu").apply {
                        styleClass.addAll("action-button", "menu-button")
                        setOnAction { onBackToMenu() }
                    }
                )
            }
        )
    }

    /**
     * Crée la boîte de classement.
     *
     * @return VBox contenant les lignes de classement
     */
    private fun createRankingBox(): VBox = VBox(10.0).apply {
        styleClass.add("ranking-box")
        alignment = Pos.CENTER
        padding = Insets(15.0)

        playersInfo.sortedBy { roleOrder(it.role) }.forEachIndexed { index, info ->
            children.add(createRankingRow(index + 1, info))
        }
    }

    /**
     * Retourne l'ordre d'affichage selon le rôle.
     *
     * @param role Rôle du joueur
     * @return Position de tri
     */
    private fun roleOrder(role: Player.Role): Int = when (role) {
        Player.Role.PRESIDENT -> 0
        Player.Role.VICE_PRESIDENT -> 1
        Player.Role.NEUTRAL -> 2
        Player.Role.VICE_ASSHOLE -> 3
        Player.Role.ASSHOLE -> 4
    }

    /**
     * Crée une ligne de classement.
     *
     * @param position Position (1=premier)
     * @param info Informations du joueur
     * @return HBox représentant la ligne
     */
    private fun createRankingRow(position: Int, info: GameController.PlayerInfo): HBox = HBox(15.0).apply {
        styleClass.addAll("ranking-row", getRoleClass(info.role))
        alignment = Pos.CENTER_LEFT
        padding = Insets(10.0, 15.0, 10.0, 15.0)

        children.addAll(
            Label(getPositionEmoji(position)).apply {
                styleClass.add("ranking-position")
                minWidth = 30.0
            },
            Label(info.name).apply {
                styleClass.add("ranking-name")
                minWidth = 100.0
            },
            Label(info.role.displayName).apply {
                styleClass.addAll("ranking-role", getRoleBadgeClass(info.role))
            }
        )
    }

    /**
     * Retourne l'emoji de position.
     *
     * @param position Position du joueur
     * @return Emoji médaille ou numéro
     */
    private fun getPositionEmoji(position: Int): String = when (position) {
        1 -> "🥇"; 2 -> "🥈"; 3 -> "🥉"; else -> "$position."
    }

    /**
     * Retourne la classe CSS de fond selon le rôle.
     *
     * @param role Rôle du joueur
     * @return Nom de la classe CSS
     */
    private fun getRoleClass(role: Player.Role): String = when (role) {
        Player.Role.PRESIDENT -> "row-president"
        Player.Role.VICE_PRESIDENT -> "row-vice-president"
        Player.Role.VICE_ASSHOLE -> "row-vice-asshole"
        Player.Role.ASSHOLE -> "row-asshole"
        else -> "row-neutral"
    }

    /**
     * Retourne la classe CSS du badge selon le rôle.
     *
     * @param role Rôle du joueur
     * @return Nom de la classe CSS
     */
    private fun getRoleBadgeClass(role: Player.Role): String = when (role) {
        Player.Role.PRESIDENT -> "role-president"
        Player.Role.VICE_PRESIDENT -> "role-vice-president"
        Player.Role.VICE_ASSHOLE -> "role-vice-asshole"
        Player.Role.ASSHOLE -> "role-asshole"
        else -> "role-neutral"
    }
}
