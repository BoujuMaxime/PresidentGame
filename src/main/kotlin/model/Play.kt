package model

import java.util.function.IntFunction

/**
 * Représente un ensemble de cartes jouées dans un jeu, avec un type de jeu spécifique.
 *
 * @property cards La liste des cartes constituant ce jeu.
 * @property playType Le type de jeu (SINGLE, PAIR, THREE_OF_A_KIND, FOUR_OF_A_KIND).
 * Par défaut, il s'agit de SINGLE.
 */
class Play(
    private val cards: List<Card>,
    val playType: PlayType = PlayType.SINGLE
) : List<Card> by cards {

    init {
        // Vérifie que la combinaison de cartes est valide pour le type de jeu spécifié.
        require(isValid()) { "Combinaison de cartes invalide pour le type $playType" }
    }

    /**
     * Vérifie si la combinaison de cartes est valide pour le type de jeu.
     *
     * @return `true` si la combinaison est valide, sinon `false`.
     */
    private fun isValid(): Boolean = when (playType) {
        PlayType.SINGLE -> cards.size == 1
        PlayType.PAIR -> cards.size == 2 && cards.all { it.rank == cards[0].rank }
        PlayType.THREE_OF_A_KIND -> cards.size == 3 && cards.all { it.rank == cards[0].rank }
        PlayType.FOUR_OF_A_KIND -> cards.size == 4 && cards.all { it.rank == cards[0].rank }
    }

    /**
     * Détermine si ce jeu peut être joué sur un autre jeu.
     *
     * @param top Le jeu actuellement au sommet de la pile, ou `null` s'il n'y a pas de jeu précédent.
     * @return `true` si ce jeu peut être joué, sinon `false`.
     */
    fun canBePlayedOn(top: Play?): Boolean {
        if (top == null) return true // Premier coup du pli
        if (this.playType != top.playType) return false // On joue le même nombre de cartes
        // On compare la valeur des cartes (toutes les cartes d'un play ont le même rang).
        return this.cards[0].rank.ordinal >= top.cards[0].rank.ordinal
    }

    /**
     * Retourne une représentation textuelle du jeu.
     *
     * @return Une chaîne de caractères représentant le type de jeu et les cartes.
     */
    override fun toString(): String = "$playType: ${cards.joinToString(", ")}"

    /**
     * Méthode héritée de l'interface `List`. Dépréciée en Kotlin.
     */
    @Deprecated("Deprecated in Kotlin")
    override fun <T : Any?> toArray(generator: IntFunction<Array<out T?>?>): Array<out T?>? {
        return super.toArray(generator)
    }

    /**
     * Enumération représentant les différents types de jeux possibles.
     */
    enum class PlayType {
        SINGLE, PAIR, THREE_OF_A_KIND, FOUR_OF_A_KIND
    }
}