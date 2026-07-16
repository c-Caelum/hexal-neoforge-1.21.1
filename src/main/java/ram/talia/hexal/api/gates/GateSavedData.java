package ram.talia.hexal.api.gates;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

public class GateSavedData extends SavedData {
    public GateSavedData() {  }
    public static final SavedData.Factory<GateSavedData> FACTORY = new SavedData.Factory<>(GateSavedData::new, (tag, provider) -> new GateSavedData(tag));


    @Override
    @ParametersAreNonnullByDefault
    public @NotNull CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        GateManager.writeToNbt(compoundTag);
        return compoundTag;
    }

    public static GateSavedData getServerState(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(ServerLevel.OVERWORLD);
        assert overworld != null;
        // I'm really sorry old players.
        GateSavedData data = overworld.getDataStorage().computeIfAbsent(FACTORY, "hexal_gate_data");
        data.setDirty();
        return data;
    }

    public GateSavedData(CompoundTag tag) {
        GateManager.readFromNbt(tag);
    }
}
