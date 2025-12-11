package model

import java.util.function.IntFunction

/**
 * Représente un ensemble de cartes jouées par un joueur, avec un type de jeu spécifique.
 *
 * @property cards La liste des cartes posées par le joueur.
 * @property playType Le type de jeu (SINGLE, PAIR, THREE_OF_A_KIND, FOUR_OF_A_KIND).
 */
class PlayerMove(
    private val cards: List<Card>,
    val playType: PlayType = PlayType.SINGLE
) : List<Card> by cards {

    init {
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
     * Retourne le rang des cartes posées par le joueur, par exemple ACE, KING, QUEEN, etc.
     *
     * @return Le rang des cartes.
     */
    fun getRank(): Card.Rank {
        return cards[0].rank
    }

    /**
     * Détermine si ce jeu peut être joué sur un autre jeu.
     *
     * @param top Le jeu actuellement au sommet de la pile, ou `null` s'il n'y a pas de jeu précédent.
     * @return `true` si ce jeu peut être joué, sinon `false`.
     */
    fun canBePlayedOn(top: PlayerMove?): Boolean {
        if (top == null) return true
        if (this.playType != top.playType) return false
        return this.cards[0].rank.ordinal >= top.cards[0].rank.ordinal
    }

    /**
     * Représentation textuelle du coup.
     *
     * @return Chaîne descriptive du coup.
     */
    override fun toString(): String {
        val cardsStr = cards.joinToString(", ") { it.toString() }
        return "${playType.displayName.trimEnd()} ${getRank()}: $cardsStr"
    }

    /**
     * Méthode héritée de l'interface `List`. Dépréciée en Kotlin.
     */
    @Deprecated("Deprecated in Kotlin")
    override fun <T : Any?> toArray(generator: IntFunction<Array<out T?>?>): Array<out T?>? {
        return super.toArray(generator)
    }

    /**
     * Enumération représentant les différents types de jeux possibles.
     *
     * @property displayName Libellé affichable du type de jeu.
     */
    enum class PlayType(val displayName: String) {
        SINGLE("Un "), PAIR("Une paire de "), THREE_OF_A_KIND("Un brelant de "), FOUR_OF_A_KIND("Un carré de ");

        override fun toString() = displayName
    }
}