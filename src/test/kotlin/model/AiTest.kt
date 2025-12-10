package model

import model.player.ai.EvaluateAi
import model.player.ai.RandomAi
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AiTest {
    @Test
    fun `random ai returns null when no plays`() {
        val ai = RandomAi("ai1", mutableListOf())
        val play = ai.playTurn(mutableListOf(), mutableListOf(), null)
        assertNull(play)
    }

    @Test
    fun `evaluate ai plays lowest when starting`() {
        val hand = mutableListOf(
            Card(Card.Rank.FOUR, Card.Suit.CLUBS),
            Card(Card.Rank.SEVEN, Card.Suit.DIAMONDS)
        )
        val ai = EvaluateAi("ai2", hand)
        val play = ai.playTurn(mutableListOf(), mutableListOf(), null)
        assertNotNull(play)
        assertEquals(Play.PlayType.SINGLE, play!!.playType)
        assertEquals(Card.Rank.FOUR, play[0].rank)
    }
}

