package ram.talia.hexal.common.casting.arithmetics.operator.mote

import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator.Companion.downcast
import at.petrak.hexcasting.api.casting.arithmetic.operator.OperatorUnary
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate.all
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate.ofType
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.item.ItemStack
import ram.talia.hexal.api.toIntCapped
import ram.talia.hexal.common.lib.hex.HexalIotaTypes.MOTE
import ram.talia.moreiotas.api.casting.iota.ItemStackIota

fun apply(iota: Iota): Iota {
    val mote = downcast(iota, MOTE)
    val stack = ItemStack(mote.item, mote.count.toIntCapped())
    stack.applyComponents(mote.components.asPatch());

    return ItemStackIota.createFiltered(stack)
}

// Talia, what???

object OperatorMoteExtractItem : OperatorUnary(all(ofType(MOTE)), ::apply)
