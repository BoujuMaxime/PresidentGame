package model

import model.player.Player
import model.player.PlayerUtils

/**
 * RoundManager extrait la logique de déroulement des plis/manches.
 * Il est stateful pour faciliter les tests (pile et défausse internes par pli).
 */
class RoundManager(
    private val parameters: Game.GameParameters,
    private val players: MutableList<Player>
) {

    /**
     * Démarre une manche complète et renvoie le classement final (dans l'ordre d'arrivée).
     * @param firstPlayer joueur qui doit commencer la première mise (optionnel)
     */
    fun startRound(firstPlayer: Player?): List<Player> {
        Utils.printGameLifecycle("Début des plis")
        val ranking = mutableListOf<Player>()
        var firstPlayerLocal = firstPlayer ?: players.first()
        var round = 0

        fun activePlayers() = players.filter { it.hand.isNotEmpty() }

        fun playPile() {
            if (activePlayers().size <= 1) return

            val pile = mutableListOf<Card>()
            val discardPile = mutableListOf<Card>()
            val maxRank = Card.Rank.entries.maxByOrNull { it.ordinal }
            val straightEnable = parameters.gameModeParameters.withStraight
            val starter = firstPlayerLocal
            val starterIndex = players.indexOf(starter).takeIf { it >= 0 } ?: 0

            tailrec fun recurse(
                turnOffset: Int,
                passes: Set<Player>,
                lastPlay: Play?,
                lastPlayer: Player?,
                anyPlayHappened: Boolean,
                prevPlay: Play?,
                playsInARow: Int
            ): Player? {
                val current = players[(starterIndex + turnOffset) % players.size]
                val mutablePasses = passes.toMutableSet()
                var lp = lastPlay
                var lplayer = lastPlayer
                var anyPlay = anyPlayHappened
                var prev = prevPlay
                var consecutive = playsInARow

                if (current.hand.isEmpty()) {
                    return recurse(turnOffset + 1, mutablePasses, lp, lplayer, anyPlay, prev, consecutive)
                }

                if (lp != null && lplayer != null) {
                    val others = players.filter { it.hand.isNotEmpty() && it != lplayer }
                    if (others.all { it in mutablePasses }) {
                        discardPile.addAll(pile)
                        pile.clear()
                        Utils.printGameLifecycle("Pli remporté par ${lplayer.id} (tout le monde a passé)")
                        return lplayer
                    }
                }

                val active = activePlayers()
                if (!anyPlay && mutablePasses.containsAll(active)) {
                    val nextStartIndex = (starterIndex + 1) % players.size
                    val nextStarter = players.subList(nextStartIndex, players.size) +
                            players.subList(0, nextStartIndex)
                    return nextStarter.firstOrNull { it.hand.isNotEmpty() } ?: starter
                }

                val forcePlayRank: Card.Rank? = if (parameters.gameModeParameters.withForcePlay
                    && consecutive >= 2 && prev != null && lp != null && prev.getRank() == lp.getRank()
                ) {
                    lp.getRank()
                } else null

                val play = try {
                    current.playTurn(pile, discardPile, lp, forcePlayRank)
                } catch (e: Exception) {
                    Utils.debug("Exception during ${current.id}.playTurn: ${e.message}")
                    null
                }

                // Si play est non nul, vérifier qu'il respecte la contrainte de force play
                if (play != null && forcePlayRank != null && play.none { it.rank == forcePlayRank }) {
                    // Le joueur n'a pas respecté la contrainte -> considérer comme passe
                    Utils.printAction(current.id, "ne respecte pas la contrainte Ta Gueule -> passe")
                    val newPasses = mutablePasses.toMutableSet()
                    newPasses.add(current)
                    consecutive = 0
                    return recurse(turnOffset + 1, newPasses, lp, lplayer, anyPlay, prev, consecutive)
                }

                if (play == null) {
                    consecutive = 0
                    mutablePasses.add(current)
                    Utils.printAction(current.id, "passe")
                    return recurse(turnOffset + 1, mutablePasses, lp, lplayer, anyPlay, prev, consecutive)
                } else {
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

            val winner = recurse(0, emptySet(), null, null, false, null, 0)
            if (winner != null) {
                firstPlayerLocal = winner
                Utils.printGameLifecycle("Premier joueur pour le pli suivant: ${winner.id}")
            }
        }

        while (players.count { it.hand.isNotEmpty() } > 1) {
            playPile()
            players.filter { it.hand.isEmpty() && it !in ranking }.forEach {
                ranking.add(it)
                Utils.printGameLifecycle("${it.id} a terminé (position ${ranking.size})")
            }
            round++
        }

        ranking.addAll(players.filter { it !in ranking })
        Utils.printGameLifecycle("Fin des plis, classement: ${ranking.map { it.id }}")
        return ranking
    }

    /**
     * Expose les coups valides pour un joueur (utilise PlayerUtils).
     */
    fun validPlaysForPlayer(hand: List<Card>, lastPlay: Play?, pile: List<Card>, straightRank: Card.Rank?) =
        PlayerUtils.possiblePlays(hand, lastPlay, pile, straightRank)
}