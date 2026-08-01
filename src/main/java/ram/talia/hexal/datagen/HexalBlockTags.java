package ram.talia.hexal.datagen;

import at.petrak.hexcasting.xplat.IXplatTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import ram.talia.hexal.common.lib.HexalBlocks;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

public class HexalBlockTags extends TagsProvider<Block> {

    public HexalBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.BLOCK, lookupProvider);
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void addTags(HolderLookup.Provider provider) {
        add(tag(BlockTags.MINEABLE_WITH_PICKAXE), HexalBlocks.MEDIAFIED_STORAGE);
    }

    void add(TagsProvider.TagAppender<Block> appender, Block... blocks) {
        for (Block block : blocks) {
            appender.add(BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow());
        }
    }
}
