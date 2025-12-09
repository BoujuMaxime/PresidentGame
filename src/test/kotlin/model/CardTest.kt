package model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Classe de test pour la classe `Card`.
 * Contient des tests pour comparer les cartes et vérifier leur représentation sous forme de chaîne.
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
}