package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import net.minecraft.world.entity.Entity
import ram.talia.hexal.api.casting.mishaps.MishapOthersWisp
import ram.talia.hexal.common.entities.BaseCastingWisp

object OpWispHex : ConstMediaAction {
	override val argc = 1

	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		val wisp = args.getEntity(env.world,0, argc)

		if (wisp !is BaseCastingWisp)
			throw MishapInvalidIota.ofType(args[0], 0, "wisp")
		if ((wisp.caster as Entity) != env.castingEntity)
			throw MishapOthersWisp(wisp.caster)

		return listOf(wisp.serHex)
	}
}