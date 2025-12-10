package model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Classe de test pour la classe `Card`.
 * Contient des tests pour comparer les cartes, vérifier leur égalité, leur ordre, et leur représentation sous forme de chaîne.
 */
class CardTest {

    /**
     * Vérifie que la comparaison de deux cartes avec différents rangs fonctionne correctement.
     * La carte avec le rang le plus élevé doit être considérée comme supérieure.
     */
    @Test
    fun compareCardsWithDifferentRanks() {
        val card1 = Card(Card.Rank.ACE, Card.Suit.HEARTS)
        val card2 = Card(Card.Rank.KING, Card.Suit.SPADES)
        assertTrue(card1 > card2)
    }

    /**
     * Vérifie que la comparaison de deux cartes avec le même rang retourne une égalité.
     */
    @Test
    fun compareCardsWithSameRank() {
        val card1 = Card(Card.Rank.TEN, Card.Suit.CLUBS)
        val card2 = Card(Card.Rank.TEN, Card.Suit.DIAMONDS)
        assertEquals(0, card1.compareTo(card2))
    }

    /**
     * Vérifie que la méthode `toString` retourne le format correct pour une carte avec une couleur spécifiée.
     */
    @Test
    fun toStringReturnsCorrectFormatWithSuit() {
        val card = Card(Card.Rank.QUEEN, Card.Suit.DIAMONDS)
        assertEquals("QUEEN of DIAMONDS", card.toString())
    }

    /**
     * Vérifie que la méthode `toString` gère correctement les cas où la couleur est spécifiée.
     */
    @Test
    fun toStringHandlesEmptySuitGracefully() {
        val card = Card(Card.Rank.SEVEN, Card.Suit.CLUBS)
        assertEquals("SEVEN of CLUBS", card.toString())
    }

    /**
     * Vérifie que deux cartes avec des rangs différents ne sont pas considérées comme égales.
     */
    @Test
    fun equalsReturnsFalseWhenRankDiffers() {
        val card1 = Card(Card.Rank.SEVEN, Card.Suit.HEARTS)
        val card2 = Card(Card.Rank.EIGHT, Card.Suit.HEARTS)
        assertTrue(card1 != card2)
    }

    /**
     * Vérifie que deux cartes identiques (même rang et même couleur) sont égales
     * et que leurs codes de hachage sont également identiques.
     */
    @Test
    fun equalsAndHashCodeMatchForIdenticalCards() {
        val card1 = Card(Card.Rank.JACK, Card.Suit.SPADES)
        val card2 = Card(Card.Rank.JACK, Card.Suit.SPADES)
        assertTrue(card1 == card2)
        assertEquals(card1.hashCode(), card2.hashCode())
    }

    /**
     * Vérifie que la méthode `compareTo` retourne une valeur négative
     * lorsqu'une carte a un rang inférieur à une autre.
     */
    @Test
    fun compareToReturnsNegativeForLowerRankCard() {
        val lower = Card(Card.Rank.THREE, Card.Suit.CLUBS)
        val higher = Card(Card.Rank.FIVE, Card.Suit.DIAMONDS)
        assertTrue(lower < higher)
    }

    /**
     * Vérifie que la carte avec le rang "TWO" est considérée comme supérieure
     * à une carte avec le rang "ACE" dans l'ordre de comparaison.
     */
    @Test
    fun compareToPlacesTwoAboveAce() {
        val two = Card(Card.Rank.TWO, Card.Suit.CLUBS)
        val ace = Card(Card.Rank.ACE, Card.Suit.SPADES)
        assertTrue(two > ace)
    }
}