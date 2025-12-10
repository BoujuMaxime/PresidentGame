package model.player

import model.Card
import model.Play

/**
 * Utilitaires du joueur.
 */
object PlayerUtils {
    /**
     * Trie la main en place par rang (ordre croissant selon `Card.rank`).
     *
     * @param hand liste mutable de `Card` à trier.
     */
    fun sortHandByRank(hand: MutableList<Card>) {
        hand.sortBy { it.rank }
    }

    /**
     * Calcule tous les coups possibles que le joueur peut jouer à partir de `hand`.
     * Si `lastPlay` est fourni, ne conserve que les coups pouvant être joués sur `lastPlay`.
     *
     * Le résultat est trié pour assurer une stabilité : d'abord par rang (ordinal de la première
     * carte du coup), puis par type de coup (SINGLE < PAIR < THREE_OF_A_KIND < FOUR_OF_A_KIND).
     *
     * @param hand liste de `Card` représentant la main du joueur.
     * @param lastPlay coup précédent (optionnel) utilisé pour filtrer les coups valides.
     * @return liste de `Play` représentant les coups possibles triés.
     */
    fun possiblePlays(hand: List<Card>, lastPlay: Play?): List<Play> {
        /**
         * Génère toutes les combinaisons (sans ordre) de taille `k` à partir de `list`.
         *
         * @param list source des éléments.
         * @param k taille de chaque combinaison.
         * @return liste des combinaisons (chaque combinaison est une `List<T>`).
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

        val plays = mutableListOf<Play>()

        // Groupement des cartes par rang pour détecter paires, brelans, carrés
        val groups = hand.groupBy { it.rank }

        // Singles : chaque carte individuelle constitue un coup de type SINGLE
        hand.forEach { card ->
            plays.add(Play(listOf(card), Play.PlayType.SINGLE))
        }

        // Pour chaque groupe de même rang, générer les combinaisons de taille 2..4 si possible
        for ((_, cardsOfSameRank) in groups) {
            val maxSize = minOf(4, cardsOfSameRank.size)
            for (size in 2..maxSize) {
                val combos = combinations(cardsOfSameRank, size)
                val playType = when (size) {
                    2 -> Play.PlayType.PAIR
                    3 -> Play.PlayType.THREE_OF_A_KIND
                    4 -> Play.PlayType.FOUR_OF_A_KIND
                    else -> null
                }
                if (playType != null) {
                    combos.forEach { combo ->
                        plays.add(Play(combo, playType))
                    }
                }
            }
        }

        // Filtrer selon lastPlay si présent : ne garder que les coups jouables sur lastPlay
        val filtered = if (lastPlay == null) {
            plays
        } else {
            plays.filter { it.canBePlayedOn(lastPlay) }
        }

        // Tri pour stabilité : par rang ordinal de la première carte puis par type de coup
        return filtered.sortedWith(
            compareBy(
            { it[0].rank.ordinal },
            {
                when (it.playType) {
                    Play.PlayType.SINGLE -> 1
                    Play.PlayType.PAIR -> 2
                    Play.PlayType.THREE_OF_A_KIND -> 3
                    Play.PlayType.FOUR_OF_A_KIND -> 4
                }
            }
        ))
    }

    /**
     * Affiche la main sur la sortie standard avec un index pour chaque carte.
     *
     * @param hand liste de `Card` à afficher.
     */
    fun printHand(hand: List<Card>) {
        println("Votre main :")
        hand.forEachIndexed { index, card -> println("$index: $card") }
    }
}