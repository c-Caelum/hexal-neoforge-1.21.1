
package ram.talia.hexal.common.casting.actions.spells.motes

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import ram.talia.hexal.api.casting.castables.CastingImageConstMediaAction
import ram.talia.hexal.api.casting.iota.MoteIota
import ram.talia.hexal.api.casting.mishaps.MishapNoBoundStorage
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager
import ram.talia.hexal.common.lib.hex.HexalImageComponents

object OpGetStorageRemainingCapacity : CastingImageConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, image: CastingImage, env: CastingEnvironment): List<Iota> {

        val storageId = image.getComponent(HexalImageComponents.TEMP_STORAGE)
            ?: (env.castingEntity as? ServerPlayer)?.let { MediafiedItemManager.getBoundStorage(it) }
            ?: throw MishapNoBoundStorage()
        if (!MediafiedItemManager.isStorageLoaded(storageId))
            throw MishapNoBoundStorage("storage_unloaded")
        val storage = MediafiedItemManager.getStorage(storageId)?.get() ?: throw MishapNoBoundStorage()

        return (HexalConfig.Server.MAX_RECORDS_IN_MEDIAFIED_STORAGE.get() - storage.storedItems.size).asActionResult
    }
}
