package site.ftka.survivalcore.essentials.usernameTracker.objects

import java.util.UUID

internal class UsernameTrackerElement {

    var username: String = "None"
    var uuid: UUID = UUID.randomUUID()
    var lastUpdate: Long = 0

}