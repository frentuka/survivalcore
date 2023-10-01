package site.ftka.proxycore.services.playerdata.events

import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.ResultedEvent.GenericResult
import site.ftka.proxycore.services.playerdata.objects.PlayerData
import java.util.UUID

class PlayerDataRegisterEvent(val uuid: UUID, val playerdata: PlayerData?): ResultedEvent<GenericResult> {

    /*
        Register event of player's data
     */


    override fun getResult(): GenericResult {
        TODO("Not yet implemented")
    }

    override fun setResult(result: GenericResult?) {
        TODO("Not yet implemented")
    }

}