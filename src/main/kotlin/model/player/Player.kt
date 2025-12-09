package model.player

import model.Card

abstract class Player(
    val hand: MutableList<Card>,
    val id: String,
    val playerUtils: PlayerUtils = PlayerUtils
) : PlayerInterface