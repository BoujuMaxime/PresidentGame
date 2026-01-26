package view

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import view.components.SpeechBubble

class SpeechBubbleTest {

    @Test
    fun `formatAction formats pass action`() {
        val result = SpeechBubble.formatAction("passe son tour")
        assertEquals("💤 Passe", result)
    }

    @Test
    fun `formatAction formats play action`() {
        val result = SpeechBubble.formatAction("joue: 2xAs")
        assertEquals("🎴  2xAs", result)
    }

    @Test
    fun `formatAction returns original for unknown action`() {
        val result = SpeechBubble.formatAction("autre action")
        assertEquals("autre action", result)
    }

    @Test
    fun `formatAction handles passe in different contexts`() {
        assertEquals("💤 Passe", SpeechBubble.formatAction("IA passe"))
        assertEquals("💤 Passe", SpeechBubble.formatAction("Joueur passe son tour"))
    }

    @Test
    fun `formatAction handles joue with various cards`() {
        assertEquals("🎴  3 de Coeur", SpeechBubble.formatAction("joue: 3 de Coeur"))
        assertEquals("🎴  2xRoi", SpeechBubble.formatAction("joue: 2xRoi"))
    }
}
