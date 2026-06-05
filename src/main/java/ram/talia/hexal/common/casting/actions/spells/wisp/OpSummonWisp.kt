package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.getPositiveDouble
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.misc.MediaConstants
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.Hexal
import ram.talia.hexal.api.addBounded
import ram.talia.hexal.api.casting.eval.env.WispCastEnv
import ram.talia.hexal.api.casting.mishaps.MishapExcessiveReproduction
import ram.talia.hexal.api.casting.mishaps.MishapNeedsCaster
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.common.entities.ProjectileWisp
import ram.talia.hexal.common.entities.TickingWisp
import kotlin.math.max

class OpSummonWisp(val ticking: Boolean) : SpellAction {
    override val argc = if (ticking) 3 else 4

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        throw IllegalStateException("OpSummonWisp implements executeWithUserdata, execute shouldn't be called.")
    }

    override fun executeWithUserdata(args: List<Iota>, env: CastingEnvironment, userData: CompoundTag): SpellAction.Result {
        args.getList(0, argc)
        val hex = args[0] as ListIota;
        val pos = args.getVec3(1, argc)
        val media: Double
        val cost: Long

        val player = env.castingEntity as? ServerPlayer ?: throw MishapNeedsCaster()

        if (env is WispCastEnv && env.wisp.summonedChildThisCast)
            throw MishapExcessiveReproduction(env.wisp) // wisps can only summon one child per cast.

        val ravenmind = if (userData.contains(HexAPI.RAVENMIND_USERDATA)) {
            Hexal.deserializeIota(userData.getCompound(HexAPI.RAVENMIND_USERDATA))
        } else {
            NullIota()
        }

        val spell = when (ticking) {
            true -> {
                media = args.getPositiveDouble(2, argc)

                cost = Hexal.toMediaFromDust(HexalConfig.Server.SUMMON_TICKING_WISP_COST.get()).addBounded((media * MediaConstants.DUST_UNIT).toLong())
                Spell(player, true, pos, hex, ravenmind, (media * MediaConstants.DUST_UNIT).toLong(), env.world)
            }
            false -> {
                val vel = args.getVec3(2, argc)
                media = args.getPositiveDouble(3, argc)
                cost = max(
                    (HexalConfig.Server.SUMMON_PROJECTILE_WISP_COST.get() * vel.lengthSqr() * 10000.0).toLong(),
                    (HexalConfig.Server.SUMMON_PROJECTILE_WISP_COST.get() * 10000.0).toLong()
                )
                            .addBounded((media * MediaConstants.DUST_UNIT).toLong())
                Spell(player, false, pos, hex, ravenmind, (media * MediaConstants.DUST_UNIT).toLong(), env.world, vel)
            }
        }

        env.assertVecInRange(pos)

        return SpellAction.Result(
            spell!!,
            cost,
            listOf(ParticleSpray.burst(pos, 1.5), ParticleSpray.cloud(pos, 0.5))
        )
    }

    private data class Spell(val player: ServerPlayer, val ticking: Boolean, val pos: Vec3, val hex: ListIota, val ravenmind: Iota?, val media: Long, val level : ServerLevel, val vel: Vec3 = Vec3.ZERO) :
        RenderedSpell {
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
            wisp.setHex(hex, level)
            wisp.setRavenmind(ravenmind)
            env.world.addFreshEntity(wisp)
        }
    }
}