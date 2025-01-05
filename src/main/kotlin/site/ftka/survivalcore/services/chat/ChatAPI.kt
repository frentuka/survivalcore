package site.ftka.survivalcore.services.chat

import java.util.UUID

class ChatAPI(private val ess: ChatService) {

    fun getChanel(name: String)
        = ess.channels_ss.getChannel(name)

    fun getGlobalChannel()
        = ess.channels_ss.getGlobalChannel()

    fun getStaffChannel()
        = ess.channels_ss.getStaffChannel()

    fun getPlayerChannel(uuid: UUID)
        = ess.channels_ss.getPlayerChannel(uuid)



}