package ram.talia.hexal.api.gates;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class GateSavedData extends SavedData {
    public GateSavedData() {  }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        GateManager.writeToNbt(compoundTag);

        return compoundTag;
    }

    public GateSavedData(CompoundTag tag) {
        GateManager.readFromNbt(tag);
    }
}
