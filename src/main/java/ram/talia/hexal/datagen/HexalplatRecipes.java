package ram.talia.hexal.datagen;

import at.petrak.hexcasting.api.advancements.HexAdvancementTriggers;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.common.lib.HexStateIngredients;
import at.petrak.hexcasting.common.recipe.ingredient.brainsweep.VillagerIngredient;
import at.petrak.hexcasting.datagen.HexAdvancements;
import at.petrak.hexcasting.datagen.recipe.builders.BrainsweepRecipeBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Blocks;
import ram.talia.hexal.common.lib.HexalBlocks;
import ram.talia.hexal.common.lib.recipe.FreezeRecipe;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

import static ram.talia.hexal.Hexal.modLoc;

// yet another rewrite :p


public class HexalplatRecipes extends RecipeProvider {
    public HexalplatRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    /*                                UNPORTED                                */
    /* //Relays                                                               */
    /* ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, HexalItems.RELAY)  */
    /*       .define('C', HexItems.CHARGED_AMETHYST)                          */
    /*       .define('S', HexBlocks.SLATE_BLOCK)                              */
    /*       .define('A', Items.AMETHYST_BLOCK)                               */
    /*       .pattern(" C ")                                                  */
    /*       .pattern("SSS")                                                  */
    /*       .pattern("SAS")                                                  */
    /*       .unlockedBy("has_item", hasItem(HexTags.Items.STAVES))           */
    /*       .save(recipes)                                                   */
    /*                                                                        */
    /* //Overcast trigger, seems no longer used                               */
    /* val enlightenment = OvercastTrigger.Instance(                          */
    /*       ContextAwarePredicate.ANY,                                       */
    /*       MinMaxBounds.Ints.ANY,  // add a little bit of slop here         */
    /*       MinMaxBounds.Doubles.atLeast(0.8),                               */
    /*       MinMaxBounds.Doubles.between(0.1, 2.05)                          */
    /* )                                                                      */

    @Override
    @ParametersAreNonnullByDefault
    public void buildRecipes(RecipeOutput recipes) {

        new BrainsweepRecipeBuilder(HexStateIngredients.of(Blocks.SHULKER_BOX),
                new VillagerIngredient(VillagerProfession.CARTOGRAPHER, null, 4),
                HexalBlocks.MEDIAFIED_STORAGE.defaultBlockState(), MediaConstants.CRYSTAL_UNIT * 10)
                .unlockedBy("enlightenment", new Criterion<>(HexAdvancementTriggers.OVERCAST_TRIGGER.get(), HexAdvancements.ENLIGHTEN))
                .save(recipes, modLoc("brainsweep/mediafied_storage"));

        new FreezeRecipe(
                HexStateIngredients.of(Blocks.ICE),
                HexStateIngredients.of(Blocks.PACKED_ICE.defaultBlockState())
        ).save(recipes, modLoc("freeze/packed_ice"));
        new FreezeRecipe(
                HexStateIngredients.of(Blocks.PACKED_ICE),
                HexStateIngredients.of(Blocks.BLUE_ICE.defaultBlockState())
        ).save(recipes, modLoc("freeze/blue_ice"));
        new FreezeRecipe(
                HexStateIngredients.of(Blocks.WATER_CAULDRON),
                HexStateIngredients.of(Blocks.POWDER_SNOW_CAULDRON)
        ).save(recipes, modLoc("freeze/powder_snow_cauldron"));

    }
}
