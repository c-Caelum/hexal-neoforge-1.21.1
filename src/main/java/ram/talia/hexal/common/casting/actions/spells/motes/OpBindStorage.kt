package ram.talia.hexal.common.casting.actions.spells.motes

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.casting.iota.MoteIota
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager
import ram.talia.hexal.common.blocks.BlockMediafiedStorage
import ram.talia.hexal.common.blocks.entity.BlockEntityMediafiedStorage
import ram.talia.hexal.xplat.IXplatAbstractions

class OpBindStorage(private val isTemporaryBinding: Boolean) : SpellAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val pos = args.getBlockPos(0, argc)

        env.assertVecInRange(pos.center)

        val storage = env.world.getBlockState(pos).block

        return SpellAction.Result(
            Spell(if (storage is BlockMediafiedStorage) pos else null, isTemporaryBinding),
            ((if (isTemporaryBinding) HexalConfig.Server.TEMP_BIND_STORAGE_COST.get() else HexalConfig.Server.BIND_STORAGE_COST.get())* (MediaConstants.DUST_UNIT.toDouble())).toLong(),
            listOf(ParticleSpray.burst(Vec3.atCenterOf(pos), 1.5))
        )
    }

    private data class Spell(val pos: BlockPos?, val isTemporaryBinding: Boolean) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            throw IllegalStateException("call cast(env, image) instead.")
        }

        override fun cast(env: CastingEnvironment, image: CastingImage): CastingImage? {
            val storage = env.world.getBlockEntity(pos) as? BlockEntityMediafiedStorage ?: return null

            val castingEntity : LivingEntity? = env.castingEntity;
            val castingPlayer : ServerPlayer? = castingEntity as? ServerPlayer;

            if (pos == null) {
                if (!(isTemporaryBinding) && castingPlayer != null) {
                    MediafiedItemManager.setBoundStorage(castingPlayer, null)
                    return null
                } else {
                    val userData = image.userData.copy()
                    userData.remove(MoteIota.TAG_TEMP_STORAGE)
                    return image.copy(userData = userData)
                }
            }

            if (isTemporaryBinding) {
                val userData = image.userData.copy()
                userData.putUUID(MoteIota.TAG_TEMP_STORAGE, storage.uuid)
                return image.copy(userData = userData)
            }

            if (!env.canEditBlockAt(pos) || !IXplatAbstractions.INSTANCE.isInteractingAllowed(env.world, pos, Direction.UP, env.castingHand, castingPlayer ?: return null))
                return null

            MediafiedItemManager.setBoundStorage(castingPlayer, storage.uuid)
            return null;
        }
    }
}
