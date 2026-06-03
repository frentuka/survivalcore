package site.ftka.survivalcore.services.chunkborder.objects

import java.util.concurrent.ConcurrentHashMap

data class BorderRegion(
    val id: String,
    val unlockedChunks: MutableSet<Pair<Int, Int>> = ConcurrentHashMap.newKeySet(),
    val blocks: ConcurrentHashMap<Long, CachedBlock> = ConcurrentHashMap()
) {
    companion object {
        fun packCoord(x: Int, y: Int, z: Int): Long {
            // x: 27 bits, z: 27 bits, y: 10 bits
            val bx = (x.toLong() and 0x7FFFFFFL) shl 37
            val bz = (z.toLong() and 0x7FFFFFFL) shl 10
            val by = (y.toLong() + 64L) and 0x3FFL
            return bx or bz or by
        }
        
        fun unpackX(packed: Long): Int {
            val v = ((packed ushr 37) and 0x7FFFFFFL).toInt()
            return if ((v and 0x4000000) != 0) v or -0x8000000 else v
        }
        
        fun unpackY(packed: Long): Int {
            return (packed and 0x3FFL).toInt() - 64
        }
        
        fun unpackZ(packed: Long): Int {
            val v = ((packed ushr 10) and 0x7FFFFFFL).toInt()
            return if ((v and 0x4000000) != 0) v or -0x8000000 else v
        }
    }
}

data class CachedBlock(
    val blockData: String,
    val isSolid: Boolean,
    val inventoryBase64: String? = null,
    val signLines: List<String>? = null,
    val signFrontColor: Int? = null,
    val signBackColor: Int? = null,
    val signFrontGlowing: Boolean? = null,
    val signBackGlowing: Boolean? = null
)
