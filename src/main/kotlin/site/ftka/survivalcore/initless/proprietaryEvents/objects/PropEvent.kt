package site.ftka.survivalcore.initless.proprietaryEvents.objects

interface PropEvent {
    val name: String
    val async: Boolean
    var cancelled: Boolean
}