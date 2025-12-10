package model

import model.player.Player

object Utils {

    /**
     * Active/désactive l'affichage détaillé dans la console.
     * À régler avant le début d'une partie : Utils.setConsoleEnabled(...)
     */
    var consoleOutputEnabled: Boolean = true

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

    fun setConsoleEnabled(enabled: Boolean) {
        consoleOutputEnabled = enabled
    }

    fun info(message: String) {
        if (consoleOutputEnabled) println(message)
    }

    fun debug(message: String) {
        if (consoleOutputEnabled) System.err.println(message)
    }

    fun printGameLifecycle(message: String) {
        if (consoleOutputEnabled) println("[GAME] $message")
    }

    fun printAction(playerId: String, action: String) {
        if (consoleOutputEnabled) println("[ACTION] $playerId: $action")
    }

    fun printPlay(playerId: String, play: Play?) {
        if (consoleOutputEnabled) {
            val content = play?.toString() ?: "passe"
            println("[PLAY] $playerId => $content")
        }
    }

    fun printDeck(deck: MutableList<Card>) {
        if (!consoleOutputEnabled) return
        println("Paquet (${deck.size}) :")
        deck.forEach { printCard(it) }
    }

    fun printCard(card: Card) {
        if (!consoleOutputEnabled) return
        println(card)
    }

    fun printHand(playerId: String, hand: List<Card>) {
        if (!consoleOutputEnabled) return
        println("Main de $playerId (${hand.size}) : ${hand.joinToString()}")
    }

    fun printRolesSummary(players: List<Player>) {
        if (!consoleOutputEnabled) return
        println("Attribution des rôles :")
        players.forEach { p ->
            println("- ${p.id} -> ${p.role}")
        }
    }
}