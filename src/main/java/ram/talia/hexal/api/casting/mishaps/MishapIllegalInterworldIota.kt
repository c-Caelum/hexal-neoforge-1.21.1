package ram.talia.hexal.api.casting.mishaps

/*

class MishapIllegalInterworldIota(val iota: Iota) : Mishap() {
    override fun accentColor(env: CastingEnvironment, errorCtx: Context): FrozenPigment = dyeColor(DyeColor.GREEN)

    override fun errorMessage(env: CastingEnvironment, errorCtx: Context): Component = error("illegal_interworld_iota", iota.display())

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        env.caster?.let { it.health /= 2 } // Bad but better than freaking TODO()
    }

    companion object {
        private fun iotaTypeIsIllegal(iota: Iota): Boolean {
            val resourceKey = HexIotaTypes.REGISTRY.getKey(iota.type) ?: return false
            return isOfTag(HexIotaTypes.REGISTRY, resourceKey, HexalTags.ILLEGAL_INTERWORLD)
        }

        fun getFromNestedIota(iota: Iota): Iota? {
            val poolToSearch = ArrayDeque<Iota>()
            poolToSearch.addLast(iota)

            while (poolToSearch.isNotEmpty()) {
                val iotaToCheck = poolToSearch.removeFirst()
                if (iotaTypeIsIllegal(iotaToCheck))
                    return iotaToCheck
                iotaToCheck.subIotas()?.let { poolToSearch.addAll(it) }
            }

            return null
        }

        fun replaceInNestedIota(iota: Iota): Iota {
            return when {
                iotaTypeIsIllegal(iota) -> GarbageIota()
                iota is ListIota -> iota.list.map { replaceInNestedIota(it) }.asActionResult[0]
                else -> iota
            }
        }
    }
}
*/