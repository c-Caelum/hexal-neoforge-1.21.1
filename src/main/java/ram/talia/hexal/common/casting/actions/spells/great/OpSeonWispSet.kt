package ram.talia.hexal.common.casting.actions.spells.great

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.server.level.ServerPlayer
import ram.talia.hexal.Hexal
import ram.talia.hexal.api.casting.mishaps.MishapOthersWisp
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.getBaseCastingWisp
import ram.talia.hexal.common.entities.BaseCastingWisp
import ram.talia.hexal.xplat.IXplatAbstractions

object OpSeonWispSet : SpellAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val wisp = args.getBaseCastingWisp(0, argc, env.world)

        if (wisp.caster != env.castingEntity)
            throw MishapOthersWisp(wisp.caster)

        return SpellAction.Result(Spell(wisp),
            Hexal.toMediaFromDust(HexalConfig.Server.SEON_WISP_SET_COST.get()),
            listOf(ParticleSpray.burst(wisp.position(), 1.0)))
    }

    private data class Spell(val wisp: BaseCastingWisp) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val caster = env.castingEntity as? ServerPlayer ?: return
            // seon can only be changed once the previous seon has died.
            val lastSeon = IXplatAbstractions.INSTANCE.getSeon(caster)
            if (lastSeon == null || lastSeon.isRemoved)
                IXplatAbstractions.INSTANCE.setSeon(caster, wisp)
        }
    }
}
