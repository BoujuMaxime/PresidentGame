// Kotlin
package model

import model.player.Player

/**
 * Gère les tours de jeu, incluant les tours des joueurs, la mise à jour des classements et les règles spéciales.
 *
 * @property parameters Les paramètres de la partie qui définissent les règles et options.
 * @property players La liste des joueurs participant à la partie.
 * @author BOUJU Maxime
 */
class GameTurns(
    private val parameters: Game.GameParameters,
    private val players: MutableList<Player>,
    private val onPileUpdated: ((List<Card>) -> Unit)? = null
) {
    /**
     * Le premier joueur à avoir vidé sa main lors du tour en cours.
     */
    private var firstPlayerToEmptyHand: Player? = null

    /**
     * Ordre chronologique des joueurs ayant vidé leur main pendant le tour.
     */
    private val finishedOrder = mutableListOf<Player>()

    /**
     * Lance un nouveau tour de jeu.
     *
     * @param firstPlayer Le joueur qui commence le tour. Si null, le premier joueur de la liste est utilisé.
     * @return La liste des joueurs classés selon leurs performances sur ce tour.
     */
    fun startTurn(firstPlayer: Player?): List<Player> {
        Utils.printGameLifecycle("Début des plis")
        val ranking = mutableListOf<Player>()
        firstPlayerToEmptyHand = null
        finishedOrder.clear()
        val discardPile = mutableListOf<Card>()
        var currentStarter = firstPlayer ?: players.first()

        while (activePlayers().size > 1) {
            currentStarter = playPile(currentStarter, discardPile) ?: currentStarter
            updateRanking(ranking)
        }

        ranking.addAll(players.filter { it !in ranking })
        Utils.printGameLifecycle("Fin des plis, classement: ${ranking.map { it.id }}")
        return ranking
    }

    /**
     * Récupère la liste des joueurs disposant encore de cartes en main.
     *
     * @return La liste des joueurs actifs.
     */
    private fun activePlayers() = players.filter { it.hand.isNotEmpty() }

    /**
     * Met à jour le classement des joueurs ayant vidé leur main.
     *
     * Utilise l'ordre chronologique `finishedOrder` pour conserver l'ordre réel des vidages.
     */
    private fun updateRanking(ranking: MutableList<Player>) {
        // Ajouter d'abord ceux enregistrés chronologiquement
        finishedOrder.filter { it !in ranking }.forEach {
            ranking.add(it)
            Utils.printGameLifecycle("${it.id} a terminé (position ${ranking.size})")
        }
        // En cas d'edge-case, ajouter aussi tout joueur vide non enregistré
        players.filter { it.hand.isEmpty() && it !in ranking }.forEach {
            ranking.add(it)
            Utils.printGameLifecycle("${it.id} a terminé (position ${ranking.size})")
        }
    }

    /**
     * Gère un pli de cartes, en orchestrant les tours des joueurs et l’application des règles du jeu.
     *
     * @param firstPlayerLocal Le joueur qui commence le pli.
     * @param discardPile La pile de cartes défaussées.
     */
    private fun playPile(firstPlayerLocal: Player, discardPile: MutableList<Card>): Player? {
        if (activePlayers().size <= 1) return null

        val pile = mutableListOf<Card>()
        onPileUpdated?.invoke(pile.toList())
        val starterIndex = players.indexOf(firstPlayerLocal).takeIf { it >= 0 } ?: 0
        val passes = mutableSetOf<Player>()
        var lastPlayerMove: PlayerMove? = null
        var lastPlayer: Player? = null
        var playsInARow = 0
        var previousRank: Card.Rank? = null


        // Boucle tant que le pli n’est pas résolu.
        while (true) {
            val size = players.size
            val currentIndex = ((starterIndex + playsInARow) % size + size) % size
            val current = players[currentIndex]
            if (current.hand.isEmpty()) {
                playsInARow++
                continue
            }

            if (hasEveryoneButLastPassed(lastPlayerMove, lastPlayer, passes)) {
                endPile(discardPile, pile, lastPlayer, "Tous les autres ont passé")
                break
            }

            val playMove = tryPlayMove(current, pile, discardPile, lastPlayerMove, playsInARow, previousRank)
            if (playMove == null) {
                passes.add(current)
                Utils.printAction(current.id, "passe")
                previousRank = null
                playsInARow++
                continue
            }

            applyPlayToPile(playMove, current, pile)
            onPileUpdated?.invoke(pile.toList())
            previousRank = lastPlayerMove?.getRank()
            lastPlayerMove = playMove
            lastPlayer = current
            if (checkSpecialRules(playMove, pile, discardPile, current)) break
            playsInARow++
            passes.clear()
        }
        return lastPlayer
    }

    /**
     * Tente de jouer pour le joueur actuel.
     *
     * @param current Le joueur dont c’est le tour.
     * @param pile La pile de cartes en cours.
     * @param discardPile La pile de cartes défaussées.
     * @param lastPlayerMove Le dernier coup joué dans la pile.
     * @param playsInARow Le nombre de tours consécutifs déjà joués.
     * @return Le coup joué par le joueur, ou null s’il passe.
     */
    private fun tryPlayMove(
        current: Player,
        pile: MutableList<Card>,
        discardPile: MutableList<Card>,
        lastPlayerMove: PlayerMove?,
        playsInARow: Int,
        previousRank: Card.Rank?
    ): PlayerMove? {
        val forcePlayRank = computeForcePlayRank(lastPlayerMove, playsInARow, previousRank)
        return try {
            current.playTurn(pile, discardPile, lastPlayerMove, forcePlayRank)
        } catch (e: Exception) {
            Utils.debug("Exception during ${current.id}.playTurn: ${e.message}")
            null
        }
    }

    /**
     * Calcule la valeur de la carte qui doit être jouée lorsque le mode force play est activé.
     *
     * @param lastPlayerMove Le dernier coup joué dans le pli.
     * @param playsInARow Le nombre de tours consécutifs déjà joués.
     * @return La valeur de la carte à jouer, ou null si le mode force play n’est pas applicable.
     */
    private fun computeForcePlayRank(
        lastPlayerMove: PlayerMove?,
        playsInARow: Int,
        previousRank: Card.Rank?
    ): Card.Rank? {
        return if (parameters.gameModeParameters.withForcePlay
            && playsInARow >= 2 && lastPlayerMove != null && previousRank == lastPlayerMove.getRank()
        ) {
            lastPlayerMove.getRank()
        } else null
    }

    /**
     * Vérifie si tous les joueurs sauf le dernier ont passé.
     *
     * @param lastPlayerMove Le dernier coup joué dans le pli.
     * @param lastPlayer Le joueur ayant effectué le dernier coup.
     * @param passes L’ensemble des joueurs ayant passé.
     * @return True si tous les joueurs sauf le dernier ont passé, false sinon.
     */
    private fun hasEveryoneButLastPassed(lastPlayerMove: PlayerMove?, lastPlayer: Player?, passes: Set<Player>) =
        lastPlayerMove != null && lastPlayer != null &&
                players.filter { it.hand.isNotEmpty() && it != lastPlayer }.all { it in passes }

    /**
     * Termine le pli courant en transférant toutes les cartes vers la pile de défausse.
     *
     * @param discardPile La pile de cartes défaussées.
     * @param pile La pile de cartes en cours.
     * @param lastPlayer Le joueur ayant remporté le pli.
     */
    private fun endPile(discardPile: MutableList<Card>, pile: MutableList<Card>, lastPlayer: Player?, reason: String) {
        discardPile.addAll(pile)
        pile.clear()
        onPileUpdated?.invoke(pile.toList())
        Utils.printGameLifecycle("Pli remporté par ${lastPlayer?.id}: $reason")
    }

    /**
     * Vérifie et applique les règles spéciales pour le coup en cours.
     *
     * @param playerMove Le coup joué par le joueur actuel.
     * @param pile La pile de cartes en cours.
     * @param discardPile La pile de cartes défaussées.
     * @param current Le joueur ayant effectué le coup.
     * @return True si le pli se termine à cause d’une règle spéciale, false sinon.
     */
    private fun checkSpecialRules(
        playerMove: PlayerMove,
        pile: MutableList<Card>,
        discardPile: MutableList<Card>,
        current: Player
    ): Boolean {
        val maxRank = Card.Rank.entries.maxByOrNull { it.ordinal }
        val straightEnable = parameters.gameModeParameters.withStraight

        return when {
            maxRank != null && playerMove.any { it.rank == maxRank } -> {
                endPile(discardPile, pile, current, "A joué un $maxRank")
                true
            }

            straightEnable && pile.size >= 4 && pile.takeLast(4).map { it.rank }.distinct().size == 1 -> {
                endPile(discardPile, pile, current, "Carré Magique")
                true
            }

            current.hand.isEmpty() && firstPlayerToEmptyHand == null -> {
                firstPlayerToEmptyHand = current
                endPile(discardPile, pile, current, "${current.id} est le président, on ne joue pas sur le président")
                true
            }

            else -> false
        }
    }

    /**
     * Applique le coup actuel au pli en retirant les cartes de la main du joueur.
     *
     * Enregistre dans `finishedOrder` si le joueur vide sa main pour conserver l'ordre chronologique.
     */
    private fun applyPlayToPile(playerMove: PlayerMove, current: Player, pile: MutableList<Card>) {
        playerMove.forEach { if (current.hand.remove(it)) pile.add(it) }
        Utils.printPlay(current.id, playerMove)

        // Enregistrer chronologiquement si le joueur vient de vider sa main
        if (current.hand.isEmpty() && current !in finishedOrder) {
            finishedOrder.add(current)
        }
    }
}
