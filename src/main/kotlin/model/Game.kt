package model

import model.player.Player

/**
 * Classe représentant le déroulement d'une partie de Président.
 *
 * @property parameters Les paramètres de la partie, définis par la classe `GameParameters`.
 * @property players La liste des joueurs participant à la partie.
 * @property deck Le paquet de cartes utilisé pour la partie.
 */
class Game(
    val parameters: GameParameters,
    val players: MutableList<Player> = mutableListOf(),
    val deck: MutableList<Card> = Utils.createDeck(),
) {

    /**
     * Classe imbriquée contenant les paramètres de configuration de la partie.
     *
     * @property nbPlayers Le nombre de joueurs dans la partie (par défaut 4).
     * @property gameMode Le mode de jeu (LOCAL ou REMOTE).
     * @property aiDifficulty Le niveau de difficulté des IA (EASY, MEDIUM, HARD).
     * @property gameModeParameters Les paramètres spécifiques au mode de jeu.
     */
    data class GameParameters(
        val nbPlayers: Int = 4,
        val gameMode: GameMode = GameMode.LOCAL,
        val aiDifficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
        val consoleOutput: Boolean = true,
        val gameModeParameters: GameModeParameters = GameModeParameters()
    ) {
        /**
         * Enumération représentant les niveaux de difficulté des IA.
         */
        enum class DifficultyLevel { EASY, MEDIUM, HARD }

        /**
         * Enumération représentant les modes de jeu possibles.
         */
        enum class GameMode { LOCAL, REMOTE }

        /**
         * Classe imbriquée contenant les paramètres spécifiques au mode de jeu.
         *
         * @property withStraight Active ou désactive la règle `Carré Magique`.
         * @property withForcePlay Active ou désactive la règle `Ta Gueule`.
         */
        data class GameModeParameters(
            val withStraight: Boolean = true,
            val withForcePlay: Boolean = true
        )
    }

    /**
     * Liste des joueurs classés selon leur performance au dernier tour.
     */
    private var lastGameRanking: List<Player> = emptyList()

    /**
     * Démarre la partie en initialisant le jeu, distribuant les cartes,
     * effectuant les échanges, jouant un tour, et assignant les rôles.
     *
     * @throws IllegalArgumentException si le nombre de joueurs est incorrect.
     */
    fun startGame() {
        Utils.setConsoleEnabled(parameters.consoleOutput)

        /**
         * Réinitialise le paquet de cartes en le recréant.
         */
        fun resetDeck() {
            Utils.printGameLifecycle("Réinitialisation du paquet")
            Utils.clearDeck(deck)
            deck.addAll(Utils.createDeck())
            Utils.shuffleDeck(deck)
            Utils.verifyDeck(deck)
            Utils.printDeck(deck)
        }

        /**
         * Distribue les cartes du paquet aux joueurs de manière équitable.
         */
        fun distributeCards() {
            Utils.printGameLifecycle("Distribution des cartes")
            players.forEach { it.hand.clear() }
            var playerIndex = 0
            val iterator = deck.iterator()
            while (iterator.hasNext()) {
                val card = iterator.next()
                players[playerIndex].hand.add(card)
                iterator.remove()
                playerIndex = (playerIndex + 1) % players.size
            }
            // Affiche les mains si activé
            players.forEach { Utils.printHand(it.id, it.hand) }
        }

        /**
         * Effectue les échanges de cartes entre les joueurs en fonction du classement
         * du dernier tour.
         */
        fun exchangeCards() {
            Utils.printGameLifecycle("Échanges de cartes (si nécessaire)")
            /**
             * Échange un nombre donné de cartes entre deux joueurs.
             *
             * @param sender Le joueur envoyant les cartes.
             * @param receiver Le joueur recevant les cartes.
             * @param count Le nombre de cartes à échanger.
             */
            fun swapCards(sender: Player, receiver: Player, count: Int) {
                /**
                 * Sélectionne un nombre donné de cartes dans la main d'un joueur.
                 *
                 * @param player Le joueur dont les cartes sont sélectionnées.
                 * @param count Le nombre de cartes à sélectionner.
                 * @param highest Si `true`, sélectionne les cartes les plus fortes, sinon les plus faibles.
                 * @return Une liste des cartes sélectionnées.
                 */
                fun selectCards(player: Player, count: Int, highest: Boolean): List<Card> {
                    if (player.hand.isEmpty()) return emptyList()
                    val sorted = player.hand.sortedBy { it.rank.ordinal }
                    return if (highest) sorted.takeLast(count) else sorted.take(count)
                }

                /**
                 * Transfère des cartes d'un joueur à un autre.
                 *
                 * @param from Le joueur envoyant les cartes.
                 * @param to Le joueur recevant les cartes.
                 * @param cards La liste des cartes à transférer.
                 */
                fun transferCards(from: Player, to: Player, cards: List<Card>) {
                    cards.forEach { card ->
                        if (from.hand.remove(card)) {
                            to.hand.add(card)
                        }
                    }
                }

                val highestFromSender = selectCards(sender, count, highest = true)
                val lowestFromReceiver = selectCards(receiver, count, highest = false)
                transferCards(sender, receiver, highestFromSender)
                transferCards(receiver, sender, lowestFromReceiver)

                Utils.printAction(
                    sender.id,
                    "donne ${highestFromSender.size} cartes à ${receiver.id}: ${highestFromSender.joinToString()}"
                )
                Utils.printAction(
                    receiver.id,
                    "donne ${lowestFromReceiver.size} cartes à ${sender.id}: ${lowestFromReceiver.joinToString()}"
                )
            }

            if (lastGameRanking.size < 2) {
                Utils.printGameLifecycle("Pas d'échanges")
                return
            }

            val ordered = lastGameRanking
            val president = ordered.first()
            val asshole = ordered.last()
            swapCards(president, asshole, 2)

            val vicePresident = ordered.getOrNull(1)
            val viceAsshole = ordered.getOrNull(ordered.lastIndex - 1)
            if (vicePresident != null && viceAsshole != null && vicePresident != president && viceAsshole != asshole) {
                swapCards(vicePresident, viceAsshole, 1)
            }
        }

        /**
         * Joue une manche complète, en permettant à chaque joueur de jouer ses cartes
         * tant qu'il reste plus d'un joueur avec des cartes.
         */
        fun playRound() {
            val roundManager = RoundManager(parameters, players)
            lastGameRanking = roundManager.startRound(lastGameRanking.firstOrNull() ?: players.first())
        }

        /**
         * Assigne les rôles aux joueurs en fonction de leur classement final.
         */
        fun assignRoles() {
            val ordered = lastGameRanking.ifEmpty { players }
            ordered.forEachIndexed { index, player ->
                player.role = when (index) {
                    0 -> Player.Role.PRESIDENT
                    1 -> Player.Role.VICE_PRESIDENT
                    ordered.lastIndex - 1 -> Player.Role.VICE_ASSHOLE
                    ordered.lastIndex -> Player.Role.ASSHOLE
                    else -> Player.Role.NEUTRAL
                }
            }
            Utils.printRolesSummary(ordered)
        }

        // Vérifie que le nombre de joueurs correspond au paramètre attendu
        require(players.size == parameters.nbPlayers) {
            "Le nombre de joueurs doit être ${parameters.nbPlayers}"
        }

        resetDeck()
        distributeCards()
        exchangeCards()
        playRound()
        assignRoles()
    }
}