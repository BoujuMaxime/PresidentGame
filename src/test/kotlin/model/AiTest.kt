package model

import model.player.PlayerUtils
import model.player.ai.EvaluateAi
import model.player.ai.RandomAi
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AiTest {
    private fun card(rank: Card.Rank, suit: Card.Suit) = Card(rank, suit)

    @Test
    fun `random ai sorts board when receiving new cards`() {
        val ai = RandomAi(
            id = "random",
            hand = mutableListOf(card(Card.Rank.KING, Card.Suit.SPADES))
        )

        ai.hand.addAll(
            listOf(card(Card.Rank.FOUR, Card.Suit.HEARTS), card(Card.Rank.NINE, Card.Suit.CLUBS))
        )
        PlayerUtils.sortHandByRank(ai.hand)

        assertEquals(
            listOf(
                card(Card.Rank.FOUR, Card.Suit.HEARTS),
                card(Card.Rank.NINE, Card.Suit.CLUBS),
                card(Card.Rank.KING, Card.Suit.SPADES)
            ),
            ai.hand
        )
    }

    @Test
    fun `random ai returns null when no possible plays`() {
        val ai = RandomAi(id = "idle", hand = mutableListOf())
        val play = ai.playTurn(mutableListOf(), mutableListOf(), null, null)
        assertNull(play)
    }

    @Test
    fun `evaluate ai plays the lowest possible card when leading`() {
        val ai = EvaluateAi(
            id = "eval",
            hand = mutableListOf(
                card(Card.Rank.SEVEN, Card.Suit.CLUBS),
                card(Card.Rank.THREE, Card.Suit.DIAMONDS),
                card(Card.Rank.NINE, Card.Suit.HEARTS)
            )
        )

        val play = ai.playTurn(mutableListOf(), mutableListOf(), null, null)
        assertNotNull(play)
        assertEquals(Card.Rank.THREE, play?.getRank())
        assertEquals(PlayerMove.PlayType.SINGLE, play?.playType)
    }

    @Test
    fun `evaluate ai respects last play requirements`() {
        val ai = EvaluateAi(
            id = "eval",
            hand = mutableListOf(
                card(Card.Rank.SEVEN, Card.Suit.CLUBS),
                card(Card.Rank.TEN, Card.Suit.SPADES)
            )
        )
        val lastPlayerMove = PlayerMove(listOf(card(Card.Rank.EIGHT, Card.Suit.HEARTS)), PlayerMove.PlayType.SINGLE)

        val play = ai.playTurn(mutableListOf(), mutableListOf(), lastPlayerMove, null)
        assertNotNull(play)
        assertEquals(PlayerMove.PlayType.SINGLE, play?.playType)
        assertEquals(Card.Rank.TEN, play?.getRank())
    }

    @Test
    fun `evaluate ai returns null when it cannot beat last play`() {
        val ai = EvaluateAi(
            id = "eval",
            hand = mutableListOf(card(Card.Rank.FIVE, Card.Suit.CLUBS))
        )
        val lastPlayerMove = PlayerMove(listOf(card(Card.Rank.SIX, Card.Suit.DIAMONDS)), PlayerMove.PlayType.SINGLE)

        val play = ai.playTurn(mutableListOf(), mutableListOf(), lastPlayerMove, null)
        assertNull(play)
    }
}
