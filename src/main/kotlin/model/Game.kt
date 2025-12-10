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
    private var lastGameRanking: List<Player> = emptyList()

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

            if (lastGameRanking.size < 2) {
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
            val ranking = mutableListOf<Player>()
            val pile = mutableListOf<Card>()
            val discardPile = mutableListOf<Card>()
            var firstPlayer = lastGameRanking.firstOrNull() ?: players.first()
            var round = 0

            /**
             * Gère le déroulement d'un pli (tour de jeu où chaque joueur peut jouer ou passer).
             *
             * C'est le Trou-du-Cul qui commence le premier pli.
             *
             * Chaque joueur joue l'un après l'autre, on peut jouer ou passer.
             *
             * Si un joueur passe, il ne peut plus jouer.
             *
             * Si un joueur joue, il doit jouer au moins une carte supérieure ou équivalente en rangs.
             *
             * Si la règle `TaGueule` est active :
             * - Dès que deux joueurs jouent consécutivement deux cartes de meme rangs le troisième doit joue une carte de ce rang
             * - S'il ne peut pas, il passe son tour (il pourra rejouer au prochain tour).
             * - S'il peut jouer, alors c'est au quatrième de jouer une carte de ce rang.
             * - S'il ne peut pas, il passe son tour (il pourra rejouer au prochain tour).
             *
             * Le premier joueur peut decidé de jouer une carte simple, une paire, un brelan ou un carré.
             *
             * Les joueurs suivants doivent jouer le même nombre de cartes de rang supérieur ou équivalent ou passer.
             *
             * Si un joueur s'est défaussé de toutes ses cartes, il est ajouté au classement.
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
                fun activePlayers() = players.filter { it.hand.isNotEmpty() }
                if (activePlayers().size <= 1) return

                val passes = mutableSetOf<Player>()
                var lastPlay: Play? = null
                var lastPlayer: Player? = null
                val maxRank = Card.Rank.entries.maxByOrNull { it.ordinal }
                val carreEnabled = parameters.gameModeParameters.withCarreMagique
                val forceEnabled = parameters.gameModeParameters.withTaGueule

                val starter = firstPlayer
                val starterIndex = players.indexOf(starter).takeIf { it >= 0 } ?: 0
                var turnOffset = 0

                var anyPlayHappened = false

                while (true) {
                    val current = players[(starterIndex + turnOffset) % players.size]
                    turnOffset++

                    if (current.hand.isEmpty()) {
                        continue
                    }

                    if (lastPlay != null && lastPlayer != null) {
                        val others = players.filter { it.hand.isNotEmpty() && it != lastPlayer }
                        if (others.all { it in passes }) {
                            discardPile.addAll(pile)
                            pile.clear()
                            firstPlayer = lastPlayer
                            return
                        }
                    }

                    val active = activePlayers()
                    if (!anyPlayHappened && passes.containsAll(active)) {
                        val nextStarter = players.subList((starterIndex + 1) % players.size, players.size) +
                                players.subList(0, (starterIndex + 1) % players.size)
                        firstPlayer = nextStarter.firstOrNull { it.hand.isNotEmpty() } ?: starter
                        return
                    }

                    val play = try {
                        current.playTurn(pile, discardPile, lastPlay)
                    } catch (e: Exception) {
                        null
                    }

                    if (play == null) {
                        passes.add(current)
                    } else {
                        if (lastPlay != null && play.playType != lastPlay.playType) {
                            passes.add(current)
                            continue
                        }
                        if (!play.canBePlayedOn(lastPlay)) {
                            passes.add(current)
                            continue
                        }

                        play.forEach { card ->
                            if (current.hand.remove(card)) {
                                pile.add(card)
                            }
                        }

                        anyPlayHappened = true
                        passes.clear()
                        lastPlay = play
                        lastPlayer = current

                        if (maxRank != null && play.any { it.rank == maxRank }) {
                            discardPile.addAll(pile)
                            pile.clear()
                            firstPlayer = current
                            return
                        }

                        if (carreEnabled && pile.size >= 4) {
                            val lastFour = pile.takeLast(4)
                            val allSameRank = lastFour.map { it.rank }.distinct().size == 1
                            if (allSameRank) {
                                discardPile.addAll(pile)
                                pile.clear()
                                firstPlayer = current
                                return
                            }
                        }

                        if (current == starter && current.hand.isEmpty()) {
                            val nextWinner = players.dropWhile { it != current }
                                .drop(1)
                                .plus(players.takeWhile { it != current })
                                .firstOrNull { it.hand.isNotEmpty() } ?: current
                            discardPile.addAll(pile)
                            pile.clear()
                            firstPlayer = nextWinner
                            return
                        }
                    }
                }
            }

            while (players.count { it.hand.isNotEmpty() } > 1) {
                playPile()
                players.filter { it.hand.isEmpty() && it !in ranking }.forEach {
                    ranking.add(it)
                }
                round++
            }

            ranking.addAll(players.filter { it !in ranking })
            lastGameRanking = ranking
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