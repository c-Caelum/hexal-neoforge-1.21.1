
package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.misc.MediaConstants
import gay.`object`.hexdebug.core.api.HexDebugCoreAPI
import gay.`object`.hexdebug.core.api.exceptions.DebugException
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.addBounded
import ram.talia.hexal.api.casting.eval.env.WispCastEnv
import ram.talia.hexal.api.casting.mishaps.MishapExcessiveReproduction
import ram.talia.hexal.api.casting.mishaps.MishapNeedsCaster
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.common.entities.ProjectileWisp
import ram.talia.hexal.common.entities.TickingWisp
import ram.talia.hexal.interop.hexdebug.WispDebugEnv
import kotlin.math.max

class OpSummonWisp(val ticking: Boolean) : SpellAction {
    override val argc = if (ticking) 3 else 4

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        throw IllegalStateException("OpSummonWisp implements executeWithUserdata, execute shouldn't be called.")
    }

    override fun executeWithUserdata(args: List<Iota>, env: CastingEnvironment, userData: CompoundTag): SpellAction.Result {
        val hex = args.getList(0, argc)
        val pos = args.getVec3(1, argc)
        val media: Double
        val cost: Long

        val player = env.castingEntity as? ServerPlayer ?: throw MishapNeedsCaster()

        if (env is WispCastEnv && env.wisp.summonedChildThisCast)
            throw MishapExcessiveReproduction(env.wisp) // wisps can only summon one child per cast.

        val ravenmind = if (userData.contains(HexAPI.RAVENMIND_USERDATA)) {
            IotaType.deserialize(userData.getCompound(HexAPI.RAVENMIND_USERDATA), env.world)
        } else {
            NullIota()
        }

        val spell = when (ticking) {
            true -> {
                media = args.getPositiveDouble(2, argc)
                cost = HexalConfig.server.summonTickingWispCost.addBounded((media * MediaConstants.DUST_UNIT).toLong())
                Spell(player, true, pos, hex.toList(), ravenmind, (media * MediaConstants.DUST_UNIT).toLong())
            }
            false -> {
                val vel = args.getVec3(2, argc)
                media = args.getPositiveDouble(3, argc)
                cost = max((HexalConfig.server.summonProjectileWispCost * vel.lengthSqr()).toLong(), HexalConfig.server.summonProjectileWispMinCost)
                            .addBounded((media * MediaConstants.DUST_UNIT).toLong())
                Spell(player, false, pos, hex.toList(), ravenmind, (media * MediaConstants.DUST_UNIT).toLong(), vel)
            }
        }

        env.assertVecInRange(pos)

        return SpellAction.Result(
            spell,
            cost,
            listOf(ParticleSpray.burst(pos, 1.5), ParticleSpray.cloud(pos, 0.5))
        )
    }

    private data class Spell(val player: ServerPlayer, val ticking: Boolean, val pos: Vec3, val hex: List<Iota>, val ravenmind: Iota?, val media: Long, val vel: Vec3 = Vec3.ZERO) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            // wisps can only summon one child per cast
            if (env is WispCastEnv)
                env.wisp.summonedChildThisCast = true

            val pigment = env.pigment
            val wisp = when (ticking) {
                true -> TickingWisp(env.world, pos, player, media)
                false -> ProjectileWisp(env.world, pos, vel, player, media)
            }
            wisp.setPigment(pigment)
            wisp.setHex(hex.toMutableList())
            wisp.setRavenmind(ravenmind)
            env.world.addFreshEntity(wisp)

            // if the current cast is being debugged, try to spawn the wisp in debug mode too
            if (HexDebugCoreAPI.INSTANCE.getDebugEnv(env) != null && wisp is TickingWisp) {
                val debugEnv = WispDebugEnv(player, wisp.uuid, ravenmind)
                try {
                    HexDebugCoreAPI.INSTANCE.createDebugThread(debugEnv, null)
                } catch (e: DebugException) {
                    // if there are no threads available, just spawn the wisp in normal mode
                    HexalAPI.LOGGER.debug("Not starting wisp in debug mode", e)
                    return
                }
                wisp.setDebugEnv(debugEnv)
            }
        }
    }
}
