package model

import kotlin.times

object Utils {
    fun createDeck(): MutableList<Card> {
        val deck = mutableListOf<Card>()
        for (suit in Card.Suit.entries) {
            for (rank in Card.Rank.entries) {
                deck.add(Card(rank, suit))
            }
        }
        return deck
    }

    fun clearDeck(deck: MutableList<Card>) {
        deck.clear()
    }

    fun verifyDeck(deck: MutableList<Card>) {
        val expectedSize = Card.Suit.entries.size * Card.Rank.entries.size
        require(deck.size == expectedSize) { "Le paquet doit contenir $expectedSize cartes." }

        val uniqueCards = deck.toSet()
        require(uniqueCards.size == expectedSize) { "Le paquet contient des doublons." }

        val expectedCards = Card.Suit.entries
            .flatMap { suit -> Card.Rank.entries.map { rank -> Card(rank, suit) } }
            .toSet()
        require(uniqueCards == expectedCards) { "Le paquet ne contient pas toutes les cartes attendues." }

    }

    fun shuffleDeck(deck: MutableList<Card>) {
        deck.shuffle()
    }

    fun printDeck(deck: MutableList<Card>) {
        deck.forEach { printCard(it) }
    }

    fun printCard(card: Card) {
        println(card)
    }
}