package model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CardTest {
    @Test
    fun `equality depends on rank and suit`() {
        val heartsAce1 = Card(Card.Rank.ACE, Card.Suit.HEARTS)
        val heartsAce2 = Card(Card.Rank.ACE, Card.Suit.HEARTS)
        val spadesAce = Card(Card.Rank.ACE, Card.Suit.SPADES)
        assertEquals(heartsAce1, heartsAce2)
        assertNotEquals(heartsAce1, spadesAce)
    }

    @Test
    fun `hashCode stable for identical cards`() {
        val card = Card(Card.Rank.KING, Card.Suit.CLUBS)
        val clone = Card(Card.Rank.KING, Card.Suit.CLUBS)
        assertEquals(card.hashCode(), clone.hashCode())
    }

    @Test
    fun `compareTo sorts by rank`() {
        val low = Card(Card.Rank.THREE, Card.Suit.HEARTS)
        val high = Card(Card.Rank.TWO, Card.Suit.HEARTS)
        assertTrue(low < high)
        assertTrue(high > low)
        assertEquals(0, low.compareTo(Card(Card.Rank.THREE, Card.Suit.CLUBS)))
    }

    @Test
    fun `toString prints friendly label`() {
        val card = Card(Card.Rank.QUEEN, Card.Suit.DIAMONDS)
        assertEquals("Dame de Carreau ♦", card.toString())
    }
}