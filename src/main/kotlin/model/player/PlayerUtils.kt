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
     * Ajout : vérification de la règle "straigth" à partir de la `pile`.
     *
     * @param hand liste de `Card` représentant la main du joueur.
     * @param lastPlay coup précédent (optionnel) utilisé pour filtrer les coups valides.
     * @param pile pile de cartes jouées (utilisée pour détecter la contrainte straigth).
     * @return liste de `Play` représentant les coups possibles triés.
     */
    fun possiblePlays(hand: List<Card>, lastPlay: Play?, pile: List<Card>, straightRank: Card.Rank?): List<Play> {
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
        val groups = hand.groupBy { it.rank }
        hand.forEach { card -> plays.add(Play(listOf(card), Play.PlayType.SINGLE)) }
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
                    combos.forEach { combo -> plays.add(Play(combo, playType)) }
                }
            }
        }

        val playsAfterStraigth = if (straightRank != null) {
            val candidate = plays.filter { play -> play.any { it.rank == straightRank } }
            candidate.ifEmpty {
                return emptyList()
            }
        } else plays

        val filtered = if (lastPlay == null) {
            playsAfterStraigth
        } else {
            playsAfterStraigth.filter { it.canBePlayedOn(lastPlay) }
        }

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
            )
        )
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