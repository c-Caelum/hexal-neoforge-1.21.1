package ram.talia.hexal.common.lib;

import at.petrak.paucal.api.datagen.PaucalLootTableSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import ram.talia.hexal.Hexal;

import java.util.Map;

public class HexalLootTables extends PaucalLootTableSubProvider {
    public HexalLootTables() {
        super(Hexal.MODID);
    }

    public HexalLootTables(HolderLookup.Provider provider) {
        super(Hexal.MODID);
    }

    @Override
    protected void makeLootTables(Map<Block, LootTable.Builder> map, Map<ResourceKey<LootTable>, LootTable.Builder> map1) {
        dropSelf(map, HexalBlocks.MEDIAFIED_STORAGE);
    }
}
