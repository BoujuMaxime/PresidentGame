package model

/**
 * Représente une carte avec un rang et une couleur.
 *
 * @property rank Le rang (valeur) de la carte (ex: As, Roi, Dame, etc.).
 * @property suit La couleur de la carte (ex: Trèfle, Carreau, etc.).
 *
 * @author BOUJU Maxime
 */
data class Card(val rank: Rank, val suit: Suit) : Comparable<Card> {

    /**
     * Enumération représentant les couleurs des cartes.
     *
     * @property displayName Le nom affichable de la couleur.
     * @property icon Le symbole associé à la couleur.
     */
    enum class Suit(val displayName: String, val icon: String) {
        CLUBS("Trèfle", "♣"),
        DIAMONDS("Carreau", "♦"),
        HEARTS("Coeur", "♥"),
        SPADES("Pique", "♠");

        /**
         * Retourne une représentation textuelle de la couleur.
         *
         * @return Une chaîne contenant le nom affichable et le symbole.
         */
        override fun toString() = "$displayName $icon"
    }

    /**
     * Enumération représentant les rangs des cartes.
     *
     * @property displayName Le nom affichable du rang.
     */
    enum class Rank(val displayName: String) {
        THREE("3"), FOUR("4"), FIVE("5"), SIX("6"), SEVEN("7"),
        EIGHT("8"), NINE("9"), TEN("10"), JACK("Valet"),
        QUEEN("Dame"), KING("Roi"), ACE("As"), TWO("2");

        /**
         * Retourne une représentation textuelle du rang.
         *
         * @return Une chaîne contenant le nom affichable.
         */
        override fun toString() = displayName
    }

    /**
     * Vérifie si deux cartes sont égales.
     *
     * @param other L'objet à comparer avec cette carte.
     * @return `true` si les deux cartes ont le même rang et la même couleur, sinon `false`.
     */
    override fun equals(other: Any?) = other is Card && rank == other.rank && suit == other.suit

    /**
     * Compare cette carte avec une autre carte en fonction de leur rang.
     *
     * @param other La carte à comparer.
     * @return Un entier négatif, zéro ou un entier positif si cette carte est
     * respectivement inférieure, égale ou supérieure à l'autre carte.
     */
    override fun compareTo(other: Card) = rank.ordinal.compareTo(other.rank.ordinal)

    /**
     * Calcule le code de hachage de la carte.
     *
     * @return Le code de hachage basé sur le rang et la couleur.
     */
    override fun hashCode() = 31 * rank.hashCode() + suit.hashCode()

    /**
     * Retourne une représentation textuelle de la carte.
     *
     * @return Une chaîne au format "Rang de Couleur".
     */
    override fun toString() = "${rank} de ${suit}"
}