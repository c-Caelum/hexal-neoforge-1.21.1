package ram.talia.hexal.common.casting.actions.spells.great

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.CastingEnvironmentComponent
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import com.mojang.datafixers.util.Either
import net.minecraft.server.level.ServerPlayer
import ram.talia.hexal.Hexal
import ram.talia.hexal.api.casting.eval.env.WispCastEnv
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.common.entities.BaseCastingWisp
import ram.talia.hexal.common.entities.IMediaEntity
import kotlin.math.ln
import kotlin.math.min

object OpConsumeWisp : SpellAction {
	override val argc = 1

	override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
		val consumed = args.getEntity(env.world, 0, argc) as? IMediaEntity<*> ?: throw MishapInvalidIota.ofType(args[0], 0, "consumable_entity")

		env.assertEntityInRange(consumed.get())

		val consumer: Either<BaseCastingWisp, ServerPlayer?> = if (env is WispCastEnv) Either.left(env.wisp) else Either.right(env.caster)

//		Hexal.LOGGER.debug("consumer: {}, {}", consumer, consumed.fightConsume(consumer))

		val selfConsume = consumer.map({wisp -> wisp == consumed},{false})
		val cost = when (consumed.fightConsume(consumer)) {
			false  -> if (selfConsume) 0 else Hexal.toMediaFromDust(HexalConfig.Server.CONSUME_WISP_OWN_WISP.get())
			true   -> (Hexal.toMediaFromDust(HexalConfig.Server.CONSUME_WISP_OTHERS_COST_PER_MEDIA.get()) * consumed.media)
		}

//		Hexal.LOGGER.debug("cost to consume {} is {}", consumed, cost)

		return SpellAction.Result(
			Spell(consumed),
			cost,
			listOf(ParticleSpray.burst(consumed.get().position(), 1.0, (ln(10.0) * 14 * ln(consumed.media/10.0 + 1)).toInt()))
		)
	}

	private data class Spell(val consumed: IMediaEntity<*>) : RenderedSpell {
		override fun cast(env: CastingEnvironment) {
			if (env is WispCastEnv) {
				if (env.wisp != consumed) env.wisp.addMedia(19 * consumed.media / 20)
			} else {
				val ext = env.getExtension(ExtractMediaHook.KEY)
				if (ext == null) {
					env.addExtension(ExtractMediaHook(19 * consumed.media / 20))
				} else {
					ext.consumedMedia += 19 * consumed.media / 20
				}
			}
			consumed.get().discard()
		}

		class ExtractMediaHook(var consumedMedia: Long) :
			CastingEnvironmentComponent.ExtractMedia.Pre {
			override fun onExtractMedia(cost: Long, simulate: Boolean): Long {
				val amountToUse = min(cost, consumedMedia)
				if (!simulate) consumedMedia -= amountToUse
				return cost - amountToUse
			}

			override fun getKey(): CastingEnvironmentComponent.Key<ExtractMediaHook> {
				return KEY
			}

			companion object {
				val KEY = object : CastingEnvironmentComponent.Key<ExtractMediaHook> {}
			}
		}
	}
}
