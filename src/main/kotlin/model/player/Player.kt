package model.player

import model.Card

abstract class Player(
    val id: String,
    val hand: MutableList<Card>,
    val role: Role = Role.NEUTRAL,
    val playerUtils: PlayerUtils = PlayerUtils
) : PlayerInterface {
    enum class Role { PRESIDENT, VICE_PRESIDENT, NEUTRAL, VICE_ASSHOLE, ASSHOLE }
}