package site.ftka.survivalcore.essentials.proprietaryEvents.objects

interface PropEvent {
    val name: String
    val async: Boolean
    var cancelled: Boolean
}