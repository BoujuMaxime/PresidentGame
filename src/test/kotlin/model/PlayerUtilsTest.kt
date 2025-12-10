package model

import model.player.PlayerUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PlayerUtilsTest {
    @Test
    fun `possible plays generates singles and pairs`() {
        val hand = listOf(
            Card(Card.Rank.THREE, Card.Suit.CLUBS),
            Card(Card.Rank.FOUR, Card.Suit.DIAMONDS),
            Card(Card.Rank.FOUR, Card.Suit.HEARTS)
        )
        val plays = PlayerUtils.possiblePlays(hand, null)
        // expect 3 singles + 1 pair = 4 plays
        assertTrue(plays.any { it.playType == Play.PlayType.PAIR })
        assertEquals(4, plays.size)
    }

    @Test
    fun `possible plays filters by last play`() {
        val hand = listOf(
            Card(Card.Rank.SIX, Card.Suit.CLUBS),
            Card(Card.Rank.SEVEN, Card.Suit.DIAMONDS)
        )
        val last = Play(listOf(Card(Card.Rank.FIVE, Card.Suit.HEARTS)), Play.PlayType.SINGLE)
        val plays = PlayerUtils.possiblePlays(hand, last)
        // both six and seven are playable on a five
        assertEquals(2, plays.size)
        val ranks = plays.map { it[0].rank }
        assertTrue(ranks.contains(Card.Rank.SIX))
        assertTrue(ranks.contains(Card.Rank.SEVEN))
    }
}
