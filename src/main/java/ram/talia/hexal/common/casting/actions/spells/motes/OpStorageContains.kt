package ram.talia.hexal.common.casting.actions.spells.motes

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerPlayer
import ram.talia.hexal.api.getMoteOrItemType
import ram.talia.hexal.common.blocks.entity.BlockEntityMediafiedStorage
import ram.talia.hexal.xplat.IXplatAbstractions

object OpStorageContains : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val storagePos = args.getBlockPos(0, argc)
        val moteOrItem = args.getMoteOrItemType(1, argc)

        env.assertVecInRange(storagePos.center)

        if (!env.canEditBlockAt(storagePos) || !IXplatAbstractions.INSTANCE.isInteractingAllowed(env.world, storagePos, Direction.UP, env.castingHand, env.castingEntity as? ServerPlayer))
            return false.asActionResult

        val storage = env.world.getBlockEntity(storagePos) as? BlockEntityMediafiedStorage ?: throw MishapInvalidIota.ofType(args[1], 0, "mediafied_storage")

        return moteOrItem?.map(
                { itemIota -> itemIota.record?.let { storage.getItemRecordsMatching(it) }?.isNotEmpty() ?: false },
                { item -> storage.getItemRecordsMatching(item).isNotEmpty() }
        )?.asActionResult ?: false.asActionResult
    }
}
