package ram.talia.hexal.common.lib.hex

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.eval.vm.ImageComponentType
import com.mojang.serialization.Codec
import net.minecraft.core.BlockPos
import net.minecraft.core.UUIDUtil
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.resources.ResourceLocation
import ram.talia.hexal.Hexal
import java.util.UUID
import java.util.function.BiConsumer

object HexalImageComponents {
    private val COMPONENTS: MutableMap<ResourceLocation, ImageComponentType<*>> = mutableMapOf()

    @JvmStatic
    fun register(r: BiConsumer<ImageComponentType<*>, ResourceLocation>) {
        for ((key, value) in COMPONENTS.entries) {
            r.accept(value, key)
        }
    }

    @JvmField
    val TEMP_STORAGE: ImageComponentType<UUID> = make("temp_storage",
        ImageComponentType(
            UUIDUtil.CODEC, UUIDUtil.STREAM_CODEC, false
        ))

    @JvmField
    val TIMES_TICKED_MAP: ImageComponentType<Map<BlockPos, Int>> = make("times_ticked_map",
        ImageComponentType(
            Codec.unboundedMap(BlockPos.CODEC, Codec.INT),
            ByteBufCodecs.map(::HashMap, BlockPos.STREAM_CODEC, ByteBufCodecs.INT),
            true
        ))

    fun <T: Any> make(name: String, comp: ImageComponentType<T>): ImageComponentType<T> {
        return if (COMPONENTS.put(Hexal.modLoc(name), comp) != null) {
            throw IllegalArgumentException("Typo? Duplicate id $name")
        } else {
            comp
        }
    }
}