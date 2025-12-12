package model

import model.player.Player

/**
 * Utilitaire pour gérer les opérations liées aux cartes, à la console et aux joueurs.
 */
object Utils {

    /**
     * Indique si l'affichage détaillé dans la console est activé.
     * À régler avant le début d'une partie via `Utils.setConsoleEnabled(...)`.
     */
    var consoleOutputEnabled: Boolean = true

    /**
     * Crée un paquet de cartes complet avec toutes les combinaisons de rangs et couleurs.
     * @return Une liste mutable contenant toutes les cartes.
     */
    fun createDeck(): MutableList<Card> {
        val deck = mutableListOf<Card>()
        for (suit in Card.Suit.entries) {
            for (rank in Card.Rank.entries) {
                deck.add(Card(rank, suit))
            }
        }
        return deck
    }

    /**
     * Vide le contenu d'un paquet de cartes.
     * @param deck Le paquet de cartes à vider.
     */
    fun clearDeck(deck: MutableList<Card>) {
        deck.clear()
    }

    /**
     * Vérifie qu'un paquet de cartes est valide.
     * @param deck Le paquet de cartes à vérifier.
     * @throws IllegalArgumentException si le paquet est invalide (taille incorrecte, doublons, cartes manquantes).
     */
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

    /**
     * Mélange un paquet de cartes.
     * @param deck Le paquet de cartes à mélanger.
     */
    fun shuffleDeck(deck: MutableList<Card>) {
        deck.shuffle()
    }

    /**
     * Active ou désactive l'affichage dans la console.
     * @param enabled `true` pour activer, `false` pour désactiver.
     */
    fun setConsoleEnabled(enabled: Boolean) {
        consoleOutputEnabled = enabled
    }

    /**
     * Affiche un message d'information dans la console si l'affichage est activé.
     * @param message Le message à afficher.
     */
    fun info(message: String) {
        if (consoleOutputEnabled) println(message)
    }

    /**
     * Affiche un message de débogage dans la console d'erreur si l'affichage est activé.
     * @param message Le message à afficher.
     */
    fun debug(message: String) {
        if (consoleOutputEnabled) System.err.println(message)
    }

    /**
     * Affiche un message lié au cycle de vie du jeu dans la console si l'affichage est activé.
     * @param message Le message à afficher.
     */
    fun printGameLifecycle(message: String) {
        if (consoleOutputEnabled) println("[GAME] $message")
    }

    /**
     * Affiche une action effectuée par un joueur dans la console si l'affichage est activé.
     * @param playerId L'identifiant du joueur.
     * @param action L'action effectuée.
     */
    fun printAction(playerId: String, action: String) {
        if (consoleOutputEnabled) println("[ACTION] $playerId: $action")
    }

    /**
     * Affiche un coup joué par un joueur dans la console si l'affichage est activé.
     * @param playerId L'identifiant du joueur.
     * @param playerMove Le coup joué (ou `null` si le joueur passe).
     */
    fun printPlay(playerId: String, playerMove: PlayerMove?) {
        if (consoleOutputEnabled) {
            val content = playerMove?.toString() ?: "passe"
            println("[PLAY] $playerId => $content")
        }
    }

    /**
     * Affiche le contenu d'un paquet de cartes dans la console si l'affichage est activé.
     * @param deck Le paquet de cartes à afficher.
     */
    fun printDeck(deck: MutableList<Card>) {
        if (!consoleOutputEnabled) return
        println("Paquet (${deck.size}) :")
        deck.forEach { printCard(it) }
    }

    /**
     * Affiche une carte dans la console si l'affichage est activé.
     * @param card La carte à afficher.
     */
    fun printCard(card: Card) {
        if (!consoleOutputEnabled) return
        println(card)
    }

    /**
     * Affiche la main d'un joueur dans la console si l'affichage est activé.
     * @param playerId L'identifiant du joueur.
     * @param hand La liste des cartes dans la main du joueur.
     */
    fun printHand(playerId: String, hand: List<Card>) {
        if (!consoleOutputEnabled) return
        println("Main de $playerId (${hand.size}) : ${hand.joinToString()}")
    }

    /**
     * Affiche un résumé des rôles attribués aux joueurs dans la console si l'affichage est activé.
     * @param players La liste des joueurs avec leurs rôles.
     */
    fun printRolesSummary(players: List<Player>) {
        if (!consoleOutputEnabled) return
        println("Attribution des rôles :")
        players.forEach { p ->
            println("- ${p.id} -> ${p.role}")
        }
    }
}