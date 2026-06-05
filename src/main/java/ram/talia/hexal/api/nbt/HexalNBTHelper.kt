package ram.talia.hexal.api.nbt

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.utils.asCompound
import at.petrak.hexcasting.api.utils.asInt
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.nbt.*
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.FastBufferedInputStream
import net.minecraft.world.entity.Entity
import ram.talia.hexal.Hexal
import ram.talia.hexal.common.lib.HexalEntities
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.zip.GZIPInputStream

fun ListTag.toIotaList(level: ServerLevel): MutableList<Iota> {
	val out = ArrayList<Iota>()
	for (patTag in this) {
		val tag = patTag.asCompound
		out.add(Hexal.deserializeIota(tag))
	}
	return out
}

@JvmName("toNbtListSpellDatum")
fun List<Iota>.toNbtList(): ListTag {
	val patsTag = ListTag()
	for (pat in this) {
		patsTag.add(Hexal.serializeIota(pat))
	}
	return patsTag
}

inline fun <reified T : Entity> ListTag.toEntityList(level: ServerLevel): MutableList<T> {
	val out = ArrayList<T>()

	for (eTag in this) {
		val uuid = NbtUtils.loadUUID(eTag)
		val entity = level.getEntity(uuid)

		if (entity != null && entity.isAlive && entity is T)
			out.add(entity)
	}

	return out
}

@JvmName("toNbtListEntity")
fun List<Entity>.toNbtList(): ListTag {
	val listTag = ListTag()

	for (entity in this) {
		listTag.add(NbtUtils.createUUID(entity.uuid))
	}

	return listTag
}

fun ListTag.toIntList(): MutableList<Int> {
	val out = mutableListOf<Int>()

	for (idTag in this) {
		out.add(idTag.asInt)
	}

	return out
}

@JvmName("toNbtListInt")

fun List<Int>.toNbtList(): ListTag {
	val listTag = ListTag()

	this.forEach { listTag.add(IntTag.valueOf(it)) }

	return listTag
}

@JvmName("toNbtListUUID")

fun List<UUID>.toNbtList(): ListTag {
	val listTag = ListTag()

	this.forEach { listTag.add(NbtUtils.createUUID(it)) }

	return listTag
}

fun ListTag.toUUIDList(): MutableList<UUID> {
	val uuids = mutableListOf<UUID>()

	this.forEach { uuids.add(NbtUtils.loadUUID(it)) }

	return uuids
}

@JvmName("toNbtListTag")
fun List<Tag>.toNbtList(): ListTag {
	val listTag = ListTag()

	this.forEach { listTag.add(it) }

	return listTag
}

fun ListTag.toCompoundTagList(): MutableList<CompoundTag> {
	val tags = mutableListOf<Tag>()
	tags.addAll(this)
	return tags.map { it as CompoundTag } as MutableList<CompoundTag>
}

fun CompoundTag.toCompressedBytes() : ByteArray {
	val out = ByteArrayOutputStream()
	NbtIo.writeCompressed(this, out)
	return out.toByteArray()
}

fun ByteArray.decompressToNBT(maxSize : Long) : CompoundTag {
	try {
		return ByteArrayInputStream(this).createDecompressorStream().use {
			NbtIo.read(it, SlightlyBetterNbtAccounter(maxSize))
		}
	} catch (exception : NbtSizeException){
		Hexal.LOGGER.info("Attempted to decompress byte array larger than $maxSize bytes.")
		throw exception
	} catch (exception : IOException){
		Hexal.LOGGER.error("Could not decompress byte array.", exception)
		throw exception
	}
}

class SlightlyBetterNbtAccounter(max: Long) : NbtAccounter(max, 7) {
	override fun accountBytes(added : Long){
		try {
			super.accountBytes(added)
		} catch (e : RuntimeException){
			throw NbtSizeException(e)
		}
	}
}

class NbtSizeException(cause : Throwable) : RuntimeException(cause)

fun InputStream.createDecompressorStream() : DataInputStream {
	return DataInputStream(FastBufferedInputStream(GZIPInputStream(this)))
}