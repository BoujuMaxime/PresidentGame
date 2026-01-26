package view

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import view.themes.ThemeManager

class ThemeManagerTest {

    @Test
    fun `default theme is casino`() {
        val manager = ThemeManager()
        assertEquals("casino", manager.getCurrentTheme())
    }

    @Test
    fun `setTheme changes current theme`() {
        val manager = ThemeManager()
        manager.setTheme("dark")
        assertEquals("dark", manager.getCurrentTheme())
    }

    @Test
    fun `setTheme ignores invalid theme`() {
        val manager = ThemeManager()
        manager.setTheme("invalid_theme")
        assertEquals("casino", manager.getCurrentTheme())
    }

    @Test
    fun `availableThemes contains all themes`() {
        val manager = ThemeManager()
        assertTrue(manager.availableThemes.containsKey("casino"))
        assertTrue(manager.availableThemes.containsKey("cartoon"))
        assertTrue(manager.availableThemes.containsKey("dark"))
        assertTrue(manager.availableThemes.containsKey("minimal"))
        assertEquals(4, manager.availableThemes.size)
    }

    @Test
    fun `getThemeColors returns valid colors for each theme`() {
        val manager = ThemeManager()

        listOf("casino", "cartoon", "dark", "minimal").forEach { themeName ->
            manager.setTheme(themeName)
            val colors = manager.getThemeColors()
            assertNotNull(colors.primary)
            assertNotNull(colors.accent)
            assertNotNull(colors.text)
            assertNotNull(colors.cardRed)
            assertNotNull(colors.cardBlack)
        }
    }

    @Test
    fun `theme display names are not blank`() {
        val manager = ThemeManager()
        manager.availableThemes.values.forEach { displayName ->
            assertTrue(displayName.isNotBlank())
        }
    }
}
