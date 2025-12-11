package model.player

import model.Card
import model.PlayerMove

/**
 * Utilitaires du joueur.
 */
object PlayerUtils {
    /**
     * Trie la main en place par rang (ordre croissant selon `Card.rank`).
     *
     * @param hand Liste mutable de `Card` à trier.
     */
    fun sortHandByRank(hand: MutableList<Card>) {
        hand.sortBy { it.rank }
    }

    /**
     * Calcule tous les coups possibles que le joueur peut jouer à partir de `hand`.
     * Si `lastPlayerMove` est fourni, ne conserve que les coups pouvant être joués sur `lastPlayerMove`.
     *
     * Le résultat est trié pour assurer une stabilité : d'abord par rang (ordinal de la première
     * carte du coup), puis par type de coup (SINGLE < PAIR < THREE_OF_A_KIND < FOUR_OF_A_KIND).
     *
     * Ajout : vérification de la règle "straight" à partir de la `pile`.
     *
     * @param hand Liste de `Card` représentant la main du joueur.
     * @param lastPlayerMove Coup précédent (optionnel) utilisé pour filtrer les coups valides.
     * @param pile Pile de cartes jouées (utilisée pour détecter la contrainte "straight").
     * @param straightRank Rang pour les suites (si applicable).
     * @return Liste de `PlayerMove` représentant les coups possibles triés.
     */
    fun possiblePlays(hand: List<Card>, lastPlayerMove: PlayerMove?, pile: List<Card>, straightRank: Card.Rank?): List<PlayerMove> {
        /**
         * Génère toutes les combinaisons possibles de taille `k` à partir d'une liste donnée.
         *
         * @param list Liste source.
         * @param k Taille des combinaisons.
         * @return Liste de combinaisons.
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

        // Ajoute les combinaisons de cartes du même rang (paires, brelans, carrés).
        for ((_, cardsOfSameRank) in groups) {
            val maxSize = minOf(4, cardsOfSameRank.size)
            for (size in 2..maxSize) {
                val combos = combinations(cardsOfSameRank, size)
                val playerMoveType = when (size) {
                    2 -> PlayerMove.PlayType.PAIR
                    3 -> PlayerMove.PlayType.THREE_OF_A_KIND
                    4 -> PlayerMove.PlayType.FOUR_OF_A_KIND
                    else -> null
                }
                if (playerMoveType != null) {
                    combos.forEach { combo -> playerMoves.add(PlayerMove(combo, playerMoveType)) }
                }
            }
        }

        // Filtre les coups selon la règle "straight" si applicable.
        val playsAfterStraight = if (straightRank != null) {
            val candidate = playerMoves.filter { play -> play.any { it.rank == straightRank } }
            candidate.ifEmpty {
                return emptyList()
            }
        } else playerMoves

        // Filtre les coups valides selon le dernier coup joué.
        val filtered = if (lastPlayerMove == null) {
            playsAfterStraight
        } else {
            playsAfterStraight.filter { it.canBePlayedOn(lastPlayerMove) }
        }

        // Trie les coups par rang et type.
        return filtered.sortedWith(
            compareBy(
                { it[0].rank.ordinal },
                {
                    when (it.playType) {
                        PlayerMove.PlayType.SINGLE -> 1
                        PlayerMove.PlayType.PAIR -> 2
                        PlayerMove.PlayType.THREE_OF_A_KIND -> 3
                        PlayerMove.PlayType.FOUR_OF_A_KIND -> 4
                    }
                }
            )
        )
    }

    /**
     * Affiche la main sur la sortie standard avec un index pour chaque carte.
     *
     * @param hand Liste de `Card` à afficher.
     */
    fun printHand(hand: List<Card>) {
        println("Votre main :")
        hand.forEachIndexed { index, card -> println("$index: $card") }
    }
}