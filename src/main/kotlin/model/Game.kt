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
             * Joue un pli complet (séquence de tours où chaque joueur peut jouer ou passer).
             *
             * Comportement principal
             * - Le pli démarre avec le joueur désigné (le « Trou-du-Cul » pour le premier pli,
             *   puis le gagnant du pli précédent pour les suivants).
             * - À son tour, un joueur peut jouer un coup valide ou passer. Un joueur qui passe
             *   ne peut plus jouer sur ce pli jusqu'à ce que la pile soit résolue.
             * - Le premier coup du pli détermine le type (simple, paire, brelan, carré) et le
             *   nombre de cartes à jouer; les suivants doivent jouer le même nombre de cartes
             *   d'un rang supérieur ou équivalent, ou passer.
             *
             * Règles spéciales
             * - `TaGueule` (si activée) : lorsqu'il y a deux poses consécutives de deux cartes
             *   du même rang, le joueur suivant est contraint d'ajouter une carte de ce rang
             *   s'il le peut; sinon il passe (et pourra rejouer lors du pli suivant). La contrainte
             *   peut se propager selon la règle décrite dans l'implémentation.
             * - `Carré magique` (si activée) : poser la quatrième carte d'une même valeur
             *   remporte immédiatement le pli.
             * - Carte « 2 » (rang maximum) : poser un 2 remporte immédiatement le pli.
             * - Si le premier joueur vide sa main en posant, le gagnant du pli devient le joueur
             *   suivant (on ne compte pas le président pour remporter ce pli).
             *
             * Effets de bord
             * - Modifie les mains des joueurs (retrait/ajout de cartes).
             * - Met à jour `firstPlayer` pour indiquer qui commencera le pli suivant.
             * - Déplace les cartes de la pile vers la défausse lorsque le pli est conclu.
             *
             * Conditions de terminaison
             * - Le pli se poursuit tant qu'il y a au moins deux joueurs avec des cartes.
             * - Le pli se termine immédiatement si une condition de victoire instantanée
             *   (pose d'un 2, carré magique) est remplie, ou quand tous les autres joueurs
             *   ont passé après une pose valide.
             *
             * Robustesse
             * - Les exceptions levées par `Player.playTurn` sont interceptées et considérées
             *   comme un passage pour le joueur concerné.
             *
             * Remarque
             * - Cette fonction n'effectue pas la distribution ni l'attribution des rôles; elle
             *   met à jour uniquement l'état lié au déroulement des plis.
             */
            fun playPile() {
                // Fonction interne pour récupérer les joueurs encore actifs (ayant des cartes en main)
                fun activePlayers() = players.filter { it.hand.isNotEmpty() }
                if (activePlayers().size <= 1) return // Arrête si un seul joueur ou moins a encore des cartes

                val pile = mutableListOf<Card>() // Pile de cartes jouées
                val discardPile = mutableListOf<Card>() // Pile de défausse
                val maxRank = Card.Rank.entries.maxByOrNull { it.ordinal } // Rang maximum des cartes
                val carreEnabled =
                    parameters.gameModeParameters.withCarreMagique // Règle "Carré magique" activée ou non
                val starter = firstPlayer // Premier joueur du pli
                val starterIndex = players.indexOf(starter).takeIf { it >= 0 } ?: 0 // Index du premier joueur

                /**
                 * Fonction récursive pour gérer les tours de jeu.
                 *
                 * @param turnOffset Décalage pour déterminer le joueur actuel.
                 * @param passes Ensemble des joueurs ayant passé leur tour.
                 * @param lastPlay Dernier coup joué.
                 * @param lastPlayer Dernier joueur ayant joué.
                 * @param anyPlayHappened Indique si un coup valide a été joué dans ce pli.
                 * @return Le joueur qui remporte le pli, ou `null` si aucun.
                 */
                tailrec fun recurse(
                    turnOffset: Int,
                    passes: Set<Player>,
                    lastPlay: Play?,
                    lastPlayer: Player?,
                    anyPlayHappened: Boolean
                ): Player? {
                    val current = players[(starterIndex + turnOffset) % players.size] // Joueur actuel
                    val mutablePasses = passes.toMutableSet() // Copie mutable des joueurs ayant passé
                    var lp = lastPlay // Dernier coup joué
                    var lplayer = lastPlayer // Dernier joueur ayant joué
                    var anyPlay = anyPlayHappened // Indique si un coup valide a été joué

                    // Si le joueur actuel n'a plus de cartes, passer au joueur suivant
                    if (current.hand.isEmpty()) {
                        return recurse(turnOffset + 1, mutablePasses, lp, lplayer, anyPlay)
                    }

                    // Vérifie si tous les autres joueurs ont passé après le dernier joueur ayant joué
                    if (lp != null && lplayer != null) {
                        val others = players.filter { it.hand.isNotEmpty() && it != lplayer }
                        if (others.all { it in mutablePasses }) {
                            discardPile.addAll(pile) // Défausse les cartes de la pile
                            pile.clear() // Vide la pile
                            return lplayer // Le dernier joueur ayant joué remporte le pli
                        }
                    }

                    // Si aucun coup n'a été joué et que tous les joueurs actifs ont passé
                    val active = activePlayers()
                    if (!anyPlay && mutablePasses.containsAll(active)) {
                        val nextStartIndex = (starterIndex + 1) % players.size
                        val nextStarter = players.subList(nextStartIndex, players.size) +
                                players.subList(0, nextStartIndex)
                        return nextStarter.firstOrNull { it.hand.isNotEmpty() } ?: starter
                    }

                    // Le joueur actuel tente de jouer
                    val play = try {
                        current.playTurn(pile, discardPile, lp)
                    } catch (e: Exception) {
                        null // En cas d'erreur, le joueur passe son tour
                    }

                    if (play == null) {
                        mutablePasses.add(current) // Ajoute le joueur à ceux ayant passé
                        return recurse(turnOffset + 1, mutablePasses, lp, lplayer, anyPlay)
                    } else {
                        // Vérifie si le coup est valide
                        if (lp != null && play.playType != lp.playType) {
                            mutablePasses.add(current)
                            return recurse(turnOffset + 1, mutablePasses, lp, lplayer, anyPlay)
                        }
                        if (!play.canBePlayedOn(lp)) {
                            mutablePasses.add(current)
                            return recurse(turnOffset + 1, mutablePasses, lp, lplayer, anyPlay)
                        }

                        // Ajoute les cartes jouées à la pile et les retire de la main du joueur
                        play.forEach { card ->
                            if (current.hand.remove(card)) {
                                pile.add(card)
                            }
                        }

                        anyPlay = true // Indique qu'un coup valide a été joué
                        mutablePasses.clear() // Réinitialise les joueurs ayant passé
                        lp = play // Met à jour le dernier coup joué
                        lplayer = current // Met à jour le dernier joueur ayant joué

                        // Vérifie si le pli est remporté par un "2" ou un "Carré magique"
                        if (maxRank != null && play.any { it.rank == maxRank }) {
                            discardPile.addAll(pile)
                            pile.clear()
                            return current
                        }

                        if (carreEnabled && pile.size >= 4) {
                            val lastFour = pile.takeLast(4)
                            val allSameRank = lastFour.map { it.rank }.distinct().size == 1
                            if (allSameRank) {
                                discardPile.addAll(pile)
                                pile.clear()
                                return current
                            }
                        }

                        // Si le premier joueur vide sa main, le joueur suivant remporte le pli
                        if (current == starter && current.hand.isEmpty()) {
                            val nextWinner = players.dropWhile { it != current }
                                .drop(1)
                                .plus(players.takeWhile { it != current })
                                .firstOrNull { it.hand.isNotEmpty() } ?: current
                            discardPile.addAll(pile)
                            pile.clear()
                            return nextWinner
                        }

                        // Passe au joueur suivant
                        return recurse(turnOffset + 1, mutablePasses, lp, lplayer, anyPlay)
                    }
                }

                // Lance la récursion pour jouer le pli
                val winner = recurse(0, emptySet(), null, null, false)
                if (winner != null) firstPlayer = winner // Met à jour le premier joueur pour le prochain pli
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