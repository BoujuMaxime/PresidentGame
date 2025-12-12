package model

import model.player.PlayerUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PlayerUtilsTest {

    @BeforeEach
    fun disableConsoleOutput() {
        Utils.setConsoleEnabled(false)
    }

    private fun card(rank: Card.Rank, suit: Card.Suit) = Card(rank, suit)

    @Test
    fun `sortHandByRank orders cards by rank`() {
        val hand = mutableListOf(
            card(Card.Rank.KING, Card.Suit.SPADES),
            card(Card.Rank.THREE, Card.Suit.DIAMONDS),
            card(Card.Rank.ACE, Card.Suit.HEARTS)
        )

        PlayerUtils.sortHandByRank(hand)

        assertEquals(
            listOf(Card.Rank.THREE, Card.Rank.KING, Card.Rank.ACE),
            hand.map { it.rank }
        )
    }

    @Test
    fun `possiblePlays returns pairs and singles when nothing blocks`() {
        val hand = listOf(
            card(Card.Rank.FOUR, Card.Suit.HEARTS),
            card(Card.Rank.FOUR, Card.Suit.SPADES),
            card(Card.Rank.SIX, Card.Suit.CLUBS)
        )

        val plays = PlayerUtils.possiblePlays(hand, null, emptyList(), null)

        assertTrue(plays.any { it.playType == PlayerMove.PlayType.PAIR })
        assertTrue(plays.any { it.playType == PlayerMove.PlayType.SINGLE })
        assertTrue(plays.any { it.getRank() == Card.Rank.FOUR })
    }

    @Test
    fun `possiblePlays honors last play requirements`() {
        val hand = listOf(
            card(Card.Rank.SEVEN, Card.Suit.HEARTS),
            card(Card.Rank.NINE, Card.Suit.SPADES)
        )
        val lastPlayerMove = PlayerMove(listOf(card(Card.Rank.SIX, Card.Suit.CLUBS)), PlayerMove.PlayType.SINGLE)

        val plays = PlayerUtils.possiblePlays(hand, lastPlayerMove, emptyList(), null)

        assertFalse(plays.isEmpty())
        assertTrue(plays.all { it.playType == PlayerMove.PlayType.SINGLE })
        assertTrue(plays.all { it.getRank().ordinal >= Card.Rank.SIX.ordinal })
    }

    @Test
    fun `possiblePlays restricts to straight rank when provided`() {
        val hand = listOf(
            card(Card.Rank.TWO, Card.Suit.CLUBS),
            card(Card.Rank.FIVE, Card.Suit.HEARTS)
        )

        val plays = PlayerUtils.possiblePlays(hand, null, emptyList(), Card.Rank.TWO)

        assertEquals(1, plays.size)
        assertEquals(Card.Rank.TWO, plays.first().getRank())
    }
}
