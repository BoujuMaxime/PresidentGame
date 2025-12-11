package model

import model.player.Player

/**
 * Représente une partie de Président.
 *
 * @property parameters Paramètres de configuration de la partie
 * @property players Liste des joueurs participant à la partie
 * @property deck Paquet de cartes utilisé pour la partie
 */
class Game(
    val parameters: GameParameters,
    val players: MutableList<Player> = mutableListOf(),
    val deck: MutableList<Card> = Utils.createDeck(),
) {

    /**
     * Paramètres de configuration d'une partie.
     *
     * @property nbPlayers Nombre de joueurs (défaut : 4)
     * @property gameMode Mode de jeu (LOCAL ou REMOTE)
     * @property aiDifficulty Niveau de difficulté des IA
     * @property consoleOutput Affichage des logs dans la console
     * @property gameModeParameters Paramètres spécifiques au mode de jeu
     */
    data class GameParameters(
        val nbPlayers: Int = 4,
        val gameMode: GameMode = GameMode.LOCAL,
        val aiDifficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
        val consoleOutput: Boolean = true,
        val gameModeParameters: GameModeParameters = GameModeParameters()
    ) {
        enum class DifficultyLevel { EASY, MEDIUM, HARD }
        enum class GameMode { LOCAL, REMOTE }

        /**
         * Paramètres spécifiques au mode de jeu.
         *
         * @property withStraight Règle "Carré Magique" activée
         * @property withForcePlay Règle "Ta Gueule" activée
         */
        data class GameModeParameters(
            val withStraight: Boolean = true,
            val withForcePlay: Boolean = true
        )
    }

    private var lastGameRanking: List<Player> = emptyList()

    /**
     * Démarre une partie complète.
     *
     * @throws IllegalArgumentException Si le nombre de joueurs ne correspond pas aux paramètres
     */
    fun startGame() {
        validatePlayerCount()
        Utils.setConsoleEnabled(parameters.consoleOutput)

        resetDeck()
        distributeCards()
        exchangeCards()
        playTurn()
        assignRoles()
    }

    /**
     * Valide que le nombre de joueurs correspond aux paramètres.
     */
    private fun validatePlayerCount() {
        require(players.size == parameters.nbPlayers) {
            "Le nombre de joueurs doit être ${parameters.nbPlayers}"
        }
    }

    /**
     * Réinitialise et mélange le paquet de cartes.
     */
    private fun resetDeck() {
        Utils.printGameLifecycle("Réinitialisation du paquet")
        Utils.clearDeck(deck)
        deck.addAll(Utils.createDeck())
        Utils.shuffleDeck(deck)
        Utils.verifyDeck(deck)
    }

    /**
     * Distribue les cartes équitablement entre tous les joueurs.
     */
    private fun distributeCards() {
        Utils.printGameLifecycle("Distribution des cartes")
        clearPlayerHands()
        distributeCardsToPlayers()
        printPlayerHands()
    }

    /**
     * Vide les mains de tous les joueurs.
     */
    private fun clearPlayerHands() {
        players.forEach { it.hand.clear() }
    }

    /**
     * Distribue les cartes du paquet aux joueurs en rotation.
     */
    private fun distributeCardsToPlayers() {
        var playerIndex = 0
        val iterator = deck.iterator()
        while (iterator.hasNext()) {
            val card = iterator.next()
            players[playerIndex].hand.add(card)
            iterator.remove()
            playerIndex = (playerIndex + 1) % players.size
        }
    }

    /**
     * Affiche les mains de tous les joueurs.
     */
    private fun printPlayerHands() {
        players.forEach { Utils.printHand(it.id, it.hand) }
    }

    /**
     * Effectue les échanges de cartes selon les rôles du tour précédent.
     */
    private fun exchangeCards() {
        Utils.printGameLifecycle("Échanges de cartes (si nécessaire)")

        if (!canExchangeCards()) {
            Utils.printGameLifecycle("Pas d'échanges")
            return
        }

        exchangePresidentAndAsshole()
        exchangeViceRoles()
    }

    /**
     * Vérifie si les échanges de cartes sont possibles.
     *
     * @return `true` si au moins 2 joueurs ont participé au tour précédent
     */
    private fun canExchangeCards(): Boolean = lastGameRanking.size >= 2

    /**
     * Échange deux cartes entre le Président et le Trou du Cul.
     */
    private fun exchangePresidentAndAsshole() {
        val president = lastGameRanking.first()
        val asshole = lastGameRanking.last()
        swapCards(asshole, president, 2)
    }

    /**
     * Échange une carte entre le Vice-Président et le Vice-Trou du Cul si possible.
     */
    private fun exchangeViceRoles() {
        val ordered = lastGameRanking
        val vicePresident = ordered.getOrNull(1)
        val viceAsshole = ordered.getOrNull(ordered.lastIndex - 1)
        val president = ordered.first()
        val asshole = ordered.last()

        if (canExchangeViceRoles(vicePresident, viceAsshole, president, asshole)) {
            swapCards(viceAsshole!!, vicePresident!!, 1)
        }
    }

    /**
     * Vérifie si l'échange entre Vice-Président et Vice-Trou du Cul est valide.
     *
     * @param vicePresident Le Vice-Président
     * @param viceAsshole Le Vice-Trou du Cul
     * @param president Le Président
     * @param asshole Le Trou du Cul
     * @return `true` si l'échange est possible
     */
    private fun canExchangeViceRoles(
        vicePresident: Player?,
        viceAsshole: Player?,
        president: Player,
        asshole: Player
    ): Boolean {
        return vicePresident != null &&
                viceAsshole != null &&
                vicePresident != president &&
                viceAsshole != asshole
    }

    /**
     * Échange un nombre déterminé de cartes entre deux joueurs.
     *
     * @param sender Joueur qui envoie ses meilleures cartes
     * @param receiver Joueur qui reçoit les meilleures cartes et envoie ses pires
     * @param count Nombre de cartes à échanger
     */
    private fun swapCards(sender: Player, receiver: Player, count: Int) {
        val highestFromSender = selectCards(sender, count, highest = true)
        val lowestFromReceiver = selectCards(receiver, count, highest = false)

        transferCards(sender, receiver, highestFromSender)
        transferCards(receiver, sender, lowestFromReceiver)

        printCardExchange(sender, receiver, highestFromSender, lowestFromReceiver)
    }

    /**
     * Sélectionne des cartes dans la main d'un joueur.
     *
     * @param player Joueur dont on sélectionne les cartes
     * @param count Nombre de cartes à sélectionner
     * @param highest Si `true`, sélectionne les cartes les plus fortes, sinon les plus faibles
     * @return Liste des cartes sélectionnées
     */
    private fun selectCards(player: Player, count: Int, highest: Boolean): List<Card> {
        if (player.hand.isEmpty()) return emptyList()
        val sorted = player.hand.sortedBy { it.rank.ordinal }
        return if (highest) sorted.takeLast(count) else sorted.take(count)
    }

    /**
     * Transfère des cartes d'un joueur à un autre.
     *
     * @param from Joueur qui envoie les cartes
     * @param to Joueur qui reçoit les cartes
     * @param cards Cartes à transférer
     */
    private fun transferCards(from: Player, to: Player, cards: List<Card>) {
        cards.forEach { card ->
            if (from.hand.remove(card)) {
                to.hand.add(card)
            }
        }
    }

    /**
     * Affiche les détails de l'échange de cartes.
     *
     * @param sender Joueur qui envoie
     * @param receiver Joueur qui reçoit
     * @param sentCards Cartes envoyées
     * @param receivedCards Cartes reçues en retour
     */
    private fun printCardExchange(
        sender: Player,
        receiver: Player,
        sentCards: List<Card>,
        receivedCards: List<Card>
    ) {
        Utils.printAction(
            sender.id,
            "donne ${sentCards.size} cartes à ${receiver.id}: ${sentCards.joinToString()}"
        )
        Utils.printAction(
            receiver.id,
            "donne ${receivedCards.size} cartes à ${sender.id}: ${receivedCards.joinToString()}"
        )
    }

    /**
     * Lance une manche complète et met à jour le classement.
     */
    private fun playTurn() {
        val gameTurns = GameTurns(parameters, players)
        val startingPlayer = lastGameRanking.lastOrNull() ?: players.first()
        lastGameRanking = gameTurns.startTurn(startingPlayer)
    }

    /**
     * Assigne les rôles aux joueurs selon leur classement.
     */
    private fun assignRoles() {
        val ordered = lastGameRanking.ifEmpty { players }
        assignRolesByRanking(ordered)
        Utils.printRolesSummary(ordered)
    }

    /**
     * Assigne un rôle à chaque joueur selon sa position dans le classement.
     *
     * @param orderedPlayers Joueurs classés par ordre de performance
     */
    private fun assignRolesByRanking(orderedPlayers: List<Player>) {
        orderedPlayers.forEachIndexed { index, player ->
            player.role = when (index) {
                0 -> Player.Role.PRESIDENT
                1 -> Player.Role.VICE_PRESIDENT
                orderedPlayers.lastIndex - 1 -> Player.Role.VICE_ASSHOLE
                orderedPlayers.lastIndex -> Player.Role.ASSHOLE
                else -> Player.Role.NEUTRAL
            }
        }
    }
}