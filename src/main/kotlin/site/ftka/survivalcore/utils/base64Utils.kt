package site.ftka.survivalcore.utils

import org.bukkit.inventory.ItemStack
import java.util.Base64

object base64Utils {

    fun toBase64(itemStack: ItemStack): String {
        val serializedItem = itemStack.serializeAsBytes()
        return Base64.getEncoder().encodeToString(serializedItem)
    }

    fun fromBase64(base64: String): ItemStack {
        val data = Base64.getDecoder().decode(base64)
        return ItemStack.deserializeBytes(data)
    }

}