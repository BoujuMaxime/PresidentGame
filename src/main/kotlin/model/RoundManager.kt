package model

import model.player.Player

/**
 * Gère les tours de jeu, incluant les tours des joueurs, la mise à jour des classements et les règles spéciales.
 *
 * @property parameters Les paramètres de la partie qui définissent les règles et options.
 * @property players La liste des joueurs participant à la partie.
 */
class RoundManager(
    private val parameters: Game.GameParameters,
    private val players: MutableList<Player>
) {

    /**
     * Lance un nouveau tour de jeu.
     *
     * @param firstPlayer Le joueur qui commence le tour. Si null, le premier joueur de la liste est utilisé.
     * @return La liste des joueurs classés selon leurs performances sur ce tour.
     */
    fun startRound(firstPlayer: Player?): List<Player> {
        Utils.printGameLifecycle("Début des plis")
        val ranking = mutableListOf<Player>()
        val discardPile = mutableListOf<Card>()
        val firstPlayerLocal = firstPlayer ?: players.first()

        // Continue à jouer tant que plus d’un joueur a encore des cartes.
        while (activePlayers().size > 1) {
            playPile(firstPlayerLocal, discardPile)
            updateRanking(ranking)
        }

        // Ajoute les joueurs restants au classement.
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
     * @param ranking La liste de classement actuelle à mettre à jour.
     */
    private fun updateRanking(ranking: MutableList<Player>) {
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
    private fun playPile(firstPlayerLocal: Player, discardPile: MutableList<Card>) {
        if (activePlayers().size <= 1) return

        val pile = mutableListOf<Card>()
        val starterIndex = players.indexOf(firstPlayerLocal).takeIf { it >= 0 } ?: 0
        val passes = mutableSetOf<Player>()
        var lastPlay: Play? = null
        var lastPlayer: Player? = null
        var playsInARow = 0

        // Boucle tant que le pli n’est pas résolu.
        while (true) {
            val current = players[(starterIndex + playsInARow) % players.size]
            if (current.hand.isEmpty()) {
                playsInARow++
                continue
            }

            if (hasEveryoneButLastPassed(lastPlay, lastPlayer, passes)) {
                endPile(discardPile, pile, lastPlayer, "Tous les autres ont passé")
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
     * Tente de jouer pour le joueur actuel.
     *
     * @param current Le joueur dont c’est le tour.
     * @param pile La pile de cartes en cours.
     * @param discardPile La pile de cartes défaussées.
     * @param lastPlay Le dernier coup joué dans la pile.
     * @param playsInARow Le nombre de tours consécutifs déjà joués.
     * @return Le coup joué par le joueur, ou null s’il passe.
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
     * Calcule la valeur de la carte qui doit être jouée lorsque le mode force play est activé.
     *
     * @param lastPlay Le dernier coup joué dans le pli.
     * @param playsInARow Le nombre de tours consécutifs déjà joués.
     * @return La valeur de la carte à jouer, ou null si le mode force play n’est pas applicable.
     */
    private fun computeForcePlayRank(lastPlay: Play?, playsInARow: Int): Card.Rank? {
        return if (parameters.gameModeParameters.withForcePlay
            && playsInARow >= 2 && lastPlay != null
        ) {
            lastPlay.getRank()
        } else null
    }

    /**
     * Vérifie si tous les joueurs sauf le dernier ont passé.
     *
     * @param lastPlay Le dernier coup joué dans le pli.
     * @param lastPlayer Le joueur ayant effectué le dernier coup.
     * @param passes L’ensemble des joueurs ayant passé.
     * @return True si tous les joueurs sauf le dernier ont passé, false sinon.
     */
    private fun hasEveryoneButLastPassed(lastPlay: Play?, lastPlayer: Player?, passes: Set<Player>) =
        lastPlay != null && lastPlayer != null &&
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
        Utils.printGameLifecycle("Pli remporté par ${lastPlayer?.id}: $reason")
    }

    /**
     * Vérifie et applique les règles spéciales pour le coup en cours.
     *
     * @param play Le coup joué par le joueur actuel.
     * @param pile La pile de cartes en cours.
     * @param discardPile La pile de cartes défaussées.
     * @param current Le joueur ayant effectué le coup.
     * @return True si le pli se termine à cause d’une règle spéciale, false sinon.
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
                endPile(discardPile, pile, current, "A joué un $maxRank")
                true
            }

            straightEnable && pile.size >= 4 && pile.takeLast(4).map { it.rank }.distinct().size == 1 -> {
                endPile(discardPile, pile, current, "Carré Magique")
                true
            }

            current.hand.isEmpty() -> {
                endPile(discardPile, pile, current, "${current.id} a vidé sa main")
                true
            }

            else -> false
        }
    }

    /**
     * Applique le coup actuel au pli en retirant les cartes de la main du joueur.
     *
     * @param play Le coup joué par le joueur actuel.
     * @param current Le joueur ayant effectué le coup.
     * @param pile La pile de cartes en cours.
     */
    private fun applyPlayToPile(play: Play, current: Player, pile: MutableList<Card>) {
        play.forEach { if (current.hand.remove(it)) pile.add(it) }
        Utils.printPlay(current.id, play)
    }
}