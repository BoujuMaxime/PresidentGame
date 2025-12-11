package model

import model.player.Player

/**
 * Manages the rounds of the game, including player turns, ranking updates, and special rules.
 *
 * @property parameters The game parameters that define the rules and settings.
 * @property players The list of players participating in the game.
 */
class RoundManager(
    private val parameters: Game.GameParameters,
    private val players: MutableList<Player>
) {

    /**
     * Starts a new round of the game.
     *
     * @param firstPlayer The player who starts the round. If null, the first player in the list is used.
     * @return A list of players ranked by their performance in the round.
     */
    fun startRound(firstPlayer: Player?): List<Player> {
        Utils.printGameLifecycle("Début des plis")
        val ranking = mutableListOf<Player>()
        val discardPile = mutableListOf<Card>()
        val firstPlayerLocal = firstPlayer ?: players.first()

        // Continue playing until only one player has cards left.
        while (activePlayers().size > 1) {
            playPile(firstPlayerLocal, discardPile)
            updateRanking(ranking)
        }

        // Add remaining players to the ranking.
        ranking.addAll(players.filter { it !in ranking })
        Utils.printGameLifecycle("Fin des plis, classement: ${ranking.map { it.id }}")
        return ranking
    }

    /**
     * Retrieves the list of players who still have cards in their hand.
     *
     * @return A list of active players.
     */
    private fun activePlayers() = players.filter { it.hand.isNotEmpty() }

    /**
     * Updates the ranking of players who have finished their hands.
     *
     * @param ranking The current ranking list to be updated.
     */
    private fun updateRanking(ranking: MutableList<Player>) {
        players.filter { it.hand.isEmpty() && it !in ranking }.forEach {
            ranking.add(it)
            Utils.printGameLifecycle("${it.id} a terminé (position ${ranking.size})")
        }
    }

    /**
     * Plays a pile of cards, managing player turns and applying game rules.
     *
     * @param firstPlayerLocal The player who starts the pile.
     * @param discardPile The pile of discarded cards.
     */
    private fun playPile(firstPlayerLocal: Player, discardPile: MutableList<Card>) {
        if (activePlayers().size <= 1) return

        val pile = mutableListOf<Card>()
        val starterIndex = players.indexOf(firstPlayerLocal).takeIf { it >= 0 } ?: 0
        val passes = mutableSetOf<Player>()
        var lastPlay: Play? = null
        var lastPlayer: Player? = null
        var playsInARow = 0

        // Loop until the pile is resolved.
        while (true) {
            val current = players[(starterIndex + playsInARow) % players.size]
            if (current.hand.isEmpty()) continue

            if (hasEveryoneButLastPassed(lastPlay, lastPlayer, passes)) {
                endPile(discardPile, pile, lastPlayer)
                break
            }

            val play = tryPlay(current, pile, discardPile, lastPlay, playsInARow)
            if (play == null) {
                passes.add(current)
                playsInARow++
                continue
            }

            applyPlayToPile(play, current, pile)
            if (checkSpecialRules(play, pile, discardPile, current)) break

            lastPlay = play
            lastPlayer = current
            playsInARow++
            passes.clear()
        }
    }

    /**
     * Attempts to play a turn for the current player.
     *
     * @param current The player whose turn it is.
     * @param pile The current pile of cards.
     * @param discardPile The pile of discarded cards.
     * @param lastPlay The last play made in the pile.
     * @param playsInARow The number of consecutive plays made.
     * @return The play made by the player, or null if the player passes.
     */
    private fun tryPlay(
        current: Player,
        pile: MutableList<Card>,
        discardPile: MutableList<Card>,
        lastPlay: Play?,
        playsInARow: Int
    ): Play? {
        val forcePlayRank = computeForcePlayRank(lastPlay, playsInARow)
        return try {
            current.playTurn(pile, discardPile, lastPlay, forcePlayRank)
        } catch (e: Exception) {
            Utils.debug("Exception during ${current.id}.playTurn: ${e.message}")
            null
        }
    }

    /**
     * Computes the rank of the card that must be played, if force play is enabled.
     *
     * @param lastPlay The last play made in the pile.
     * @param playsInARow The number of consecutive plays made.
     * @return The rank of the card to be played, or null if force play is not applicable.
     */
    private fun computeForcePlayRank(lastPlay: Play?, playsInARow: Int): Card.Rank? {
        return if (parameters.gameModeParameters.withForcePlay
            && playsInARow >= 2 && lastPlay != null
        ) {
            lastPlay.getRank()
        } else null
    }

    /**
     * Checks if all players except the last player have passed.
     *
     * @param lastPlay The last play made in the pile.
     * @param lastPlayer The player who made the last play.
     * @param passes The set of players who have passed.
     * @return True if all players except the last player have passed, false otherwise.
     */
    private fun hasEveryoneButLastPassed(lastPlay: Play?, lastPlayer: Player?, passes: Set<Player>) =
        lastPlay != null && lastPlayer != null &&
                players.filter { it.hand.isNotEmpty() && it != lastPlayer }.all { it in passes }

    /**
     * Ends the current pile, moving all cards to the discard pile.
     *
     * @param discardPile The pile of discarded cards.
     * @param pile The current pile of cards.
     * @param lastPlayer The player who won the pile.
     */
    private fun endPile(discardPile: MutableList<Card>, pile: MutableList<Card>, lastPlayer: Player?) {
        discardPile.addAll(pile)
        pile.clear()
        Utils.printGameLifecycle("Pli remporté par ${lastPlayer?.id}")
    }

    /**
     * Checks and applies special rules for the current play.
     *
     * @param play The play made by the current player.
     * @param pile The current pile of cards.
     * @param discardPile The pile of discarded cards.
     * @param current The player who made the play.
     * @return True if the pile ends due to a special rule, false otherwise.
     */
    private fun checkSpecialRules(
        play: Play,
        pile: MutableList<Card>,
        discardPile: MutableList<Card>,
        current: Player
    ): Boolean {
        val maxRank = Card.Rank.entries.maxByOrNull { it.ordinal }
        val straightEnable = parameters.gameModeParameters.withStraight

        return when {
            maxRank != null && play.any { it.rank == maxRank } -> {
                endPile(discardPile, pile, current)
                true
            }
            straightEnable && pile.size >= 4 && pile.takeLast(4).map { it.rank }.distinct().size == 1 -> {
                endPile(discardPile, pile, current)
                true
            }
            current.hand.isEmpty() -> {
                endPile(discardPile, pile, current)
                true
            }
            else -> false
        }
    }

    /**
     * Applies the current play to the pile, removing cards from the player's hand.
     *
     * @param play The play made by the current player.
     * @param current The player who made the play.
     * @param pile The current pile of cards.
     */
    private fun applyPlayToPile(play: Play, current: Player, pile: MutableList<Card>) {
        play.forEach { if (current.hand.remove(it)) pile.add(it) }
        Utils.printPlay(current.id, play)
    }
}