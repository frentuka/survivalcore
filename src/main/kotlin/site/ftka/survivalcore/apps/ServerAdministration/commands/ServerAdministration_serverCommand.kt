import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.TabCompleteEvent
import site.ftka.survivalcore.MClass

class ServerAdministration_serverCommand(private val src: ServerAdministrationApp, private val plugin: MClass): CommandExecutor, Listener {

    /*
        Command: server / sv
     */

    // shortcut vars
    private val essFwk = plugin.essentialsFwk
    private val servFwk = plugin.servicesFwk

    private val SERVER_ADMINISTRATION_PLAYER_PERMISSION = "staff.admin"

    private enum class ServicesEnum {
        CHAT, INVENTORYGUI, LANGUAGE, PERMISSIONS, PLAYERDATA
    }

    private enum class AppsEnum {
        NOTHING, HERE, YET
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>?): Boolean {
        if (sender is Player) {
            sender.sendMessage(servFwk.playerData.getPlayerDataMap().keys.toString())
            sender.sendMessage(sender.uniqueId.toString())
        }

        if (sender !is ConsoleCommandSender) { // check authorization
            if (sender !is Player) return false // can't happen
            else if (!servFwk.playerData.getPlayerDataMap().containsKey(sender.uniqueId)) return false // if not exists in pdata
            if (!servFwk.permissions.permissions_ss.playerHasPerm(sender.uniqueId, SERVER_ADMINISTRATION_PLAYER_PERMISSION)) {
                val noPermissionMessage = plugin.servicesFwk.language.api.playerLanguagePack(sender.uniqueId).command_error_player_noPermission
                sender.sendMessage(noPermissionMessage) // player doesn't have permission
                // return false (testing with no return. shouldn't be commented)
            }
        }

        //val msg = ServerAdminCommandMessages()

        // get help
        if (args == null || args.isEmpty()) {
            sender.sendMessage(src.lang.help_message)
            return false
        }

        if (args.get(0) == "service") return false


        return false
    }

    @EventHandler
    fun giveSuggestions(event: TabCompleteEvent) {
        if (!event.isCommand) return
        val processedBuffer = event.buffer.replace("/", "").split(" ").get(0).lowercase()
        if (processedBuffer != "server" && processedBuffer != "sv") return

        val splatBuffer = event.buffer.split(" ")
        event.completions.clear()

        // first stage: /cmd (suggestion)
        if (splatBuffer.size == 1) {
            event.completions.add(event.buffer + " app")
            event.completions.add(event.buffer + " service")
        }

        // second stage: /cmd <app/service> (suggestion)
        if (splatBuffer.size == 2) {
            when (splatBuffer[1].lowercase()) {
                "app" -> {
                    for (enums in AppsEnum.entries)
                        event.completions.add(event.buffer + " " + enums.name.lowercase())
                }

                "service" -> {
                    for (enums in ServicesEnum.entries)
                        event.completions.add(event.buffer + " " + enums.name.lowercase())
                }
            }
        }

        // third stage: /cmd <app/service> <selection> reload
        if (splatBuffer.size == 3)
            event.completions.add(event.buffer + " reload")
    }

}