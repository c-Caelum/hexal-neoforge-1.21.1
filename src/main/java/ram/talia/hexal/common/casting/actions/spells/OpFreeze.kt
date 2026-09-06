package ram.talia.hexal.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.Vec3
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.common.lib.recipe.CopyProperties
import ram.talia.hexal.common.lib.recipe.FreezeRecipe

object OpFreeze : SpellAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val toFreeze = Vec3.atCenterOf(BlockPos.containing(args.getVec3(0, argc)))

        env.assertVecInRange(toFreeze)

        return SpellAction.Result(
            Spell(toFreeze),
            (HexalConfig.Server.FREEZE_COST.get() * MediaConstants.DUST_UNIT).toLong(),
            listOf(ParticleSpray.burst(toFreeze, 1.0))
        )
    }

    private data class Spell(val vec: Vec3) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val pos = BlockPos.containing(vec)
            val originalBlockState = env.world.getBlockState(pos)
            val fluidState = env.world.getFluidState(pos)

            if (!env.canEditBlockAt(pos) || !IXplatAbstractions.INSTANCE.isBreakingAllowed(
                    env.world,
                    pos,
                    originalBlockState,
                    env.castingEntity as Player?
                )
            )
                return

            if (fluidState.type == Fluids.WATER && originalBlockState.block is LiquidBlock) {
                env.world.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState())
                return
            }
            if (fluidState.type == Fluids.LAVA && originalBlockState.block is LiquidBlock) {
                env.world.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState())
                return
            }
            if (fluidState.type == Fluids.FLOWING_LAVA && originalBlockState.block is LiquidBlock) {
                env.world.setBlockAndUpdate(pos, Blocks.COBBLESTONE.defaultBlockState())
                return
            }

            val recipes = env.world.recipeManager.getAllRecipesFor(FreezeRecipe.TYPE)

            val recipe = recipes.find {
                it.value.from.test(originalBlockState)
            }

            if (recipe != null)
                env.world.setBlockAndUpdate(pos, CopyProperties.copyProperties(originalBlockState, recipe.value.to.displayed.first()))

        }
    }
}