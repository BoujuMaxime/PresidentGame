package model

import model.player.Player
import kotlin.compareTo

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
    val deck: MutableList<Card> = Utils.createDeck()
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
         * @property withCarreMagique Active ou désactive la règle `Carré Magique`.
         * @property withTaGueule Active ou désactive la règle `Ta Gueule`.
         */
        data class GameModeParameters(
            val withCarreMagique: Boolean = true,
            val withTaGueule: Boolean = true
        )
    }

    /**
     * Liste des joueurs classés selon leur performance au dernier tour.
     */
    private var lastRoundRanking: List<Player> = emptyList()

    /**
     * Démarre la partie en initialisant le jeu, distribuant les cartes,
     * effectuant les échanges, jouant un tour, et assignant les rôles.
     *
     * @throws IllegalArgumentException si le nombre de joueurs est incorrect.
     */
    fun startGame() {
        /**
         * Réinitialise le paquet de cartes en le recréant.
         */
        fun resetDeck() {
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
            players.forEach { it.hand.clear() }
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
         * Effectue les échanges de cartes entre les joueurs en fonction du classement
         * du dernier tour.
         */
        fun exchangeCards() {
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
            }

            if (lastRoundRanking.size < 2) return
            val ordered = lastRoundRanking
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
         * jusqu'à ce que tous les joueurs aient vidé leur main.
         */
        fun playRound() {
            val ranking = mutableListOf<Player>()
            val pile = mutableListOf<Card>()
            val discardPile = mutableListOf<Card>()
            var currentPlayerIndex = 0
            var round = 0

            /**
             * Gère le déroulement d'un pli (tour de jeu où chaque joueur peut jouer ou passer).
             *
             * Chaque joueur joue l'un après l'autre, on peut jouer ou passer.
             *
             * Si un joueur passe, il ne peut plus jouer.
             *
             * Si un joueur joue, il doit jouer au moins une carte supérieure ou équivalente en rangs.
             *
             * Le premier joueur peut decidé de jouer une carte simple, une paire, un brelan ou un carré.
             *
             * Les joueurs suivants doivent jouer le même nombre de cartes de rang supérieur ou équivalent ou passer.
             *
             * Règles de victoire d'un pli :
             * - Un joueur joue un "2" : il remporte immédiatement le pli.
             * - Un joueur pose la quatrième carte d'une même valeur ("Carré magique", si la règle est activée) : il remporte le pli.
             * - Tous les autres joueurs passent après une pose : le dernier joueur à avoir joué remporte le pli.
             * - Si le premier joueur vide sa main pendant le pli, le joueur suivant remporte le pli (on ne joue pas sur le président).
             *
             * Le vainqueur du pli commence le pli suivant.
             */
            fun playPile() {
                TODO("Not yet implemented")
            }

            // Boucle principale : on joue des plis tant qu'il reste plus d'un joueur avec des cartes
            while (players.count { it.hand.isNotEmpty() } > 1) {
                playPile()
                players.filter { it.hand.isEmpty() && it !in ranking }.forEach { ranking.add(it) }
            }

            // Ajoute le dernier joueur restant
            ranking.addAll(players.filter { it !in ranking })
            lastRoundRanking = ranking
        }

        /**
         * Assigne les rôles aux joueurs en fonction de leur classement final.
         */
        fun assignRoles() {
            val ordered = lastRoundRanking.ifEmpty { players }
            ordered.forEachIndexed { index, player ->
                player.role = when (index) {
                    0 -> Player.Role.PRESIDENT
                    1 -> Player.Role.VICE_PRESIDENT
                    ordered.lastIndex - 1 -> Player.Role.VICE_ASSHOLE
                    ordered.lastIndex -> Player.Role.ASSHOLE
                    else -> Player.Role.NEUTRAL
                }
            }
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