package site.ftka.proxycore.services.playerdata.events

import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.ResultedEvent.GenericResult
import site.ftka.proxycore.services.playerdata.objects.PlayerData
import java.util.UUID

class PlayerDataJoinEvent(val uuid: UUID, val playerdata: PlayerData?): ResultedEvent<GenericResult> {

    /*
        JoinEvent but managed after PlayerData is ready
     */


    override fun getResult(): GenericResult {
        TODO("Not yet implemented")
    }

    override fun setResult(result: GenericResult?) {
        TODO("Not yet implemented")
    }

}