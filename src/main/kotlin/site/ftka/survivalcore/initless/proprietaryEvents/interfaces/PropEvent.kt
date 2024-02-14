package site.ftka.survivalcore.initless.proprietaryEvents.interfaces

interface PropEvent {
    val name: String
    val async: Boolean
    var cancelled: Boolean
}