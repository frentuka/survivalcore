package site.ftka.survivalcore.essentials.actionbar.objects

/**
 * Defines how multiple active action bar layers should be rendered together on the client's screen.
 */
enum class ActionBarStrategy {
    /**
     * Joins all active layers together using a separator, sorted by priority.
     */
    CONCATENATION,

    /**
     * Displays only the layer with the highest priority.
     */
    EXCLUSIVE,

    /**
     * Cycles through the active layers periodically (e.g. every 2 seconds).
     */
    CAROUSEL
}
