package model

import model.player.Player
import model.player.PlayerUtils

/**
 * Classe gérant le déroulement des manches dans une partie de Président.
 *
 * @property parameters Les paramètres de la partie, définis par la classe `GameParameters`.
 * @property players La liste des joueurs participant à la manche.
 */
class RoundManager(
    private val parameters: Game.GameParameters,
    private val players: MutableList<Player>
) {

    /**
     * Démarre une manche complète et renvoie le classement final (dans l'ordre d'arrivée).
     *
     * @param firstPlayer Le joueur qui doit commencer la première mise (optionnel).
     * @return Une liste des joueurs dans l'ordre de leur classement final.
     */
    fun startRound(firstPlayer: Player?): List<Player> {
        Utils.printGameLifecycle("Début des plis")
        val ranking = mutableListOf<Player>()
        var firstPlayerLocal = firstPlayer ?: players.first()
        var round = 0

        // Fonction locale pour récupérer les joueurs encore actifs (ayant des cartes en main).
        fun activePlayers() = players.filter { it.hand.isNotEmpty() }

        // Fonction locale pour jouer un pli.
        fun playPile() {
            if (activePlayers().size <= 1) return

            val pile = mutableListOf<Card>() // Pile de cartes en jeu.
            val discardPile = mutableListOf<Card>() // Pile de défausse.
            val maxRank = Card.Rank.entries.maxByOrNull { it.ordinal } // Rang maximum des cartes.
            val straightEnable = parameters.gameModeParameters.withStraight // Activation des suites.
            val starter = firstPlayerLocal // Premier joueur du pli.
            val starterIndex = players.indexOf(starter).takeIf { it >= 0 } ?: 0

            /**
             * Fonction récursive pour gérer les tours d'un pli.
             *
             * @param turnOffset Décalage pour déterminer le joueur actuel.
             * @param passes Ensemble des joueurs ayant passé leur tour.
             * @param lastPlay Dernier coup joué.
             * @param lastPlayer Dernier joueur ayant joué.
             * @param anyPlayHappened Indique si un coup valide a été joué dans le pli.
             * @param prevPlay Avant-dernier coup joué (pour certaines règles spécifiques).
             * @param playsInARow Nombre de coups consécutifs joués par le même joueur.
             * @return Le joueur ayant remporté le pli, ou null si aucun.
             */
            tailrec fun recurse(
                turnOffset: Int,
                passes: Set<Player>,
                lastPlay: Play?,
                lastPlayer: Player?,
                anyPlayHappened: Boolean,
                prevPlay: Play?,
                playsInARow: Int
            ): Player? {
                val current = players[(starterIndex + turnOffset) % players.size] // Joueur actuel.
                val mutablePasses = passes.toMutableSet()
                var lp = lastPlay
                var lplayer = lastPlayer
                var anyPlay = anyPlayHappened
                var prev = prevPlay
                var consecutive = playsInARow

                // Si le joueur actuel n'a plus de cartes, passer au suivant.
                if (current.hand.isEmpty()) {
                    return recurse(turnOffset + 1, mutablePasses, lp, lplayer, anyPlay, prev, consecutive)
                }

                // Vérifie si tout le monde a passé après un coup valide.
                if (lp != null && lplayer != null) {
                    val others = players.filter { it.hand.isNotEmpty() && it != lplayer }
                    if (others.all { it in mutablePasses }) {
                        discardPile.addAll(pile) // Défausse les cartes du pli.
                        pile.clear()
                        Utils.printGameLifecycle("Pli remporté par ${lplayer.id} (tout le monde a passé)")
                        return lplayer
                    }
                }

                // Si aucun coup n'a été joué et que tous les joueurs actifs ont passé.
                val active = activePlayers()
                if (!anyPlay && mutablePasses.containsAll(active)) {
                    val nextStartIndex = (starterIndex + 1) % players.size
                    val nextStarter = players.subList(nextStartIndex, players.size) +
                            players.subList(0, nextStartIndex)
                    return nextStarter.firstOrNull { it.hand.isNotEmpty() } ?: starter
                }

                // Détermine si une contrainte de "force play" s'applique.
                val forcePlayRank: Card.Rank? = if (parameters.gameModeParameters.withForcePlay
                    && consecutive >= 2 && prev != null && lp != null && prev.getRank() == lp.getRank()
                ) {
                    lp.getRank()
                } else null

                // Le joueur actuel joue son tour.
                val play = try {
                    current.playTurn(pile, discardPile, lp, forcePlayRank)
                } catch (e: Exception) {
                    Utils.debug("Exception during ${current.id}.playTurn: ${e.message}")
                    null
                }

                // Si le joueur ne respecte pas la contrainte de "force play", il passe.
                if (play != null && forcePlayRank != null && play.none { it.rank == forcePlayRank }) {
                    Utils.printAction(current.id, "ne respecte pas la contrainte Ta Gueule -> passe")
                    val newPasses = mutablePasses.toMutableSet()
                    newPasses.add(current)
                    consecutive = 0
                    return recurse(turnOffset + 1, newPasses, lp, lplayer, anyPlay, prev, consecutive)
                }

                // Si le joueur passe son tour.
                if (play == null) {
                    consecutive = 0
                    mutablePasses.add(current)
                    Utils.printAction(current.id, "passe")
                    return recurse(turnOffset + 1, mutablePasses, lp, lplayer, anyPlay, prev, consecutive)
                } else {
                    // Vérifie si le coup est valide.
                    if (lp != null && play.playType != lp.playType) {
                        consecutive = 0
                        mutablePasses.add(current)
                        Utils.printAction(current.id, "pose un type différent -> invalide")
                        return recurse(turnOffset + 1, mutablePasses, lp, lplayer, anyPlay, prev, consecutive)
                    }
                    if (!play.canBePlayedOn(lp)) {
                        consecutive = 0
                        mutablePasses.add(current)
                        Utils.printAction(current.id, "pose mais ne peut pas jouer sur le dernier coup -> passe")
                        return recurse(turnOffset + 1, mutablePasses, lp, lplayer, anyPlay, prev, consecutive)
                    }

                    // Ajoute les cartes jouées à la pile et les retire de la main du joueur.
                    play.forEach { card ->
                        if (current.hand.remove(card)) {
                            pile.add(card)
                        }
                    }

                    anyPlay = true
                    mutablePasses.clear()

                    prev = if (consecutive >= 1) lp else null
                    lp = play
                    lplayer = current
                    consecutive += 1

                    Utils.printPlay(current.id, play)

                    // Vérifie si le pli est remporté par un coup spécial.
                    if (maxRank != null && play.any { it.rank == maxRank }) {
                        discardPile.addAll(pile)
                        pile.clear()
                        Utils.printGameLifecycle("${current.id} remporte le pli en posant un ${maxRank}")
                        return current
                    }

                    if (straightEnable && pile.size >= 4) {
                        val lastFour = pile.takeLast(4)
                        val allSameRank = lastFour.map { it.rank }.distinct().size == 1
                        if (allSameRank) {
                            discardPile.addAll(pile)
                            pile.clear()
                            Utils.printGameLifecycle("${current.id} remporte le pli par Carré magique")
                            return current
                        }
                    }

                    // Si le joueur actuel vide sa main, détermine le prochain gagnant.
                    if (current == starter && current.hand.isEmpty()) {
                        val nextWinner = players.dropWhile { it != current }
                            .drop(1)
                            .plus(players.takeWhile { it != current })
                            .firstOrNull { it.hand.isNotEmpty() } ?: current
                        discardPile.addAll(pile)
                        pile.clear()
                        Utils.printGameLifecycle("${current.id} a vidé sa main en premier -> ${nextWinner.id} remporte ce pli")
                        return nextWinner
                    }

                    return recurse(turnOffset + 1, mutablePasses, lp, lplayer, anyPlay, prev, consecutive)
                }
            }

            // Détermine le gagnant du pli.
            val winner = recurse(0, emptySet(), null, null, false, null, 0)
            if (winner != null) {
                firstPlayerLocal = winner
                Utils.printGameLifecycle("Premier joueur pour le pli suivant: ${winner.id}")
            }
        }

        // Boucle principale pour jouer les plis jusqu'à ce qu'il reste un seul joueur actif.
        while (players.count { it.hand.isNotEmpty() } > 1) {
            playPile()
            players.filter { it.hand.isEmpty() && it !in ranking }.forEach {
                ranking.add(it)
                Utils.printGameLifecycle("${it.id} a terminé (position ${ranking.size})")
            }
            round++
        }

        // Ajoute les joueurs restants au classement.
        ranking.addAll(players.filter { it !in ranking })
        Utils.printGameLifecycle("Fin des plis, classement: ${ranking.map { it.id }}")
        return ranking
    }

    /**
     * Expose les coups valides pour un joueur.
     *
     * @param hand La main du joueur.
     * @param lastPlay Le dernier coup joué.
     * @param pile La pile de cartes en jeu.
     * @param straightRank Le rang pour les suites (si applicable).
     * @return Une liste des coups possibles pour le joueur.
     */
    fun validPlaysForPlayer(hand: List<Card>, lastPlay: Play?, pile: List<Card>, straightRank: Card.Rank?) =
        PlayerUtils.possiblePlays(hand, lastPlay, pile, straightRank)
}