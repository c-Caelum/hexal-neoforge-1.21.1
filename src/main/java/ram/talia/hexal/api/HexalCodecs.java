package ram.talia.hexal.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import ram.talia.hexal.api.mediafieditems.ItemRecord;
import ram.talia.hexal.api.mediafieditems.MediafiedItemManager;

import java.util.UUID;

public class HexalCodecs {
    public static final Codec<Item> ITEM_CODEC = ItemStack.ITEM_NON_AIR_CODEC.xmap(Holder::value, BuiltInRegistries.ITEM::wrapAsHolder);
    public static final StreamCodec<RegistryFriendlyByteBuf, Item> ITEM_STREAM_CODEC =
            ByteBufCodecs.registry(Registries.ITEM);
    public static final Codec<ItemRecord> ITEM_RECORD_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                ITEM_CODEC.fieldOf("item").forGetter(ItemRecord::getItem),
                DataComponentPatch.CODEC.fieldOf("components").forGetter(a -> a.getComponents().asPatch()),
                Codec.LONG.fieldOf("count").forGetter(ItemRecord::getCount)
        ).apply(instance, ItemRecord::new)
    );
    // these are so easy to create I just might as well

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemRecord> ITEM_RECORD_STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, ItemRecord>() {
        /* layout:
        * Item
        * Components
        * Count
        */
        // this is so smooth :hearts: shoulda used the ItemStack one thoug.
        @Override
        public @NotNull ItemRecord decode(@NotNull RegistryFriendlyByteBuf buf) {
            Item item = ITEM_STREAM_CODEC.decode(buf);
            DataComponentPatch patch = DataComponentPatch.STREAM_CODEC.decode(buf);
            long count = ByteBufCodecs.VAR_LONG.decode(buf);
            return new ItemRecord(item, PatchedDataComponentMap.fromPatch(item.components(), patch), count);
        }
        // budda
        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull ItemRecord itemRecord) {
            ITEM_STREAM_CODEC.encode(buf, itemRecord.getItem());
            DataComponentPatch.STREAM_CODEC.encode(buf, itemRecord.getComponents().asPatch());
            ByteBufCodecs.VAR_LONG.encode(buf, itemRecord.getCount());
        }
    };

    public static final Codec<MediafiedItemManager.Index> INDEX_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.CODEC.fieldOf("storage").forGetter(MediafiedItemManager.Index::getStorage),
                    Codec.INT.fieldOf("index").forGetter(MediafiedItemManager.Index::getIndex)
            ).apply(instance, MediafiedItemManager.Index::new)
    );
    public static final StreamCodec<ByteBuf, MediafiedItemManager.Index> INDEX_STREAM_CODEC = new StreamCodec<ByteBuf, MediafiedItemManager.Index>() {
        @Override
        public MediafiedItemManager.Index decode(ByteBuf byteBuf) {
            UUID storage = UUIDUtil.STREAM_CODEC.decode(byteBuf);
            int index = ByteBufCodecs.INT.decode(byteBuf);

            return new MediafiedItemManager.Index(storage, index);
        }

        @Override
        public void encode(ByteBuf buf, MediafiedItemManager.Index index) {
            UUIDUtil.STREAM_CODEC.encode(buf, index.getStorage());
            ByteBufCodecs.INT.encode(buf, index.getIndex());
        }
    };
}
