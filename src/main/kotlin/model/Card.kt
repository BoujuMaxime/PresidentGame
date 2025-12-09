package model

/**
 * Représente une carte à jouer avec une valeur et une couleur.
 *
 * @property rank La valeur de la carte, définie par l'énumération `Rank`.
 * @property suit La couleur de la carte, définie par l'énumération `Suit`.
 */
data class Card(val rank: Rank, val suit: Suit) : Comparable<Card> {

    /**
     * Énumération représentant les couleurs possibles d'une carte.
     */
    enum class Suit { CLUBS, DIAMONDS, HEARTS, SPADES }

    /**
     * Énumération représentant les valeurs possibles d'une carte.
     * Les valeurs sont ordonnées selon leur puissance dans le jeu.
     */
    enum class Rank { THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE, TWO }

    /**
     * Vérifie l'égalité entre deux cartes.
     * Deux cartes sont égales si elles ont la même valeur (`rank`) et la même couleur (`suit`).
     *
     * @param other L'objet à comparer avec cette carte.
     * @return `true` si les deux cartes sont égales, `false` sinon.
     */
    override fun equals(other: Any?): Boolean =
        other is Card && rank == other.rank && suit == other.suit

    /**
     * Compare cette carte avec une autre carte en fonction de leur valeur (`rank`).
     *
     * @param other La carte à comparer.
     * @return Un entier négatif, zéro ou un entier positif si cette carte est respectivement
     * moins, égale ou supérieure à l'autre carte.
     */
    override fun compareTo(other: Card): Int = rank.ordinal.compareTo(other.rank.ordinal)

    /**
     * Génère un code de hachage unique pour cette carte, basé sur sa valeur et sa couleur.
     *
     * @return Le code de hachage de cette carte.
     */
    override fun hashCode(): Int = 31 * rank.hashCode() + suit.hashCode()

    /**
     * Retourne une représentation textuelle de la carte sous la forme "RANK of SUIT".
     *
     * @return Une chaîne de caractères représentant la carte.
     */
    override fun toString(): String = "${rank.name} of ${suit.name}"
}