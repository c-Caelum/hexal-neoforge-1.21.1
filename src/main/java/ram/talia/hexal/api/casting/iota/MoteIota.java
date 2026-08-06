package ram.talia.hexal.api.casting.iota;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ram.talia.hexal.api.HexalCodecs;
import ram.talia.hexal.api.mediafieditems.ItemRecord;
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager;
import ram.talia.hexal.common.lib.hex.HexalIotaTypes;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Similar to GateIotas, stores a reference to an item stored in the
 * media. When the item is used up, all references to it become null.
 */
public class MoteIota extends Iota {
    MediafiedItemManager.Index index;
    Component displayMessage = Component.empty();
    long count = 0;


    /**
     * Used to get the UUID of the temporarily bound storage from userData, if one exists.
     */
    public static final String TAG_TEMP_STORAGE = "hexal:temp_storage";

    public MoteIota(MediafiedItemManager.Index index) {
        super(() -> HexalIotaTypes.MOTE);
        WeakReference<ItemRecord> record = MediafiedItemManager.getRecord(index);
        ItemRecord rec;
        if (record != null && (rec = record.get()) != null) {
            displayMessage = rec.getDisplayName();
            count = rec.getCount();
        }
        this.index = index;
    }

    public MoteIota(MediafiedItemManager.Index index, Component displayMessage, long count) {
        super(() -> HexalIotaTypes.MOTE);
        this.index = index;
        this.displayMessage = displayMessage;
        this.count = count;
    }

    public static @Nullable MoteIota makeIfStorageLoaded(ItemStack stack, UUID storageUUID) {
        var index = MediafiedItemManager.assignItem(stack, storageUUID);

        if (index != null)
            return new MoteIota(index);
        else
            return null;
    }

    public static @Nullable MoteIota makeIfStorageLoaded(ItemRecord record, UUID storageUUID) {
        var index = MediafiedItemManager.assignItem(record, storageUUID);

        if (index != null)
            return new MoteIota(index);
        else
            return null;
    }

    /**
     * Returns the MoteIota if its item still exists, or null otherwise. SHOULD ALWAYS
     * BE CALLED BEFORE MAKING USE OF AN ITEM IOTA {@literal (built into List<Iota>.getItem)}.
     */
    public @Nullable MoteIota selfOrNull() {
        if (MediafiedItemManager.contains(index))
            return this;
        return null;
    }

    public boolean isEmpty() {
        return MediafiedItemManager.isEmpty(index);
    }

    public MediafiedItemManager.Index getItemIndex() {
        return index;
    }

    public @Nullable ItemRecord getRecord() {
        var record = MediafiedItemManager.getRecord(index);
        if (record == null)
            return null;
        return record.get();
    }

    public Item getItem() {
        return Objects.requireNonNull(MediafiedItemManager.getItem(index), "MediafiedItemManager returned null for Item that has existing MoteIota.");
    }

    public PatchedDataComponentMap getComponents() {
        return MediafiedItemManager.getComponents(index);
    }

    public void setComponents(PatchedDataComponentMap components) {
        MediafiedItemManager.setComponents(index, components);
    }

    public long getCount() {
        return Objects.requireNonNull(MediafiedItemManager.getCount(index), "MediafiedItemManager returned null for Item that has existing MoteIota.");
    }

    public void absorb(MoteIota other) {
        MediafiedItemManager.merge(index, other.getItemIndex());
    }

    public int absorb(ItemStack other) {
        return MediafiedItemManager.merge(index, other);
    }

    public boolean typeMatches(MoteIota other) {
        return MediafiedItemManager.typeMatches(index, other.getItemIndex());
    }

    public boolean typeMatches(ItemStack other) {
        return MediafiedItemManager.typeMatches(index, other);
    }

    public @Nullable MoteIota splitOff(long amount, @Nullable UUID storage) {
        var newIndex = MediafiedItemManager.splitOff(index, amount, storage);
        if (newIndex == null)
            return null;
        return new MoteIota(newIndex);
    }

    public List<ItemStack> getStacksToDrop(int count) {
        return MediafiedItemManager.getStacksToDrop(index, count);
    }

    public long removeItems(int count) {
        return removeItems((long) count);
    }

    public long removeItems(long count) {
        return MediafiedItemManager.removeItems(index, count);
    }

    /**
     * Takes a template ItemStack and sets the item and tag of the referenced ItemRecord to that item and tag, while leaving the count the same.
     */
    public void templateOff(@NotNull ItemStack template) {
        MediafiedItemManager.templateOff(index, template, null);
    }

    /**
     * Takes a template ItemStack and sets the item and tag of the referenced ItemRecord to that item and tag, as well as overriding the count to newCount.
     */
    public void templateOff(@NotNull ItemStack template, long newCount) {
        MediafiedItemManager.templateOff(index, template, newCount);
    }

    public MoteIota copy() {
        return new MoteIota(index);
    }

    public @Nullable MoteIota setStorage(@NotNull UUID uuid) {
        var storageFull = MediafiedItemManager.isStorageFull(uuid);
        if (storageFull == null || storageFull) // isStorageFull can return null
            return null;

        var record = getRecord();
        MediafiedItemManager.removeRecord(index);
        if (record == null)
            return null;

        var newIndex = MediafiedItemManager.assignItem(record, uuid);
        return new MoteIota(newIndex);
    }

    @Override
    protected boolean toleratesOther(Iota that) {
        return (typesMatch(this, that) &&
                that instanceof MoteIota ithat &&
                index.equals(ithat.getItemIndex())) ||
                (this.isEmpty() && (that instanceof NullIota ||
                        (that instanceof MoteIota ithat2 &&
                                ithat2.isEmpty())));
    }

    @Override
    public Component display() {
        if (displayMessage == Component.empty() || count == 0) {
            return Component.translatable("hexcasting.iota.hexcasting:null").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
        }

        return Component.translatable("hexal.spelldata.mote", displayMessage, count).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public int hashCode() {
        return Objects.hash(MediafiedItemManager.getItem(index), MediafiedItemManager.getCount(index), MediafiedItemManager.getComponents(index));
    }

    @Override
    public boolean isTruthy() {
        return !this.isEmpty();
    }

    public static IotaType<MoteIota> TYPE = new IotaType<>() {
        public static final MapCodec<MoteIota> CODEC = HexalCodecs.INDEX_CODEC.xmap(MoteIota::new, MoteIota::getItemIndex).fieldOf("moteIndex");
        public static final StreamCodec<RegistryFriendlyByteBuf, MoteIota> STREAM_CODEC = StreamCodec.composite(
                HexalCodecs.INDEX_STREAM_CODEC, a -> a.index, ComponentSerialization.STREAM_CODEC, a -> a.displayMessage, ByteBufCodecs.VAR_LONG, a -> a.count,
                MoteIota::new);

        @Override
        public boolean validate(MoteIota iota, ServerLevel level) {
            WeakReference<ItemRecord> rec = MediafiedItemManager.getRecord(iota.index);
            ItemRecord item;
            if (rec != null && (item = rec.get()) != null) {
                iota.count = item.getCount();
                iota.displayMessage = item.getDisplayName();
            }

            return rec != null && rec.get() != null && rec.get().getCount() != 0;
        }

        @Override
        public MapCodec<MoteIota> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MoteIota> streamCodec() {
            return STREAM_CODEC;
        }

        @Override
        public int color() {
            return 0xff_ffff55;
        }
    };
}
