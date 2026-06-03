package site.ftka.survivalcore.apps.PlayerDataManager.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.apps.PlayerDataManager.PlayerDataManagerApp
import site.ftka.survivalcore.apps.PlayerDataManager.gui.PlayerDataManager_OnlineListGUI
import site.ftka.survivalcore.apps.PlayerDataManager.gui.PlayerDataManager_MainGUI
import site.ftka.survivalcore.apps.PlayerDataManager.gui.PlayerDataManager_DeleteConfirmGUI

class PlayerDataManager_Command(private val app: PlayerDataManagerApp, private val plugin: MClass) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Only players can use this command.")
            return true
        }

        // Must have permission
        if (!plugin.servicesFwk.permissions.api.player_hasPerm_locally(sender.uniqueId, "staff.admin.playerdatamanager")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.").color(NamedTextColor.RED))
            return true
        }

        if (args.isEmpty()) {
            // Open Online List GUI
            val gui = PlayerDataManager_OnlineListGUI(plugin, sender)
            sender.openInventory(gui.inventory)
            return true
        }

        if (args.size == 1) {
            // Open MainGUI for specific player
            val targetName = args[0]
            val targetUUID = plugin.essentialsFwk.usernameTracker.getUUID(targetName)
            if (targetUUID == null) {
                sender.sendMessage(Component.text("Player '$targetName' not found in tracking database.").color(NamedTextColor.RED))
                return true
            }

            val gui = PlayerDataManager_MainGUI(plugin, sender, targetUUID)
            sender.openInventory(gui.inventory)
            return true
        }

        if (args.size == 2 && args[0].equals("delete", ignoreCase = true)) {
            val targetName = args[1]
            val targetUUID = plugin.essentialsFwk.usernameTracker.getUUID(targetName)
            if (targetUUID == null) {
                sender.sendMessage(Component.text("Player '$targetName' not found.").color(NamedTextColor.RED))
                return true
            }
            
            val gui = PlayerDataManager_DeleteConfirmGUI(plugin, sender, targetUUID)
            sender.openInventory(gui.inventory)
            return true
        }

        sender.sendMessage(Component.text("Usage: /pdata [player] | /pdata delete <player>").color(NamedTextColor.RED))
        return true
    }
}
