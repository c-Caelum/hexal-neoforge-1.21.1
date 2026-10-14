@file:Suppress("unused")

package ram.talia.hexal.common.lib.hex

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.castables.OperationAction
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.casting.actions.selectors.OpGetEntitiesBy
import at.petrak.hexcasting.common.casting.actions.selectors.OpGetEntityAt
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.Hexal
import ram.talia.hexal.api.casting.wisp.triggers.WispTriggerTypes
import ram.talia.hexal.api.plus
import ram.talia.hexal.common.casting.actions.*
import ram.talia.hexal.common.casting.actions.spells.OpFallingBlock
import ram.talia.hexal.common.casting.actions.spells.OpFreeze
import ram.talia.hexal.common.casting.actions.spells.OpParticles
import ram.talia.hexal.common.casting.actions.spells.OpPlaceType
import ram.talia.hexal.common.casting.actions.spells.OpSmelt
import ram.talia.hexal.common.casting.actions.spells.gates.*
import ram.talia.hexal.common.casting.actions.spells.great.OpConsumeWisp
import ram.talia.hexal.common.casting.actions.spells.great.OpSeonWispSet
import ram.talia.hexal.common.casting.actions.spells.great.OpTick
import ram.talia.hexal.common.casting.actions.spells.motes.*
import ram.talia.hexal.common.casting.actions.spells.wisp.*
import ram.talia.hexal.common.entities.BaseWisp
import java.util.function.BiConsumer

object HexalActions {

	private val ACTIONS: MutableMap<ResourceLocation, ActionRegistryEntry> = mutableMapOf()

	@JvmStatic
	fun register(r: BiConsumer<ActionRegistryEntry, ResourceLocation>) {
		for ((key, value) in ACTIONS.entries) {
			r.accept(value, key)
		}
	}

	// ========================== Misc Info Gathering =================================
	@JvmField
	val CURRENT_TICK = make("current_tick", HexPattern.fromAngleString("ddwaa", HexDir.NORTH_WEST), OpCurrentTick)
	@JvmField
	val BREATH = make("breath", HexPattern.fromAngleString("aqawdwaqawd", HexDir.NORTH_WEST), OpGetBreath)
	@JvmField
	val HEALTH = make("health", HexPattern.fromAngleString("aqwawqa", HexDir.NORTH_WEST), OpGetHealth)
	@JvmField
	val ARMOUR = make("armour", HexPattern.fromAngleString("wqqqqw", HexDir.NORTH_WEST), OpGetArmour)
	@JvmField
	val TOUGHNESS = make("toughness", HexPattern.fromAngleString("aeqqqqea", HexDir.EAST), OpGetToughness)
	@JvmField
	val LIGHT_LEVEL = make("light_level", HexPattern.fromAngleString("qedqde", HexDir.NORTH_EAST), OpGetLightLevel)

	// =============================== Misc Maths =====================================
	@JvmField
	val FACTORIAL = make("factorial", HexPattern.fromAngleString("wawdedwaw", HexDir.SOUTH_EAST), OpFactorial)
	@JvmField
	val RUNNING_SUM = make("running/sum", HexPattern.fromAngleString("aea", HexDir.WEST), OpRunningOp ({ if (it is Vec3Iota) Vec3Iota(
		Vec3.ZERO) else DoubleIota(0.0) })
	{ running, iota ->
		when (running) {
			is DoubleIota -> DoubleIota(running.double + ((iota as? DoubleIota)?.double ?: throw OpRunningOp.InvalidIotaException("list.double")))
			is Vec3Iota -> Vec3Iota(running.vec3 + ((iota as? Vec3Iota)?.vec3 ?: throw OpRunningOp.InvalidIotaException("list.vec")))
			else -> throw OpRunningOp.InvalidIotaException("list.doublevec")
		}
	})
	@JvmField
	val RUNNING_MUL = make("running/mul", HexPattern.fromAngleString("qaawaaq", HexDir.NORTH_EAST), OpRunningOp ({ DoubleIota(1.0) })
	{ running, iota ->
		if (running !is DoubleIota)
			throw OpRunningOp.InvalidIotaException("list.double")
		DoubleIota(running.double * ((iota as? DoubleIota)?.double ?: throw OpRunningOp.InvalidIotaException("list.double")))
	})
	/*
	// ================================ Everbook ======================================
	@JvmField
	val EVERBOOK_READ = make("everbook/read", HexPattern.fromAngleString("eweeewedqdeddw", HexDir.NORTH_EAST), OpEverbookRead)
	@JvmField
	val EVERBOOK_WRITE = make("everbook/write", HexPattern.fromAngleString("qwqqqwqaeaqaaw", HexDir.SOUTH_EAST), OpEverbookWrite)
	@JvmField
	val EVERBOOK_DELETE = make("everbook/delete", HexPattern.fromAngleString("qwqqqwqaww", HexDir.SOUTH_EAST), OpEverbookDelete)
	@JvmField
	val EVERBOOK_TOGGLE_MACRO = make("everbook/toggle_macro", HexPattern.fromAngleString("eweeewedww", HexDir.SOUTH_WEST), OpToggleMacro)
	*/
	// ============================== Misc Spells =====================================
	@JvmField
	val SMELT = make("smelt", HexPattern.fromAngleString("wqqqwqqadad", HexDir.EAST), OpSmelt)
	@JvmField
	val FREEZE = make("freeze", HexPattern.fromAngleString("weeeweedada", HexDir.WEST), OpFreeze)
	@JvmField
	val FALLING_BLOCK = make("falling_block", HexPattern.fromAngleString("wqwawqwqwqwqwqw", HexDir.EAST), OpFallingBlock)
	@JvmField
	val PLACE_TYPE = make("place_type", HexPattern.fromAngleString("eeeeedeeeee", HexDir.WEST), OpPlaceType)
	@JvmField
	val PARTICLES = make("particles", HexPattern.fromAngleString("eqqqqa", HexDir.NORTH_EAST), OpParticles)

	// =============================== Wisp Stuff =====================================
	@JvmField
	val WISP_SUMMON_PROJECTILE = make("wisp/summon/projectile", HexPattern.fromAngleString("aqaeqeeeee", HexDir.NORTH_WEST), OpSummonWisp(false))
	@JvmField
	val WISP_SUMMON_TICKING = make("wisp/summon/ticking", HexPattern.fromAngleString("aqaweewaqawee", HexDir.NORTH_WEST),
        OpSummonWisp(true)
    )

	@JvmField
	val WISP_SELF = make("wisp/self", HexPattern.fromAngleString("dedwqqwdedwqqaw", HexDir.NORTH_EAST), OpWispSelf)
	@JvmField
	val WISP_MEDIA = make("wisp/media", HexPattern.fromAngleString("aqaweewaqaweedw", HexDir.NORTH_WEST), OpWispMedia)
	@JvmField
	val WISP_HEX = make("wisp/hex", HexPattern.fromAngleString("aweewaqaweewaawww", HexDir.SOUTH_EAST), OpWispHex)
	@JvmField
	val WISP_OWNER = make("wisp/owner", HexPattern.fromAngleString("dwqqwdedwqqwddwww", HexDir.SOUTH_WEST), OpWispOwner)

	// Set and Get Move Target
	@JvmField
	val WISP_MOVE_TARGET_SET = make("wisp/move/target/set", HexPattern.fromAngleString("awqwawqaw", HexDir.WEST), OpMoveTargetSet)
	@JvmField
	val WISP_MOVE_TARGET_GET = make("wisp/move/target/get", HexPattern.fromAngleString("ewdwewdew", HexDir.EAST), OpMoveTargetGet)
	@JvmField
	val WISP_MOVE_SPEED_SET = make("wisp/move/speed/set", HexPattern.fromAngleString("aeawqqqae", HexDir.WEST), OpMoveSpeedSet)
	@JvmField
	val WISP_MOVE_SPEED_GET = make("wisp/move/speed/get", HexPattern.fromAngleString("eeewdqdee", HexDir.EAST), OpMoveSpeedGet)

	// Allow and Disallow Media Transfer
	/*
	@JvmField
	val WISP_TRANSFER_ALLOW = make("wisp/transfer/allow", HexPattern.fromAngleString("qqqqqewwqeeeee", HexDir.NORTH_WEST), OpTransferAllowed(true))
	@JvmField
	val WISP_TRANSFER_DISALLOW = make("wisp/transfer/disallow", HexPattern.fromAngleString("qqqqqeqdeddweqqqqq", HexDir.NORTH_WEST), OpTransferAllowed(false))
	@JvmField
	val WISP_TRANSFER_OTHERS_ALLOW = make(
			"wisp/transfer/others/allow",
			HexPattern.fromAngleString("eeeeeqwweqqqqq", HexDir.SOUTH_WEST),
			OpTransferAllowedOthers(true))
	@JvmField
	val WISP_TRANSFER_OTHERS_DISALLOW = make(
			"wisp/transfer/others/disallow",
			HexPattern.fromAngleString("eeeeeqeaqaawqeeeee", HexDir.SOUTH_WEST),
			OpTransferAllowedOthers(false))
			*/

	// Entity purification and Zone distillations
	@JvmField
	val GET_ENTITY_WISP = make("get_entity/wisp",
			HexPattern.fromAngleString("qqwdedwqqdaqaaww", HexDir.SOUTH_EAST),
		OpGetEntityAt{ entity -> entity is BaseWisp })
	@JvmField
	val ZONE_ENTITY_WISP = make("zone_entity/wisp",
			HexPattern.fromAngleString("qqwdedwqqwdeddww", HexDir.SOUTH_EAST),
		OpGetEntitiesBy({ entity -> entity is BaseWisp }, false))
	@JvmField
	val ZONE_ENTITY_NOT_WISP = make("zone_entity/not_wisp",
			HexPattern.fromAngleString("eewaqaweewaqaaww", HexDir.NORTH_EAST),
			OpGetEntitiesBy({ entity -> entity is BaseWisp }, true))

	// Triggers
	@JvmField
	val WISP_TRIGGER_TICK = make("wisp/trigger/tick",
			HexPattern.fromAngleString("aqawded", HexDir.NORTH_WEST),
		    OpWispSetTrigger(WispTriggerTypes.TICK_TRIGGER_TYPE))
	@JvmField
	val WISP_TRIGGER_MOVE = make("wisp/trigger/move",
			HexPattern.fromAngleString("eqwawqwaqww", HexDir.EAST),
		    OpWispSetTrigger(WispTriggerTypes.MOVE_TRIGGER_TYPE))

	val WISP_SEON_GET = make("wisp/seon/get",
			HexPattern.fromAngleString("daqweewqaeaqweewqaqwwww", HexDir.EAST),
			OpSeonWispGet)
	/*

	// =============================== Link Stuff =====================================
	@JvmField
	val LINK = make("link", HexPattern.fromAngleString("eaqaaeqqqqqaweaqaaw", HexDir.EAST), OpLink)
	@JvmField
	val LINK_OTHERS = make("link/others", HexPattern.fromAngleString("eqqqqqawqeeeeedww", HexDir.EAST), OpLinkOthers)
	@JvmField
	val UNLINK = make("link/unlink", HexPattern.fromAngleString("qdeddqeeeeedwqdeddw", HexDir.WEST), OpUnlink)
	@JvmField
	val UNLINK_OTHERS = make("link/unlink/others", HexPattern.fromAngleString("qeeeeedweqqqqqaww", HexDir.WEST), OpUnlinkOthers)
	@JvmField
	val LINK_GET = make("link/get", HexPattern.fromAngleString("eqqqqqaww", HexDir.EAST), OpGetLinked)
	@JvmField
	val LINK_GET_INDEX = make("link/get_index", HexPattern.fromAngleString("aeqqqqqawwd", HexDir.SOUTH_WEST), OpGetLinkedIndex)
	@JvmField
	val LINK_NUM = make("link/num", HexPattern.fromAngleString("qeeeeedww", HexDir.WEST), OpNumLinked)
	@JvmField
	val LINK_COMM_SEND = make("link/comm/send", HexPattern.fromAngleString("qqqqqwdeddw", HexDir.NORTH_WEST), OpSendIota)
	@JvmField
	val LINK_COMM_READ = make("link/comm/read", HexPattern.fromAngleString("weeeeew", HexDir.NORTH_EAST), OpReadReceivedIota)
	@JvmField
	val LINK_COMM_NUM = make("link/comm/num", HexPattern.fromAngleString("aweeeeewaa", HexDir.SOUTH_EAST), OpNumReceivedIota)
	@JvmField
	val LINK_COMM_CLEAR = make("link/comm/clear", HexPattern.fromAngleString("aweeeeewa", HexDir.SOUTH_EAST), OpClearReceivedIotas)
	@JvmField
	val LINK_COMM_OPEN_TRANSMIT = make("link/comm/open_transmit", HexPattern.fromAngleString("qwdedwq", HexDir.WEST), OpOpenTransmit)
	@JvmField
	val LINK_COMM_CLOSE_TRANSMIT = make("link/comm/close_transmit", HexPattern.fromAngleString("ewaqawe", HexDir.EAST), OpCloseTransmit)
	*/
	// =============================== Gate Stuff =====================================
	@JvmField
	val GATE_MARK = make("gate/mark", HexPattern.fromAngleString("qaqeede", HexDir.WEST), OpMarkGate)
	@JvmField
	val GATE_UNMARK = make("gate/unmark", HexPattern.fromAngleString("edeqqaq", HexDir.EAST), OpUnmarkGate)
	@JvmField
	val GATE_MARK_GET = make("gate/mark/get", HexPattern.fromAngleString("edwwdeeede", HexDir.EAST), OpGetMarkedGate)
//	@JvmField
//	val GATE_MARK_NUM_GET = make("gate/mark/num/get", HexPattern.fromAngleString("qawwaqqqaq", HexDir.WEST), OpGetNumMarkedGate)
	@JvmField
	val GATE_CLOSE = make("gate/close", HexPattern.fromAngleString("qqqwwqqqwqqawdedw", HexDir.WEST), OpCloseGate)
	@JvmField
	val GATE_CLOSEABLE = make("gate/closeable", HexPattern.fromAngleString("qqqwwqqqwqaqdde", HexDir.WEST), OpCloseable)

	// =============================== Gate Stuff =====================================*/
	@JvmField
	val BIND_STORAGE = make("mote/storage/bind", HexPattern.fromAngleString("qaqwqaqwqaq", HexDir.NORTH_WEST), OpBindStorage(false))
	@JvmField
	val BIND_STORAGE_TEMP = make("mote/storage/bind/temp", HexPattern.fromAngleString("edewedewede", HexDir.NORTH_EAST), OpBindStorage(true))
	@JvmField
	val MOTE_CONTAINED_TYPE_GET = make("mote/contained_type/get", HexPattern.fromAngleString("dwqqqqqwddww", HexDir.NORTH_EAST), OpGetContainedItemTypes)
	@JvmField
	val MOTE_CONTAINED_GET = make("mote/contained/get", HexPattern.fromAngleString("aweeeeewaaww", HexDir.SOUTH_EAST), OpGetContainedMotes)
	@JvmField
	val MOTE_STORAGE_REMAINING_CAPACITY_GET = make("mote/storage/remaining_capacity/get", HexPattern.fromAngleString("awedqdewa", HexDir.SOUTH_EAST), OpGetStorageRemainingCapacity)
	@JvmField
	val MOTE_STORAGE_CONTAINS = make("mote/storage/contains", HexPattern.fromAngleString("dwqaeaqwd", HexDir.NORTH_EAST), OpStorageContains)
	@JvmField
	val MOTE_MAKE = make("mote/make", HexPattern.fromAngleString("eaqa", HexDir.WEST), OpMakeMote)
	@JvmField
	val MOTE_RETURN = make("mote/return", HexPattern.fromAngleString("qded", HexDir.EAST), OpReturnMote)
//	@JvmField
//	val MOTE_COUNT_GET = make("mote/count/get", HexPattern.fromAngleString("qqqqwqqqqqaa", HexDir.NORTH_WEST), OpGetCountMote)
//	@JvmField
//	val MOTE_COMBINE = make("mote/combine", HexPattern.fromAngleString("aqaeqded", HexDir.NORTH_WEST), OpCombineMotes)
	@JvmField
	val MOTE_COMBINABLE = make("mote/combinable", HexPattern.fromAngleString("dedqeaqa", HexDir.SOUTH_WEST), OpMotesCombinable)
	@JvmField
	val MOTE_SPLIT = make("mote/split", HexPattern.fromAngleString("eaqaaw", HexDir.EAST), OpSplitMote)
	@JvmField
	val MOTE_STORAGE_GET = make("mote/storage/get", HexPattern.fromAngleString("qqqqqaw", HexDir.SOUTH_WEST), OpGetMoteStorage)
	@JvmField
	val MOTE_STORAGE_SET = make("mote/storage/set", HexPattern.fromAngleString("eeeeedw", HexDir.SOUTH_EAST), OpSetMoteStorage)
	@JvmField
	val MOTE_CRAFT = make("mote/craft", HexPattern.fromAngleString("wwawdedwawdewwdwaqawdwwedwawdedwaww", HexDir.SOUTH_EAST), OpCraftMote)
	@JvmField
	val MOTE_CRAFT_PREVIEW = make("mote/craft/preview", HexPattern.fromAngleString("wwdwaqawdwaqwwawdedwawwqawdwaqawdww", HexDir.NORTH_EAST), OpCraftMotePreview)
	@JvmField
	val MOTE_VILLAGER_LEVEL_GET = make("mote/villager/level/get", HexPattern.fromAngleString("qqwdedwqqaww", HexDir.NORTH_WEST), OpGetVillagerLevel)
	@JvmField
	val MOTE_TRADE_GET = make("mote/trade/get", HexPattern.fromAngleString("awdedwaawwqded", HexDir.SOUTH_EAST), OpGetItemTrades)
	@JvmField
	val MOTE_TRADE = make("mote/trade", HexPattern.fromAngleString("awdedwaeqded", HexDir.NORTH_WEST), OpTradeMote)
	@JvmField
	val MOTE_USE_ON = make("mote/use_on", HexPattern.fromAngleString("qqqwqqqqaa", HexDir.EAST), OpUseMoteOn)

	// ============================== Great Stuff =====================================
	@JvmField
	val CONSUME_WISP = make("wisp/consume",
			HexPattern.fromAngleString("wawqwawwwewwwewwwawqwawwwewwwewdeaweewaqaweewaawwww", HexDir.NORTH_WEST),
			OpConsumeWisp)
	@JvmField
	val WISP_SEON_SET = make("wisp/seon/set",
			HexPattern.fromAngleString("aqweewqaeaqweewqaqwww", HexDir.SOUTH_WEST),
			OpSeonWispSet)
	@JvmField
	val TICK = make("tick", HexPattern.fromAngleString("wwwdwdwwwawqqeqwqqwqeqwqq", HexDir.SOUTH_EAST), OpTick)
	@JvmField
	val GATE_MAKE = make("gate/make", HexPattern.fromAngleString("qwqwqwqwqwqqeaeaeaeaeae", HexDir.WEST), OpMakeGate)



	fun make(name: String, pattern: HexPattern, action: Action): ActionRegistryEntry = make(name, ActionRegistryEntry(pattern, action))

	fun make(name: String, are: ActionRegistryEntry): ActionRegistryEntry {
		return if (ACTIONS.put(Hexal.modLoc(name), are) != null) {
			throw IllegalArgumentException("Typo? Duplicate id $name")
		} else {
			are
		}
	}

	fun make(name: String, oa: OperationAction): ActionRegistryEntry {
		val are = ActionRegistryEntry(oa.pattern, oa)
		return if (ACTIONS.put(Hexal.modLoc(name), are) != null) {
			throw IllegalArgumentException("Typo? Duplicate id $name")
		} else {
			are
		}
	}

}
