package org.valkyrienskies.mod.common.air_pockets

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.ChunkStatus
import org.valkyrienskies.mod.util.FluidStateManager

internal class ShipChunkCache(private val level: Level) {
    // Non-null chunks, keyed by ChunkPos.asLong(cx, cz)
    private val chunks = Long2ObjectOpenHashMap<ChunkAccess>()
    // Tracks keys where getChunk returned null (don't re-fetch)
    private val nullChunks = LongOpenHashSet()

    fun getChunk(chunkX: Int, chunkZ: Int): ChunkAccess? {
        val key = ChunkPos.asLong(chunkX, chunkZ)
        if (nullChunks.contains(key)) return null
        val cached = chunks[key]  // returns null (default) if absent
        if (cached != null) return cached
        val chunk = level.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false)
        if (chunk == null) nullChunks.add(key) else chunks.put(key, chunk)
        return chunk
    }

    fun getFluidData(pos: BlockPos): FluidStateManager.FluidData? {
        val chunk = getChunk(
            SectionPos.blockToSectionCoord(pos.x),
            SectionPos.blockToSectionCoord(pos.z),
        ) ?: return null
        return FluidStateManager.getFluidData(chunk, pos)
    }
}
