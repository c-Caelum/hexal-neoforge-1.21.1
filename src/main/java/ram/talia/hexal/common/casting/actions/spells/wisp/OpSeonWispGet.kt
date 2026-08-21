package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import net.minecraft.server.level.ServerPlayer
import ram.talia.hexal.xplat.IXplatAbstractions

object OpSeonWispGet : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        return (env.castingEntity as? ServerPlayer)?.let { IXplatAbstractions.INSTANCE.getSeon(it)?.asActionResult }
            ?: listOf(NullIota())
    }
}
