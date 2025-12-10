package model

import java.util.function.IntFunction

/**
 * Représente un coup joué par un joueur, qui est une combinaison de cartes.
 *
 * @property cards La liste des cartes jouées dans ce coup.
 * @property playType Le type de combinaison représentée par ce coup (ex: SINGLE, PAIR, STRAIGHT, etc.).
 */
class Play(
    private val cards: List<Card>, // Liste des cartes jouées
    val playType: PlayType = PlayType.SINGLE // Type de combinaison par défaut : SINGLE
) : List<Card> by cards {

    /**
     * Retourne la taille de la liste des cartes jouées.
     */
    override val size: Int
        get() = cards.size

    /**
     * Retourne une représentation textuelle du coup, incluant le type de combinaison
     * et les cartes jouées.
     *
     * @return Une chaîne de caractères représentant le coup.
     */
    override fun toString(): String =
        "$playType: ${cards.joinToString(", ")}"

    @Deprecated(level = DeprecationLevel.HIDDEN, message = "Deprecated in Kotlin")
    override fun <T : Any?> toArray(generator: IntFunction<Array<out T?>?>): Array<out T?>? {
        return super.toArray(generator)
    }

    /**
     * Enumération des différents types de combinaisons possibles pour un coup.
     */
    enum class PlayType {
        SINGLE, // Une seule carte
        PAIR, // Une paire de cartes
        STRAIGHT, // Une suite de cartes
        FOUR_OF_A_KIND // Un carré (quatre cartes identiques)
    }
}