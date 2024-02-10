package site.ftka.survivalcore.initless.proprietaryEvents.enums

enum class PropEventPriority(priority: Int) {

    /**
     * Event call is of very low importance and should be ran first, to allow
     * other plugins to further customise the outcome
     */
    FIRST(0),
    /**
     * Event call is neither important nor unimportant, and may be ran
     * normally
     */
    NORMAL(1),
    /**
     * Event call is critical and must have the final say in what happens
     * to the event
     */
    LAST(2),
    /**
     * Event is listened to purely for monitoring the outcome of an event.
     * <p>
     * No modifications to the event should be made under this priority
     */
    MONITOR(3);

}