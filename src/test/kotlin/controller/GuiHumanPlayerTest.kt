package controller

import model.Card
import model.Utils
import model.player.PlayerUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GuiHumanPlayerTest {

    @BeforeEach
    fun disableConsoleOutput() {
        Utils.setConsoleEnabled(false)
    }

    private fun card(rank: Card.Rank, suit: Card.Suit) = Card(rank, suit)

    @Test
    fun `hand can be sorted using PlayerUtils sortHandByRank`() {
        // Créer une main non triée
        val hand = mutableListOf(
            card(Card.Rank.KING, Card.Suit.SPADES),
            card(Card.Rank.THREE, Card.Suit.DIAMONDS),
            card(Card.Rank.ACE, Card.Suit.HEARTS),
            card(Card.Rank.FIVE, Card.Suit.CLUBS)
        )

        // Trier la main en utilisant PlayerUtils (la méthode utilisée par GuiHumanPlayer.sortHand)
        PlayerUtils.sortHandByRank(hand)

        // Vérifier que la main est triée par rang
        assertEquals(
            listOf(Card.Rank.THREE, Card.Rank.FIVE, Card.Rank.KING, Card.Rank.ACE),
            hand.map { it.rank }
        )
    }

    @Test
    fun `sorting works with empty hand`() {
        val hand = mutableListOf<Card>()
        PlayerUtils.sortHandByRank(hand)
        assertTrue(hand.isEmpty())
    }

    @Test
    fun `sorting works with single card`() {
        val hand = mutableListOf(card(Card.Rank.KING, Card.Suit.SPADES))
        PlayerUtils.sortHandByRank(hand)
        assertEquals(1, hand.size)
        assertEquals(Card.Rank.KING, hand[0].rank)
    }

    @Test
    fun `sorting maintains suit information`() {
        val hand = mutableListOf(
            card(Card.Rank.FIVE, Card.Suit.HEARTS),
            card(Card.Rank.FIVE, Card.Suit.DIAMONDS),
            card(Card.Rank.THREE, Card.Suit.SPADES)
        )

        PlayerUtils.sortHandByRank(hand)

        // Vérifier que toutes les cartes sont présentes avec leurs couleurs
        assertEquals(3, hand.size)
        assertEquals(Card.Rank.THREE, hand[0].rank)
        assertEquals(Card.Suit.SPADES, hand[0].suit)
        assertEquals(Card.Rank.FIVE, hand[1].rank)
        assertEquals(Card.Rank.FIVE, hand[2].rank)
        // Les deux cinq doivent être là (ordre des couleurs peut varier)
        assertTrue(hand.any { it.rank == Card.Rank.FIVE && it.suit == Card.Suit.HEARTS })
        assertTrue(hand.any { it.rank == Card.Rank.FIVE && it.suit == Card.Suit.DIAMONDS })
    }
}
