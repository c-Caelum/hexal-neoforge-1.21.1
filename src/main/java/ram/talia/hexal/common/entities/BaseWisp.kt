package ram.talia.hexal.common.entities

import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.asCompound
import at.petrak.hexcasting.api.utils.putCompound
import at.petrak.hexcasting.common.particles.ConjureParticleOptions
import net.minecraft.client.Minecraft
import net.minecraft.client.ParticleStatus
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializer
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Pose
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.Hexal
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.minus
import ram.talia.hexal.api.nextColour
import kotlin.math.*

abstract class BaseWisp(entityType: EntityType<out BaseWisp>, world: Level, pigment : FrozenPigment) : Entity(entityType, world), IMediaEntity<BaseWisp> {
	@Suppress("LeakingThis")
	var oldPos: Vec3 = position()

	override var media: Long
		get() = entityData.get(MEDIA)
		set(value) = entityData.set(MEDIA, max(value, 0))

	override val isConsumable = true

	override fun get() = this

	fun pigment(): FrozenPigment = entityData.get(PIGMENT);

	init {
		entityData.set(PIGMENT, pigment);
	}

	fun getEyeHeight(pose: Pose, dim: EntityDimensions) = 0f

	override fun makeBoundingBox(): AABB {
		return super.makeBoundingBox().move(0.0, -getDimensions(Pose.STANDING).height*0.5, 0.0)
	}

	/**
	 * Set the look vector of the wisp equal a given vector
	 */
	fun setLookVector(vel: Vec3) {
		val direction = vel.normalize()

		val pitch = asin(direction.y)
		val yaw = asin(max(min(direction.x / cos(pitch), 1.0), -1.0))

		xRot = (-pitch * 180 / Math.PI).toFloat()
		yRot = (-yaw * 180 / Math.PI).toFloat()
		xRotO = xRot
		yRotO = yRot
	}

	/**
	 * Returns the maximum length vector that the wisp can move (up to the length of [step], and along the line of [step]) before it collides with something.
	 */
	fun maxMove(step: Vec3): Vec3 {
		val bBox = this.boundingBox
		val voxelShapes = level().getEntityCollisions(this, bBox.expandTowards(deltaMovement))
		return if (step.lengthSqr() == 0.0) step else collideBoundingBox(this, step, bBox, level(), voxelShapes)
	}

	fun renderCentre(): Vec3 = position()
	//override fun renderCentre(other: ILinkable.IRenderCentre, recursioning: Boolean): Vec3 = renderCentre()

	fun playTrailParticles() {
		playTrailParticles(pigment())
	}

	protected open fun playWispParticles(pigment: FrozenPigment) {
		val radius = (media.toDouble() / MediaConstants.DUST_UNIT).pow(1.0 / 3) / 100

		val level = level();

		var configCount = 50;
		if (level is ClientLevel) {
			configCount = HexalConfig.Client.WISP_PARTICLE_COUNT.get();
		}

		val repeats = when (Minecraft.getInstance().options.particles().get() as ParticleStatus) {
			ParticleStatus.ALL -> configCount
			ParticleStatus.DECREASED -> (configCount.toDouble()*0.5).toInt()
			ParticleStatus.MINIMAL -> 0
		}

		for (i in 0..repeats) {
			val colour: Int = pigment.nextColour(random)

			level().addParticle(
				ConjureParticleOptions(colour),
				(renderCentre().x + radius*random.nextGaussian()),
				(renderCentre().y + radius*random.nextGaussian()),
				(renderCentre().z + radius*random.nextGaussian()),
				0.0125 * (random.nextDouble() - 0.5),
				0.0125 * (random.nextDouble() - 0.5),
				0.0125 * (random.nextDouble() - 0.5)
			)
		}
	}

	protected open fun playTrailParticles(pigment: FrozenPigment) {
		val radius = ceil((media.toDouble() / MediaConstants.DUST_UNIT).pow(1.0 / 3) / 10)

		val delta = oldPos - position()

		val coefficient = when (Minecraft.getInstance().options.particles().get() as ParticleStatus) {
			ParticleStatus.ALL -> HexalConfig.Client.WISP_PARTICLE_COUNT.get().toDouble()*0.25
			ParticleStatus.DECREASED -> (HexalConfig.Client.WISP_PARTICLE_COUNT.get().toDouble()*0.125)
			ParticleStatus.MINIMAL -> 0.0
		}

		val dist = delta.length() * coefficient;

		if (dist == 0.0) {
			return;
		}

		for (i in 0..<ceil(dist).toInt()) {
			val colour: Int = pigment.nextColour(random)

			val coeff = i / dist
			level().addParticle(
				ConjureParticleOptions(colour),
				(renderCentre().x + delta.x * coeff),
				(renderCentre().y + delta.y * coeff),
				(renderCentre().z + delta.z * coeff),
				0.0125 * (random.nextDouble() - 0.5),
				0.0125 * (random.nextDouble() - 0.5),
				0.0125 * (random.nextDouble() - 0.5)
			)
		}
	}

	fun setPigment(pigment: FrozenPigment): FrozenPigment {
		entityData.set(PIGMENT, pigment);
		return pigment;
	}

	override fun readAdditionalSaveData(compound: CompoundTag) {
		entityData.set(PIGMENT, FrozenPigment.CODEC.decode(NbtOps.INSTANCE, compound.getCompound(TAG_PIGMENT)).orThrow.first)

		media = compound.getLong(TAG_MEDIA)

		oldPos = position() // so that reloading a wisp doesn't result in it having a trail to the origin forever
	}

	override fun addAdditionalSaveData(compound: CompoundTag) {
		val res = FrozenPigment.CODEC.encodeStart(NbtOps.INSTANCE, entityData.get(PIGMENT))
			.getOrThrow { IllegalArgumentException("Expected a serializable pigment; got none") };

		compound.put(TAG_PIGMENT, res.asCompound)
		compound.putLong(TAG_MEDIA, media)
	}

	override fun defineSynchedData(builder: SynchedEntityData.Builder) {
		builder.define(PIGMENT, FrozenPigment.DEFAULT.get());
		builder.define(MEDIA, 0);
	}

	companion object {
		val stuff : Unit = Hexal.LOGGER.info("media:")
		@JvmStatic
		val PIGMENT: EntityDataAccessor<FrozenPigment> = SynchedEntityData.defineId(BaseWisp::class.java,
			Hexal.PIGMENT_SERIALIZER)
		@JvmStatic
		val MEDIA: EntityDataAccessor<Long> = SynchedEntityData.defineId(BaseWisp::class.java, EntityDataSerializers.LONG)

		const val TAG_PIGMENT = "pigment"
		const val TAG_MEDIA = "media"
	}
}