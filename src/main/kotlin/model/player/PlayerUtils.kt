package model.player

import model.Card
import model.PlayerMove

/**
 * Utilitaires pour les joueurs.
 * @author BOUJU Maxime
 */
object PlayerUtils {

    /**
     * Trie la main d'un joueur par rang des cartes.
     *
     * @param hand La main du joueur, une liste mutable de cartes.
     */
    fun sortHandByRank(hand: MutableList<Card>) {
        hand.sortBy { it.rank }
    }

    /**
     * Retourne les coups possibles depuis la main d'un joueur, en respectant les règles du jeu.
     *
     * @param hand La main du joueur, une liste de cartes.
     * @param lastPlayerMove Le dernier coup joué par un autre joueur, ou null si aucun.
     * @param straightRank Le rang de la séquence en cours, ou null si aucune séquence.
     * @return Une liste des coups possibles que le joueur peut effectuer.
     */
    fun possiblePlays(
        hand: List<Card>,
        lastPlayerMove: PlayerMove?,
        straightRank: Card.Rank?
    ): List<PlayerMove> {
        /**
         * Génère toutes les combinaisons possibles de taille `k` à partir d'une liste donnée.
         *
         * @param list La liste d'éléments.
         * @param k La taille des combinaisons à générer.
         * @return Une liste de combinaisons, chaque combinaison étant une liste d'éléments.
         */
        fun <T> combinations(list: List<T>, k: Int): List<List<T>> {
            if (k == 0) return listOf(emptyList())
            if (k > list.size) return emptyList()
            val result = mutableListOf<List<T>>()
            fun recurse(start: Int, comb: MutableList<T>) {
                if (comb.size == k) {
                    result.add(ArrayList(comb))
                    return
                }
                for (i in start until list.size) {
                    comb.add(list[i])
                    recurse(i + 1, comb)
                    comb.removeAt(comb.lastIndex)
                }
            }
            recurse(0, mutableListOf())
            return result
        }

        val playerMoves = mutableListOf<PlayerMove>()
        val groups = hand.groupBy { it.rank }

        // Ajoute les coups simples (une seule carte).
        hand.forEach { card -> playerMoves.add(PlayerMove(listOf(card), PlayerMove.PlayType.SINGLE)) }

        // Ajoute les combinaisons (paires, brelans, carrés).
        groups.forEach { (_, cards) ->
            (2..minOf(4, cards.size)).forEach { size ->
                combinations(cards, size).forEach { combo ->
                    val type = PlayerMove.PlayType.entries[size - 1]
                    playerMoves.add(PlayerMove(combo, type))
                }
            }
        }

        // Filtre les coups selon la règle "straight".
        val filteredByStraight = straightRank?.let { rank ->
            playerMoves.filter { move -> move.any { it.rank == rank } }.ifEmpty { return emptyList() }
        } ?: playerMoves

        // Filtre les coups selon le dernier coup joué.
        val filteredByLastMove = lastPlayerMove?.let { move ->
            filteredByStraight.filter { it.canBePlayedOn(move) }
        } ?: filteredByStraight

        // Trie les coups possibles par rang et type de jeu.
        return filteredByLastMove.sortedWith(
            compareBy(
                { it[0].rank.ordinal },
                { it.playType.ordinal + 1 }
            )
        )
    }

    /**
     * Affiche la main d'un joueur avec un index pour chaque carte.
     *
     * @param hand La main du joueur, une liste de cartes.
     */
    fun printHand(hand: List<Card>) {
        println("Votre main :")
        hand.forEachIndexed { index, card -> println("$index: $card") }
    }
}