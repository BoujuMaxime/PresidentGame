package model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PlayTest {
    private fun card(rank: Card.Rank, suit: Card.Suit) = Card(rank, suit)

    @Test
    fun `valid single play is allowed`() {
        val playerMove = PlayerMove(listOf(card(Card.Rank.THREE, Card.Suit.CLUBS)), PlayerMove.PlayType.SINGLE)
        assertEquals(PlayerMove.PlayType.SINGLE, playerMove.playType)
        assertEquals(Card.Rank.THREE, playerMove.getRank())
    }

    @Test
    fun `pair constructor rejects mixed ranks`() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            PlayerMove(
                listOf(card(Card.Rank.FOUR, Card.Suit.HEARTS), card(Card.Rank.FIVE, Card.Suit.HEARTS)),
                PlayerMove.PlayType.PAIR
            )
        }
        assertEquals("Combinaison de cartes invalide pour le type ${PlayerMove.PlayType.PAIR}", ex.message)
    }

    @Test
    fun `canBePlayedOn requires same type and higherOrEqual rank`() {
        val top = PlayerMove(
            listOf(card(Card.Rank.QUEEN, Card.Suit.HEARTS)),
            PlayerMove.PlayType.SINGLE
        )
        val stronger = PlayerMove(
            listOf(card(Card.Rank.KING, Card.Suit.SPADES)),
            PlayerMove.PlayType.SINGLE
        )
        val weakerPair = PlayerMove(
            listOf(card(Card.Rank.TEN, Card.Suit.CLUBS), card(Card.Rank.TEN, Card.Suit.DIAMONDS)),
            PlayerMove.PlayType.PAIR
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
        val playerMove = PlayerMove(cards, PlayerMove.PlayType.THREE_OF_A_KIND)
        assertEquals(
            "Un brelant de Valet: Valet de Coeur ♥, Valet de Carreau ♦, Valet de Trèfle ♣",
            playerMove.toString()
        )
    }
}
