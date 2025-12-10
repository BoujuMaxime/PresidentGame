package model.player

import model.Card

object PlayerUtils {
    fun sortHandByRank(hand: MutableList<model.Card>) {
        TODO("Not yet implemented")
    }

    /**
     * Retourne une liste de cartes jouables en fonction de la dernière combinaison jouée.
     *
     * @param hand La main du joueur.
     * @param lastPlayedCards La dernière combinaison de cartes ou carte jouée.
     * @return La liste des cartes jouables.
     */
    fun playableCards(hand: List<Card>, lastPlayedCards: List<Card>): List<Card> {
        TODO("Not yet implemented")
    }

    fun printHand(hand: List<Card>) {
        println("Votre main :")
        hand.forEachIndexed { index, card -> println("$index: $card") }
    }
}