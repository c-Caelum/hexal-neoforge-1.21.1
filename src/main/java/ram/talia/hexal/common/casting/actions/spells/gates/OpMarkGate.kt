package ram.talia.hexal.common.casting.actions.spells.gates

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapImmuneEntity
import at.petrak.hexcasting.api.mod.HexTags
import net.minecraft.world.entity.Entity
import ram.talia.hexal.Hexal
import ram.talia.hexal.api.casting.iota.GateIota
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.getGate

object OpMarkGate : SpellAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val gate = args.getGate(0, argc)
        val entity = args.getEntity(env.world, 1, argc)
        env.assertEntityInRange(entity)

        if (entity.type.`is`(HexTags.Entities.CANNOT_TELEPORT))
            throw MishapImmuneEntity(entity)


        return SpellAction.Result(
            Spell(gate, entity),
            Hexal.toMediaFromDust(HexalConfig.Server.MARK_GATE_COST.get()),
            listOf(ParticleSpray.cloud(entity.position(), 1.0))
        )
    }

    private class Spell(val gate: GateIota, val entity: Entity) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            gate.mark(entity)
        }
    }
}