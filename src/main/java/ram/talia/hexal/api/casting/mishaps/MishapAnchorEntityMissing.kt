package ram.talia.hexal.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor
import ram.talia.hexal.api.casting.iota.GateIota


class MishapAnchorEntityMissing(val gate: GateIota) : Mishap() {
    override fun accentColor(env: CastingEnvironment, errorCtx: Context): FrozenPigment = dyeColor(DyeColor.YELLOW)

    override fun errorMessage(env: CastingEnvironment, errorCtx: Context): Component = error("entity_gate_with_no_entity", gate.display())

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {}
}