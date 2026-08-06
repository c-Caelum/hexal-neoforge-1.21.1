package ram.talia.hexal.api.mediafieditems

import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.core.component.PatchedDataComponentMap
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import ram.talia.hexal.Hexal
import ram.talia.hexal.api.addBounded
import ram.talia.hexal.api.config.HexalConfig
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * Much of the structure of this class comes from AE2's [AEItemKey](https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/9ff272a869508125daf5727746c9d9b8b00248bd/src/main/java/appeng/api/stacks/AEItemKey.java).
 */
data class ItemRecord(var item: Item, var components : PatchedDataComponentMap, var count: Long) {
    constructor(stack: ItemStack) :
            this(stack.item, PatchedDataComponentMap.fromPatch(stack.components, stack.componentsPatch), stack.count.toLong())
    constructor(item : Item, components : DataComponentPatch, count : Long):
            this(item, PatchedDataComponentMap.fromPatch(item.components(), components), count)

    fun typeMatches(other: ItemRecord): Boolean {
        // patch job n+1
        return item == other.item && Objects.equals(components.asPatch(), other.components.asPatch());
    }

    fun typeMatches(other: ItemStack): Boolean {
        // a rather concerning and annoying implementation, but oh well!
        return item == other.item && Objects.equals(other.componentsPatch, this.components.asPatch());
    }

    fun addCount(toAdd: Long) {
        count = count.addBounded(toAdd);
    }

    /**
     * Absorb the contents of another [ItemRecord] that matches this one,
     * increasing this record's count by the other's.
     */
    fun absorb(other: ItemRecord): Boolean {
        if (!typeMatches(other))
            return false

        // protection against overflow errors (really shouldn't happen but ya know why not
        val oldCount = count
        addCount(other.count)
        other.addCount(oldCount - count) // reduce the other's count by the amount moved to this' count.

        return true
    }

    fun absorb(other: ItemStack): Int {
        if (!typeMatches(other))
            return other.count

        // protection against overflow errors (really shouldn't happen but ya know why not)
        val oldCount = count
        addCount(other.count.toLong())
        return other.count - (count - oldCount).toInt()
    }

    fun split(amount: Long): ItemRecord {
        val splittee = ItemRecord(item, components.copy(), min(count, amount))
        count -= splittee.count
        return splittee
    }

    fun getDisplayName(): Component {
        //val itemStack = ItemStack(item)
        val custom : Component? = components.get(DataComponents.CUSTOM_NAME)
        val name = components.get(DataComponents.ITEM_NAME);
        return (custom ?: name) ?: item.description;
    }

    fun toStack(): ItemStack = toStack(1)

    fun toStack(count: Int): ItemStack {
        if (count <= 0) {
            return ItemStack.EMPTY
        }

        val result = ItemStack(item)
        result.applyComponents(components)
        result.count = count
        return result
    }

    fun maxStackSize(): Long {
        return components.getOrDefault(DataComponents.MAX_STACK_SIZE, item.defaultMaxStackSize).toLong();
    }

    fun addDrops(amount: Long, drops: MutableList<ItemStack>) {
        var leftToTake = min(amount, count)

        while (leftToTake > 0) {
            if (drops.size > HexalConfig.Server.getMaxItemsReturned()/64) {
                Hexal.LOGGER.warn("Tried dropping an excessive amount of items, ignoring $leftToTake $item")
                break
            }
            val taken = min(leftToTake, maxStackSize())
            leftToTake -= taken
            drops.add(toStack(taken.toInt()));
        }

        // subtracts the amount taken from the remaining.
        // unless it attempted to make more than 1000 stacks,
        // this should simplify to count - amount
        // the max(..., 0) is to make sure that if the original
        // amount was greater than count it doesn't result in
        // count being less than 0.
        count = max(count - amount + leftToTake, 0)
    }
}
