package model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PlayTest {
    @Test
    fun `single can be played on null`() {
        val card = Card(Card.Rank.THREE, Card.Suit.CLUBS)
        val play = Play(listOf(card), Play.PlayType.SINGLE)
        assertTrue(play.canBePlayedOn(null))
    }

    @Test
    fun `pair cannot be played on single`() {
        val c1 = Card(Card.Rank.FIVE, Card.Suit.CLUBS)
        val c2 = Card(Card.Rank.FIVE, Card.Suit.DIAMONDS)
        val pair = Play(listOf(c1, c2), Play.PlayType.PAIR)
        val single = Play(listOf(Card(Card.Rank.FOUR, Card.Suit.HEARTS)), Play.PlayType.SINGLE)
        assertFalse(pair.canBePlayedOn(single))
    }

    @Test
    fun `higher single beats lower single`() {
        val low = Play(listOf(Card(Card.Rank.SIX, Card.Suit.CLUBS)), Play.PlayType.SINGLE)
        val high = Play(listOf(Card(Card.Rank.SEVEN, Card.Suit.DIAMONDS)), Play.PlayType.SINGLE)
        assertTrue(high.canBePlayedOn(low))
    }

    @Test
    fun `four of a kind valid and compares`() {
        val cards = listOf(
            Card(Card.Rank.TEN, Card.Suit.CLUBS),
            Card(Card.Rank.TEN, Card.Suit.DIAMONDS),
            Card(Card.Rank.TEN, Card.Suit.HEARTS),
            Card(Card.Rank.TEN, Card.Suit.SPADES)
        )
        val four = Play(cards, Play.PlayType.FOUR_OF_A_KIND)
        assertTrue(four.canBePlayedOn(null))
    }
}

