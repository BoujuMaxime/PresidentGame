package model

import model.player.Player

/**
 * Classe représentant le déroulement d'une partie de Président.
 *
 * @property parameters Les paramètres de la partie, définis par la classe `GameParameters`.
 * @property players La liste des joueurs participant à la partie.
 */
class Game(val parameters: GameParameters, val players: MutableList<Player> = mutableListOf()) {

    /**
     * Classe imbriquée contenant les paramètres de configuration de la partie.
     *
     * @property nbPlayers Le nombre de joueurs dans la partie (par défaut 4).
     * @property gameMode Le mode de jeu (LOCAL ou REMOTE).
     * @property aiDifficulty Le niveau de difficulté des IA (EASY, MEDIUM, HARD).
     * @property gameModeParameters Les paramètres spécifiques au mode de jeu.
     */
    data class GameParameters(
        val nbPlayers: Int = 4,
        val gameMode: GameMode = GameMode.LOCAL,
        val aiDifficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
        val gameModeParameters: GameModeParameters = GameModeParameters()
    ) {
        /**
         * Enumération représentant les niveaux de difficulté des IA.
         */
        enum class DifficultyLevel { EASY, MEDIUM, HARD }

        /**
         * Enumération représentant les modes de jeu possibles.
         */
        enum class GameMode { LOCAL, REMOTE }

        /**
         * Classe imbriquée contenant les paramètres spécifiques au mode de jeu.
         *
         * @property withCarreMagique Active ou désactive la règle `Carré Magique`.
         *   Description : règle optionnelle qui s'applique lorsqu'à l'intérieur d'un même pli
         *   les quatre cartes d'une même valeur sont jouées consécutivement (quelle que soit
         *   leur provenance). Dans ce cas, le joueur ayant posé la quatrième carte remporte
         *   immédiatement le pli ; la pile est vidée et ce joueur mène le pli suivant.
         *
         * @property withTaGueule Active ou désactive la règle `Ta Gueule`.
         *   Description : règle optionnelle qui s'applique aux poses consécutives de cartes
         *   du même rang. Si, au cours d'un pli, deux joueurs posent consécutivement une
         *   carte (ou une combinaison) de la même valeur, le joueur suivant est tenu, si possible,
         *   de jouer la même valeur pour continuer le pli ; s'il ne peut ou ne veut pas le faire,
         *   la règle `Ta Gueule` l'oblige à passer et le jeu continue avec le joueur suivant.
         */
        data class GameModeParameters(
            val withCarreMagique: Boolean = true,
            val withTaGueule: Boolean = true
        )
    }


    /**
     * Initialise et démarre la partie.
     * Cette méthode doit être appelée après avoir ajouté les joueurs à la liste `players`.
     */
    fun startGame() {
        TODO("Implémenter la logique de démarrage de la partie")

        // Début boucle principale du jeu

            // Distribution

            // Échange des cartes (pas d'échange pour la première partie)

            // Début de la partie

            // Fin de la partie

            // Attribution des rôles

        // Fin boucle principale du jeu
    }
}