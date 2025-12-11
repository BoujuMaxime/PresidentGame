package model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PlayTest {
    private fun card(rank: Card.Rank, suit: Card.Suit) = Card(rank, suit)

    @Test
    fun `valid single play is allowed`() {
        val play = Play(listOf(card(Card.Rank.THREE, Card.Suit.CLUBS)), Play.PlayType.SINGLE)
        assertEquals(Play.PlayType.SINGLE, play.playType)
        assertEquals(Card.Rank.THREE, play.getRank())
    }

    @Test
    fun `pair constructor rejects mixed ranks`() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            Play(
                listOf(card(Card.Rank.FOUR, Card.Suit.HEARTS), card(Card.Rank.FIVE, Card.Suit.HEARTS)),
                Play.PlayType.PAIR
            )
        }
        assertEquals("Combinaison de cartes invalide pour le type ${Play.PlayType.PAIR}", ex.message)
    }

    @Test
    fun `canBePlayedOn requires same type and higherOrEqual rank`() {
        val top = Play(
            listOf(card(Card.Rank.QUEEN, Card.Suit.HEARTS)),
            Play.PlayType.SINGLE
        )
        val stronger = Play(
            listOf(card(Card.Rank.KING, Card.Suit.SPADES)),
            Play.PlayType.SINGLE
        )
        val weakerPair = Play(
            listOf(card(Card.Rank.TEN, Card.Suit.CLUBS), card(Card.Rank.TEN, Card.Suit.DIAMONDS)),
            Play.PlayType.PAIR
        )
        assertTrue(stronger.canBePlayedOn(top))
        assertFalse(weakerPair.canBePlayedOn(top))
    }

    @Test
    fun `toString includes type and rank description`() {
        val cards = listOf(
            card(Card.Rank.JACK, Card.Suit.HEARTS),
            card(Card.Rank.JACK, Card.Suit.DIAMONDS),
            card(Card.Rank.JACK, Card.Suit.CLUBS)
        )
        val play = Play(cards, Play.PlayType.THREE_OF_A_KIND)
        assertEquals(
            "Un brelant de Valet: Valet de Coeur ♥, Valet de Carreau ♦, Valet de Trèfle ♣",
            play.toString()
        )
    }
}
