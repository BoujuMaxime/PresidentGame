package model

import model.player.HumanPlayer
import model.player.Player
import model.player.ai.EvaluateAi
import model.player.ai.RandomAi
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GameTest {

    @BeforeEach
    fun disableConsoleOutput() {
        Utils.setConsoleEnabled(false)
    }

    @Test
    fun `startGame validates player count before running`() {
        val players: MutableList<Player> = mutableListOf(
            HumanPlayer("p1", mutableListOf()),
            HumanPlayer("p2", mutableListOf()),
            HumanPlayer("p3", mutableListOf())
        )
        val game = Game(Game.GameParameters(nbPlayers = 4), players)

        val ex = assertThrows(IllegalArgumentException::class.java) {
            game.startGame()
        }

        assertEquals("Le nombre de joueurs doit être 4", ex.message)
    }

    @Test
    fun `assignRolesByRanking maps positions to social roles`() {
        val players = listOf(
            HumanPlayer("p1", mutableListOf()),
            HumanPlayer("p2", mutableListOf()),
            HumanPlayer("p3", mutableListOf()),
            HumanPlayer("p4", mutableListOf()),
            HumanPlayer("p5", mutableListOf())
        )
        val game = Game(Game.GameParameters(nbPlayers = players.size))
        val method = Game::class.java.getDeclaredMethod("assignRolesByRanking", List::class.java)
        method.isAccessible = true
        method.invoke(game, players)

        assertEquals(Player.Role.PRESIDENT, players[0].role)
        assertEquals(Player.Role.VICE_PRESIDENT, players[1].role)
        assertEquals(Player.Role.VICE_ASSHOLE, players[players.lastIndex - 1].role)
        assertEquals(Player.Role.ASSHOLE, players[players.lastIndex].role)
        assertEquals(Player.Role.NEUTRAL, players[2].role)
    }
}