package model.player.ai

import model.PlayerMove

object AiUtils {
    /** Choisit aléatoirement un PlayerMove dans la liste, ou null si la liste est vide. */
    fun chooseRandomPlay(playerMoves: List<PlayerMove>): PlayerMove? {
        if (playerMoves.isEmpty()) return null
        return playerMoves.shuffled().first()
    }

    /**
     * Évalue les coups possibles et retourne la liste des coups évalués,
     * dédupliquée par (playType, rank) et triée du meilleur au moins bon.
     *
     * Stratégie simple :
     * - On privilégie d'abord les coups qui débarrassent le plus de cartes (taille du coup).
     * - Ensuite on favorise les cartes de rang faible (plus faciles à se débarrasser).
     *
     * @return Liste triée des PlayerMove uniques.
     */
    fun evaluatePossibleMoves(playerMoves: List<PlayerMove>): List<PlayerMove> {
        if (playerMoves.isEmpty()) return emptyList()

        // Déduplication : un seul représentant par (playType, rank)
        val uniqueMoves = playerMoves
            .groupBy { Pair(it.playType, it.getRank()) }
            .mapValues { (_, moves) -> moves.first() } // on prend simplement le premier représentant
            .values
            .toList()

        // Déterminer l'ordinal maximum de rang présent pour calcul de poids
        val maxRankOrdinal = playerMoves.maxOf { it.getRank().ordinal }

        // Calculer un score simple pour chaque coup
        val scored = uniqueMoves.map { move ->
            val countScore = move.size * 100      // priorité forte pour éliminer plusieurs cartes
            val weakRankScore = (maxRankOrdinal - move.getRank().ordinal) * 5 // favoriser rangs faibles
            val totalScore = countScore + weakRankScore
            move to totalScore
        }

        // Retourner la liste triée par score décroissant
        return scored
            .sortedByDescending { it.second }
            .map { it.first }
    }
}