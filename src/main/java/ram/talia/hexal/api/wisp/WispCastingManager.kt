package ram.talia.hexal.api.casting.wisp

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.eval.ExecutionClientView
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.IotaType.isTooLargeToSerialize
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.utils.TreeList
import at.petrak.hexcasting.api.utils.asCompound
import at.petrak.hexcasting.api.utils.putCompound
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.UUIDUtil
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import ram.talia.hexal.Hexal
import ram.talia.hexal.api.casting.eval.env.WispCastEnv
import ram.talia.hexal.api.casting.wisp.WispCastingManager.WispCast
import ram.talia.hexal.common.entities.BaseCastingWisp
import ram.talia.hexal.common.entities.TickingWisp
import java.util.*

class WispCastingManager(private val casterUUID: UUID, private var cachedServer: MinecraftServer?) {
	constructor(caster: ServerPlayer) : this(caster.uuid, caster.server) {
		cachedCaster = caster
	}

	private var cachedCaster: ServerPlayer? = null
	private val caster: ServerPlayer?
		get() {
			return if (cachedCaster != null && !cachedCaster!!.isRemoved && !cachedCaster!!.isDeadOrDying) {
				cachedCaster
			} else {
				cachedCaster = server?.playerList?.getPlayer(casterUUID)
				cachedCaster
			}
		}
	private val server: MinecraftServer?
		get() = cachedServer ?: cachedCaster?.server

	private val queue: PriorityQueue<WispCast> = PriorityQueue()

	/**
	 * Schedule a given cast to be added to this WispCastingManager's priority queue. It will be evaluated in the next tick unless the player is doing something that
	 * is producing a lot of Wisp casts. Higher [priority] casts will always be executed first - between casts of equal [priority], the first one added to the stack is
	 * preferred.
	 */
	fun scheduleCast(
			wisp: BaseCastingWisp,
			priority: Int,
			hex: ListIota,
			initialStack: TreeList<Iota>,
			initialRavenmind: Iota?,
	) {
		if (caster == null)
			return

		val cast = WispCast(wisp, priority, caster!!.level().gameTime, hex, initialStack, initialRavenmind)

		/*// if the wisp is one that is hard enough to forkbomb with (specifically, lasting wisps), let it go through without reaching the queue
		if (specialHandlers.any { handler -> handler.invoke(this, cast).also {
					if (it) { // if it should be let through, immediately cast it and execute the callback.
						this.cast(cast).callback()
					}
				} })
			return*/
		queue.add(cast)
	}

	/**
	 * Called by CCWispCastingManager (Fabric) and WispCastingManagerEventHandler (Forge) each tick, evaluates up to WISP_EVALS_PER_TICK Wisp casts.
	 */
	fun executeCasts() {
		if (caster == null || caster!!.tickCount <= 1) {
			return;
		}
		if (caster!!.level().isClientSide) {
			Hexal.LOGGER.error("HOW DID THIS HAPPEN")
			return
		}

		if (queue.size > 0) {
//			Hexal.LOGGER.info("player ${caster!!.uuid} is executing up to $WISP_EVALS_PER_TICK of ${queue.size} on tick ${caster!!.level().gameTime}")
		}

		var evalsLeft = WISP_EVALS_PER_TICK

		val itr = queue.iterator()

		val results = ArrayList<WispCastResult>()

		while (evalsLeft > 0 && itr.hasNext()) {
			val cast = itr.next()
			itr.remove()

			// if the wisp isn't chunkloaded at the moment, delete it from the queue (this is a small enough edge case I can't be bothered robustly handling it)
			val wisp = cast.wisp ?: caster!!.serverLevel().getEntity(cast.wispUUID) as? BaseCastingWisp ?: continue
			cast.wisp = wisp

			if (wisp.isRemoved)
				continue

			if (wisp.level().dimension() != caster?.level()?.dimension()) {
				wisp.castCallback(WispCastResult(wisp, false, TreeList.empty(), NullIota(), true))
				continue
			}

			results += cast(cast)

			evalsLeft--
		}

		results.forEach { result -> result.callback() }
	}

	/**
	 * Actually executes the cast described in [cast]. Will throw a NullPointerException if it somehow got here with [cast] == null.
	 */
	fun cast(cast: WispCast): WispCastResult {
		val wisp = cast.wisp!!
		wisp.summonedChildThisCast = false // restricts the wisp to only summoning one other wisp per cast.

		val ctx = WispCastEnv(
			wisp,
			wisp.level() as ServerLevel
		)

		val userData = CompoundTag()
        cast.initialRavenmind.let { userData.putCompound(HexAPI.RAVENMIND_USERDATA, Hexal.serializeIota(it).asCompound) }
		val image = CastingImage().copy(
			stack = cast.initialStack,
			userData = userData
		)

		val hex = cast.hex;

		val harness = CastingVM(image, ctx)

		val info : ExecutionClientView;

		if (hex is ListIota) {
			info = harness.queueExecuteAndWrapIotas(hex.subIotas()!!.toList(), wisp.level() as ServerLevel);
		} else {
			info = harness.queueExecuteAndWrapIota(hex, wisp.level() as ServerLevel)
		}

		// the wisp will have things it wants to do once the cast is successful, so a callback on it is called to let it know that happened, and what the end state of the
		// stack and ravenmind is. This is returned and added to a list that [executeCasts] will loop over to hopefully prevent concurrent modification problems.
		return WispCastResult(wisp, info.resolutionType.success, harness.image)
	}

	fun readFromNbt(tag: CompoundTag?, level: ServerLevel) {
		val list = tag?.get(TAG_CAST_LIST) as? ListTag ?: return

		for (castTag in list) {
			queue.add(WispCast.CODEC.decode(NbtOps.INSTANCE, castTag).orThrow.first)
		}
	}

	fun writeToNbt(tag: CompoundTag) {
		val list = ListTag()

		for (cast in queue) {
			list.add(WispCast.CODEC.encodeStart(NbtOps.INSTANCE, cast).orThrow)
		}

		tag.put(TAG_CAST_LIST, list)
	}

	data class WispCast(
		val wispUUID: UUID,
		val priority: Int,
		val timeAdded: Long,
		val hex: Iota,
		val initialStack: TreeList<Iota>,
		val initialRavenmind: Iota,
	) : Comparable<WispCast> {
		/**
		 * when loading from NBT, it calls ServerLevel.entity(UUID), which could return null.
		 */
		var wisp: BaseCastingWisp? = null

		constructor(
			wisp: BaseCastingWisp,
			priority: Int,
			timeAdded: Long,
			hex: ListIota,
			initialStack: TreeList<Iota>,
			initialRavenmind: Iota?
		) : this(wisp.uuid, priority, timeAdded, hex, initialStack, initialRavenmind ?: NullIota()) {
			this.wisp = wisp
		}

		override fun compareTo(other: WispCast): Int {
			if (priority != other.priority)
				return priority - other.priority
			return (timeAdded - other.timeAdded).toInt()
		}

		companion object {
			val CODEC : Codec<WispCast> = RecordCodecBuilder.create {
				it.group(
					UUIDUtil.CODEC.fieldOf(TAG_WISP).forGetter(WispCast::wispUUID),
					Codec.INT.fieldOf(TAG_PRIORITY).forGetter(WispCast::priority),
					Codec.LONG.fieldOf(TAG_TIME_ADDED).forGetter(WispCast::timeAdded),
					IotaType.TYPED_CODEC.fieldOf(TAG_HEX).forGetter(WispCast::hex),
				TreeList.codecOf(IotaType.TYPED_CODEC).fieldOf(TAG_INITIAL_STACK).forGetter(WispCast::initialStack),
					IotaType.TYPED_CODEC.fieldOf(TAG_INITIAL_RAVENMIND).forGetter(WispCast::initialRavenmind)
				).apply(it, ::WispCast)
			}
			const val TAG_WISP = "wisp"
			const val TAG_PRIORITY = "priority"
			const val TAG_TIME_ADDED = "time_added"
			const val TAG_HEX = "hex"
			const val TAG_INITIAL_STACK = "initial_stack"
			const val TAG_INITIAL_RAVENMIND = "initial_ravenmind"
		}
	}

	/**
	 * the result passed back to the Wisp after its cast is successfully executed.
	 */
	data class WispCastResult(val wisp: BaseCastingWisp, val succeeded: Boolean, val endStack: TreeList<Iota>, val endRavenmind: Iota, val cancelled: Boolean = false) {
		constructor(
			wisp: BaseCastingWisp,
			succeeded: Boolean,
			image: CastingImage,
			cancelled: Boolean = false,
		) : this(
			wisp = wisp,
			succeeded = succeeded,
			// TODO: Make this a mishap
			// Clear stack if it gets too large
			endStack = if (isTooLargeToSerialize(image.stack)) TreeList.empty() else image.stack,
			endRavenmind = Hexal.deserializeIota(image.userData.getCompound(HexAPI.RAVENMIND_USERDATA)),
			cancelled = cancelled,
		)

		fun callback() { wisp.castCallback(this) }
	}

	companion object {
		const val TAG_CAST_LIST = "cast_list"
		const val WISP_EVALS_PER_TICK = 10

		/**
		 * This is a list of pure methods that accept the casting manager and the WispCast, and if that WispCast should
		 * be executed immediately rather than added to the queue, returns true.
		 */
		var specialHandlers: MutableList<(WispCastingManager, WispCast) -> Boolean> = mutableListOf()

		init {
			// if a wisp is bound, it should skip the queue.
			specialHandlers.add { _, cast -> cast.wisp?.seon == true }
		}
	}
}
