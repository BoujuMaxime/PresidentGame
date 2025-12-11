package model.player

import model.Card

/**
 * Classe abstraite représentant un joueur dans le jeu.
 *
 * @property id Identifiant unique du joueur.
 * @property hand Main du joueur contenant ses cartes.
 * @property role Rôle actuel du joueur, par défaut `Role.NEUTRAL`.
 * @author BOUJU Maxime
 */
abstract class Player(
    val id: String,
    val hand: MutableList<Card>,
    var role: Role = Role.NEUTRAL
) : PlayerInterface {

    /**
     * Enumération des rôles possibles pour un joueur.
     *
     * @property displayName Nom affichable du rôle.
     */
    enum class Role(val displayName: String) {
        PRESIDENT("Président"),             // Rôle de président.
        VICE_PRESIDENT("Vice-Président"),   // Rôle de vice-président.
        NEUTRAL("Neutre"),                  // Rôle neutre, par défaut.
        VICE_ASSHOLE("Vice-Trou-du-Cul"),   // Rôle de vice-trou-du-cul.
        ASSHOLE("Trou-du-Cul");             // Rôle de trou-du-cul.

        /**
         * Retourne le nom affichable du rôle.
         *
         * @return Le nom affichable du rôle.
         */
        override fun toString() = displayName
    }
}