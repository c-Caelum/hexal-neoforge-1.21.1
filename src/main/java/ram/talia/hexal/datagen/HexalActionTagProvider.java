package ram.talia.hexal.datagen;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.castables.Action;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.common.lib.HexRegistries;
import at.petrak.hexcasting.xplat.IXplatTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import ram.talia.hexal.Hexal;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

public class HexalActionTagProvider extends TagsProvider<ActionRegistryEntry> {
    public HexalActionTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, HexRegistries.ACTION, lookupProvider);
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void addTags(HolderLookup.Provider provider) {
        for (String entr : new String[]{"wisp/consume", "wisp/seon/set", "tick", "gate/make"}) {
            ResourceLocation loc = Hexal.modLoc(entr);
            ResourceKey<ActionRegistryEntry> key = ResourceKey.create(HexRegistries.ACTION, loc);
            tag(HexTags.Actions.PER_WORLD_PATTERN).add(key);
            tag(HexTags.Actions.CAN_START_ENLIGHTEN).add(key);
            tag(HexTags.Actions.REQUIRES_ENLIGHTENMENT).add(key);
        }
    }
}
