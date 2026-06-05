package ram.talia.hexal.common.casting.actions.spells.gates

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import ram.talia.hexal.api.getGate

object OpCloseable : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val gate = args.getGate(0, argc)

        if (gate.isDrifting) {
            // always true for drifting gates since they don't even have a destination to check
            return true.asActionResult
        } else {
            // check if destination is null (unloaded/missing entity anchor) or outside of world
            // subtract 0,1,0 because teleporting into bedrock floor is also invalid
            val targetPos = gate.getTargetPos(env.world) ?: return false.asActionResult
            return env.isVecInWorld(targetPos.subtract(0.0, 1.0, 0.0)).asActionResult
        }
    }
}