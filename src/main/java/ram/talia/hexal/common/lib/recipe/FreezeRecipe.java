package ram.talia.hexal.common.lib.recipe;

import at.petrak.hexcasting.common.lib.HexStateIngredients;
import at.petrak.hexcasting.common.recipe.RecipeSerializerBase;
import at.petrak.hexcasting.common.recipe.ingredient.state.StateIngredient;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import ram.talia.hexal.Hexal;

public class FreezeRecipe implements Recipe<SingleRecipeInput> {

    public static final ResourceLocation FREEZE_RECIPE_TYPE_LOCATION = Hexal.modLoc("freeze");
    public static final RecipeType<FreezeRecipe> TYPE = RecipeType.simple(FREEZE_RECIPE_TYPE_LOCATION);
    @Override public @NotNull RecipeType<?> getType() {
        return TYPE;
    }

    //Using HexCasting's StateIngredient not to reinvent the wheel, although a round enough stone would be enough.
    public final StateIngredient from;
    public final StateIngredient to;

    public FreezeRecipe(StateIngredient from, StateIngredient to) {
        this.from = from;
        this.to   = to;
    }


    //Serialization area

    @Override public @NotNull RecipeSerializer<FreezeRecipe> getSerializer() { return SERIALIZER; }
    public static final RecipeSerializer<FreezeRecipe> SERIALIZER = new RecipeSerializerBase<>() {

        @Override public @NotNull MapCodec<FreezeRecipe> codec() { return CODEC; }
        public static final MapCodec<FreezeRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                HexStateIngredients.TYPED_CODEC
                        .fieldOf("ingredient")
                        .forGetter(freezeRecipe -> freezeRecipe.from),
                HexStateIngredients.TYPED_CODEC
                        .fieldOf("result")
                        .forGetter(freezeRecipe -> freezeRecipe.to)
        ).apply(inst, FreezeRecipe::new));

        @Override public @NotNull StreamCodec<RegistryFriendlyByteBuf, FreezeRecipe> streamCodec() { return STREAM_CODEC; }
        public static final StreamCodec<RegistryFriendlyByteBuf, FreezeRecipe> STREAM_CODEC = StreamCodec.composite(
                HexStateIngredients.TYPED_STREAM_CODEC, recipe -> recipe.from,
                HexStateIngredients.TYPED_STREAM_CODEC, recipe -> recipe.to,
                FreezeRecipe::new
        );
    };


    //Miscellaneous

    @Override
    public boolean matches(SingleRecipeInput recipeInput, @NotNull Level level) {
        return from.getDisplayedStacks().contains(recipeInput.item());
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull SingleRecipeInput recipeInput, HolderLookup.@NotNull Provider provider) {
        return getResultItem(provider).copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider provider) {
        return to.getDisplayedStacks().getFirst();
    }

    //Simple datagen
    public void save(RecipeOutput recipeOutput, ResourceLocation resourceLocation) {
        recipeOutput.accept(resourceLocation, this, null);
    }
}
