package model.player

import model.Card

abstract class Player(
    val id: String,
    val hand: MutableList<Card>,
    var role: Role = Role.NEUTRAL
) : PlayerInterface {
    enum class Role { PRESIDENT, VICE_PRESIDENT, NEUTRAL, VICE_ASSHOLE, ASSHOLE }
}