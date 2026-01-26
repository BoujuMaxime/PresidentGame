package view.components

import javafx.animation.FadeTransition
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.shape.Line
import javafx.util.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Journal des événements de la partie avec historique scrollable.
 *
 * @author BOUJU Maxime
 */
class EventLog : VBox() {

    /**
     * Enumération des types d'événements avec leur icône et classe de style associée.
     */
    enum class EventType(val icon: String, val styleClass: String) {
        INFO("ℹ️", "event-info"),
        PLAYER_ACTION("🎴", "event-player"),
        AI_ACTION("🤖", "event-ai"),
        SYSTEM("⚙️", "event-system"),
        SUCCESS("✅", "event-success"),
        WARNING("⚠️", "event-warning"),
        ROUND("🏆", "event-round")
    }

    /** Conteneur pour les événements. */
    private val eventsContainer: VBox

    /** Panneau de défilement pour le conteneur d'événements. */
    private val scrollPane: ScrollPane

    /** Formateur pour l'heure des événements. */
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    companion object {
        /** Nombre maximum d'événements conservés. */
        private const val MAX_EVENTS = 50
    }

    init {
        styleClass.add("event-log")
        setVgrow(this, Priority.ALWAYS)

        eventsContainer = VBox(5.0).apply {
            styleClass.add("events-container")
            padding = Insets(10.0)
        }

        scrollPane = ScrollPane(eventsContainer).apply {
            styleClass.add("event-scroll-pane")
            isFitToWidth = true
            vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        }

        children.add(scrollPane)
        setVgrow(scrollPane, Priority.ALWAYS)
    }

    /**
     * Ajoute un événement au journal avec un type par défaut INFO.
     * @param message Le message de l'événement.
     * @param type Le type de l'événement.
     */
    fun addEvent(message: String, type: EventType = EventType.INFO) {
        val eventBox = createEventEntry(message, type)
        eventBox.opacity = 0.0
        eventsContainer.children.add(eventBox)

        FadeTransition(Duration.millis(300.0), eventBox).apply { toValue = 1.0 }.play()

        while (eventsContainer.children.size > MAX_EVENTS) {
            eventsContainer.children.removeAt(0)
        }

        scrollPane.applyCss()
        scrollPane.layout()
        scrollPane.vvalue = 1.0
    }

    /**
     * Crée une entrée d'événement sous forme de HBox.
     * @param message Le message de l'événement.
     * @param type Le type de l'événement.
     * @return L'HBox représentant l'entrée d'événement.
     */
    private fun createEventEntry(message: String, type: EventType): HBox {
        return HBox(6.0).apply {
            styleClass.addAll("event-entry", type.styleClass)
            alignment = Pos.CENTER_LEFT
            padding = Insets(5.0, 8.0, 5.0, 8.0)

            children.addAll(
                Label(LocalTime.now().format(timeFormatter)).apply {
                    styleClass.add("event-time")
                    minWidth = 55.0
                    maxWidth = 55.0
                },
                Label(type.icon).apply {
                    styleClass.add("event-icon")
                    minWidth = 20.0
                },
                Label(message).apply {
                    styleClass.add("event-message")
                    isWrapText = true
                    maxWidth = Double.MAX_VALUE
                    HBox.setHgrow(this, Priority.ALWAYS)
                }
            )
        }
    }

    /**
     * Efface tous les événements du journal.
     */
    fun clear() {
        eventsContainer.children.clear()
    }

    /**
     * Ajoute un séparateur avec un titre au journal.
     * @param title Le titre du séparateur.
     */
    fun addSeparator(title: String) {
        eventsContainer.children.add(HBox().apply {
            styleClass.add("event-separator")
            alignment = Pos.CENTER
            padding = Insets(10.0, 0.0, 10.0, 0.0)

            children.addAll(
                Line(0.0, 0.0, 50.0, 0.0).apply { styleClass.add("separator-line") },
                Label(title).apply {
                    styleClass.add("separator-label")
                    padding = Insets(0.0, 10.0, 0.0, 10.0)
                },
                Line(0.0, 0.0, 50.0, 0.0).apply { styleClass.add("separator-line") }
            )
        })
    }
}