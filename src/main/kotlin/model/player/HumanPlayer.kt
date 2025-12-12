package model.player

import model.Card
import model.PlayerMove

/**
 * Représente un joueur humain dans le jeu.
 *
 * @param id L'identifiant unique du joueur.
 * @param hand La main initiale du joueur, une liste mutable de cartes.
 */
class HumanPlayer(
    id: String,
    hand: MutableList<Card>
) : Player(id, hand) {

    /**
     * Affiche la main du joueur ainsi que les coups possibles.
     *
     * @param possibleMoves Une liste des coups possibles que le joueur peut effectuer.
     */
    private fun displayHand(possibleMoves: List<PlayerMove>) {
        PlayerUtils.printHand(hand) // Affiche les cartes dans la main.
        println("Coups possibles :")
        if (possibleMoves.isEmpty()) {
            println("  Aucun coup valide") // Message si aucun coup n'est possible.
        } else {
            possibleMoves.forEachIndexed { index, move ->
                println("  $index -> $move") // Affiche chaque coup possible avec son indice.
            }
        }
    }

    /**
     * Permet au joueur humain de jouer son tour.
     *
     * @param pile La pile principale de cartes.
     * @param discardPile La pile de défausse.
     * @param lastPlayerMove Le dernier coup joué par un autre joueur, ou null si aucun.
     * @param straightRank Le rang de la séquence en cours, ou null si aucune séquence.
     * @return Le coup choisi par le joueur, ou null si le joueur passe son tour.
     */
    override fun playTurn(
        pile: MutableList<Card>,
        discardPile: MutableList<Card>,
        lastPlayerMove: PlayerMove?,
        straightRank: Card.Rank?
    ): PlayerMove? {
        val possibleMoves = PlayerUtils.possiblePlays(hand, lastPlayerMove, straightRank)
        displayHand(possibleMoves) // Affiche la main et les coups possibles.
        println("Sélectionnez l'indice du coup à jouer ou appuyez sur Entrée pour passer :")
        while (true) {
            val input = readlnOrNull()?.trim() // Lit l'entrée utilisateur.
            val index = input?.toIntOrNull()
            if (input.isNullOrEmpty()) return null // Retourne null si l'utilisateur passe son tour.
            if (index != null && index in possibleMoves.indices) return possibleMoves[index] // Retourne le coup choisi.
            println("Indice invalide, recommencez ou appuyez sur Entrée pour passer.") // Message d'erreur pour une entrée invalide.
        }
    }

    /**
     * Ajoute des cartes à la main du joueur et les trie par rang.
     *
     * @param cards Les cartes à ajouter à la main du joueur.
     */
    override fun giveCardsToPlayer(cards: List<Card>) {
        hand.addAll(cards) // Ajoute les cartes à la main.
        PlayerUtils.sortHandByRank(hand) // Trie la main par rang.
    }
}