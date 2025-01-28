package site.ftka.survivalcore.services.chat

import java.util.UUID

class ChatAPI(private val svc: ChatService) {

    fun getChanel(name: String)
        = svc.channels_ss.getChannel(name)

    fun getGlobalChannel()
        = svc.channels_ss.getGlobalChannel()

    fun getStaffChannel()
        = svc.channels_ss.getStaffChannel()

    fun getPlayerChannel(uuid: UUID)
        = svc.channels_ss.getPlayerChannel(uuid)



}