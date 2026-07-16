package ram.talia.hexal.datagen;

import at.petrak.hexcasting.api.advancements.HexAdvancementTriggers;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.common.lib.HexStateIngredients;
import at.petrak.hexcasting.common.recipe.BrainsweepRecipe;
import at.petrak.hexcasting.common.recipe.ingredient.brainsweep.VillagerIngredient;
import at.petrak.hexcasting.datagen.HexAdvancements;
import at.petrak.hexcasting.datagen.recipe.builders.BrainsweepRecipeBuilder;
import at.petrak.hexcasting.interop.patchouli.BrainsweepProcessor;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Blocks;
import static ram.talia.hexal.Hexal.modLoc;

import org.jetbrains.annotations.NotNull;
import ram.talia.hexal.common.lib.HexalBlocks;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

// yet another rewrite :p


public class HexalplatRecipes extends RecipeProvider {
    public HexalplatRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    		/*ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, HexalItems.RELAY)
			.define('C', HexItems.CHARGED_AMETHYST)
			.define('S', HexBlocks.SLATE_BLOCK)
			.define('A', Items.AMETHYST_BLOCK)
			.pattern(" C ")
			.pattern("SSS")
			.pattern("SAS")
			.unlockedBy("has_item", hasItem(HexTags.Items.STAVES))
			.save(recipes)*/

		/*FreezeRecipeBuilder(StateIngredientHelper.of(Blocks.ICE), Blocks.PACKED_ICE.defaultBlockState())
			.unlockedBy("has_item", hasItem(HexTags.Items.STAVES))
			.save(recipes, modLoc("freeze/packed_ice"))
		FreezeRecipeBuilder(StateIngredientHelper.of(Blocks.PACKED_ICE), Blocks.BLUE_ICE.defaultBlockState())
			.unlockedBy("has_item", hasItem(HexTags.Items.STAVES))
			.save(recipes, modLoc("freeze/blue_ice"))
		FreezeRecipeBuilder(StateIngredientHelper.of(Blocks.WATER_CAULDRON), Blocks.POWDER_SNOW_CAULDRON.defaultBlockState())
			.unlockedBy("has_item", hasItem(HexTags.Items.STAVES))
			.save(recipes, modLoc("freeze/powder_snow_cauldron"))

		val enlightenment = OvercastTrigger.Instance(
			ContextAwarePredicate.ANY,
			MinMaxBounds.Ints.ANY,  // add a little bit of slop here
			MinMaxBounds.Doubles.atLeast(0.8),
			MinMaxBounds.Doubles.between(0.1, 2.05)
		)

		BrainsweepRecipeBuilder(StateIngredientHelper.of(BlockTags.SHULKER_BOXES),
			VillagerIngredient(VillagerProfession.CARTOGRAPHER, null, 2),
			HexalBlocks.MEDIAFIED_STORAGE.defaultBlockState(),
			MediaConstants.CRYSTAL_UNIT * 10)
			.unlockedBy("enlightenment", enlightenment)
			.save(recipes, modLoc("brainsweep/mediafied_storage"),)*/

    @Override
    @ParametersAreNonnullByDefault
    public void buildRecipes(RecipeOutput recipes) {
        new BrainsweepRecipeBuilder(HexStateIngredients.of(Blocks.SHULKER_BOX),
                new VillagerIngredient(VillagerProfession.CARTOGRAPHER, null, 4),
                HexalBlocks.MEDIAFIED_STORAGE.defaultBlockState(), MediaConstants.CRYSTAL_UNIT*10)
                .unlockedBy("enlightenment", new Criterion<>(HexAdvancementTriggers.OVERCAST_TRIGGER, HexAdvancements.ENLIGHTEN))
                .save(recipes, modLoc("brainsweep/mediafied_storage"));
    }
}
