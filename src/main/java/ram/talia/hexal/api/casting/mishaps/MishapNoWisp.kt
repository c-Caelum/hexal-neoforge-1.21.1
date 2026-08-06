package ram.talia.hexal.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.TreeList
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantments

class MishapNoWisp : Mishap() {
	override fun accentColor(env: CastingEnvironment, errorCtx: Context): FrozenPigment = dyeColor(DyeColor.LIGHT_BLUE)

	override fun errorMessage(env: CastingEnvironment, errorCtx: Context): Component = error("no_wisp")

	private inline fun dropAll(player: Player, stacks: MutableList<ItemStack>, filter: (ItemStack) -> Boolean = { true }) {
		for (index in stacks.indices) {
			val item = stacks[index]
			if (!item.isEmpty && filter(item)) {
				player.drop(item, true, false)
				stacks[index] = ItemStack.EMPTY
			}
		}
	}

	override fun execute(env: CastingEnvironment, errorCtx: Context, stack: TreeList<Iota>): TreeList<Iota> {
		if (env !is PlayerBasedCastEnv)
			return stack;
		val caster = env.caster ?: return stack
		dropAll(caster, caster.inventory.items)
		dropAll(caster, caster.inventory.offhand)
		dropAll(caster, caster.inventory.armor) {
			it.get(DataComponents.ENCHANTMENTS)?.keySet()?.any { e -> e.`is`(Enchantments.BINDING_CURSE) } != true
		}
		return stack;
	}
}