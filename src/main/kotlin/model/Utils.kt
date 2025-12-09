package model

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

    fun shuffleDeck(deck: MutableList<Card>) {
        deck.shuffle()
    }

    fun dealCard(deck: MutableList<Card>): Card {
        return deck.removeAt(0)
    }

    fun printDeck(deck: MutableList<Card>) {
        deck.forEach { printCard(it) }
    }

    fun printCard(card: Card) {
        println(card)
    }
}