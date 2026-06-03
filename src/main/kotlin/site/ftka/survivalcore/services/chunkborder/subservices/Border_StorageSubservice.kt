package site.ftka.survivalcore.services.chunkborder.subservices

import com.google.gson.Gson
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.block.Sign
import org.bukkit.inventory.ItemStack
import site.ftka.survivalcore.MClass
import site.ftka.survivalcore.services.chunkborder.ChunkBorderService
import site.ftka.survivalcore.services.chunkborder.objects.CachedBlock
import java.util.Base64
import java.io.ByteArrayOutputStream
import org.bukkit.util.io.BukkitObjectOutputStream

class Border_StorageSubservice(private val service: ChunkBorderService, private val plugin: MClass) {

    private val gson = Gson()

    /**
     * Serializes a Bukkit Block into our CachedBlock representation.
     */
    fun serializeBlock(block: Block): CachedBlock {
        val blockDataStr = block.blockData.asString
        val state = block.state
        var inventoryBase64: String? = null
        var signLines: List<String>? = null
        var signFrontColor: Int? = null
        var signBackColor: Int? = null
        var signFrontGlowing: Boolean? = null
        var signBackGlowing: Boolean? = null

        if (state is Container) {
            inventoryBase64 = itemsToBase64(state.inventory.contents)
        } else if (state is Sign) {
            signLines = state.lines.toList()
            signFrontColor = state.color.color.asRGB()
            // In 1.20+, signs have front and back text, but we'll focus on front for simplicity,
            // or we could use the new SignSide API
            signFrontGlowing = state.isGlowingText
            
            // To be robust for 1.20+
            val frontSide = state.getSide(org.bukkit.block.sign.Side.FRONT)
            val backSide = state.getSide(org.bukkit.block.sign.Side.BACK)
            
            signLines = frontSide.lines.toList()
            signFrontColor = frontSide.color?.color?.asRGB()
            signFrontGlowing = frontSide.isGlowingText
            
            signBackColor = backSide.color?.color?.asRGB()
            signBackGlowing = backSide.isGlowingText
        }

        return CachedBlock(
            blockData = blockDataStr,
            isSolid = block.type.isSolid,
            inventoryBase64 = inventoryBase64,
            signLines = signLines,
            signFrontColor = signFrontColor,
            signBackColor = signBackColor,
            signFrontGlowing = signFrontGlowing,
            signBackGlowing = signBackGlowing
        )
    }

    private fun itemsToBase64(items: Array<out ItemStack?>): String {
        return try {
            val outputStream = ByteArrayOutputStream()
            val dataOutput = BukkitObjectOutputStream(outputStream)
            dataOutput.writeInt(items.size)
            for (item in items) {
                dataOutput.writeObject(item)
            }
            dataOutput.close()
            Base64.getEncoder().encodeToString(outputStream.toByteArray())
        } catch (e: Exception) {
            service.logger.log("Failed to serialize inventory: ${e.message}")
            ""
        }
    }
}
