package ram.talia.hexal.api.casting.iota;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import kotlin.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ram.talia.hexal.api.gates.GateManager;
import ram.talia.hexal.common.lib.hex.HexalIotaTypes;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class GateIota extends Iota {
    public static String TAG_INDEX = "index";
    public static String TAG_TARGET_TYPE = "target_type";
    public static String TAG_TARGET_POSITION = "target_position";
    public static String TAG_TARGET_UUID = "target_uuid";
    public static String TAG_TARGET_NAME = "target_name";
    public static String TAG_TARGET = "target";

    public record EntityAnchor(@Nullable UUID uuid, @Nullable String name, @NotNull Vec3 offset) {
        public static final Codec<EntityAnchor> CODEC = RecordCodecBuilder.create( gateIota -> gateIota.group(
                UUIDUtil.CODEC.fieldOf(TAG_TARGET_UUID).forGetter(EntityAnchor::uuid),
                Codec.STRING.fieldOf(TAG_TARGET_NAME).forGetter(EntityAnchor::name),
                Vec3.CODEC.fieldOf(TAG_TARGET_POSITION).forGetter(EntityAnchor::offset)
                ).apply(gateIota, EntityAnchor::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, EntityAnchor> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, EntityAnchor>() {
            /* Layout:
            type (if this is true, then it's entity anchored, meaning uuid/name exist)
            uuid,
            name,
            pos
             */
            @Override
            @ParametersAreNonnullByDefault
            public @NotNull EntityAnchor decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
                boolean bool = registryFriendlyByteBuf.readBoolean();
                UUID uuid = null;
                String name = null;
                if (bool) {
                    uuid = registryFriendlyByteBuf.readUUID();
                    name = ByteBufCodecs.STRING_UTF8.decode(registryFriendlyByteBuf);
                }
                Vec3 pos = registryFriendlyByteBuf.readVec3();
                return new EntityAnchor(uuid, name, pos);
            }
            @Override
            @ParametersAreNonnullByDefault
            public void encode(RegistryFriendlyByteBuf buf, EntityAnchor entityAnchor) {
                boolean isEntityAnchored = entityAnchor.uuid != null;
                if (isEntityAnchored && entityAnchor.name != null) {
                    buf.writeUUID(entityAnchor.uuid);
                    ByteBufCodecs.STRING_UTF8.encode(buf, entityAnchor.name);
                }
                buf.writeVec3(entityAnchor.offset);
            }
        };
    }

    private record Payload(int index, Either<Vec3, EntityAnchor> target) {
        public static final Codec<Payload> CODEC = RecordCodecBuilder.create( payloadInstance -> payloadInstance.group(
                Codec.INT.fieldOf(TAG_INDEX).forGetter(Payload::index),
                Codec.either(Vec3.CODEC, EntityAnchor.CODEC).fieldOf(TAG_TARGET).forGetter(Payload::target)
            ).apply(payloadInstance, Payload::new)
        );
    }

    public final Payload target;

    public GateIota(int index, @Nullable Either<Vec3, Pair<Entity, Vec3>> target) {
        super(() -> HexalIotaTypes.GATE);
        this.target = new Payload(index, target == null ? null : target.mapRight(pair -> new EntityAnchor(pair.getFirst().getUUID(), pair.getFirst().getName().getString(), pair.getSecond())));
    }

    private GateIota(Payload payload) {
        super(() -> HexalIotaTypes.GATE);
        this.target = payload;
    }

    public int getGateIndex() {
        return target.index;
    }

    public @Nullable Either<Vec3, EntityAnchor> getTarget() {
        return target.target;
    }

    public @Nullable Vec3 getTargetPos(ServerLevel level) {
        var target = this.getTarget();

        if (target == null)
            return null;

        return target.map(vec3 -> vec3, entityAnchor -> {
            var entity = level.getEntity(entityAnchor.uuid);

            if (entity == null)
                return null;

            return entity.position().add(entityAnchor.offset);
        });
    }

    public boolean isDrifting() {
        return this.getTarget() == null;
    }

    public boolean isLocationAnchored() {
        var target = this.getTarget();
        return (target != null) && (target.left().isPresent());
    }

    public boolean isEntityAnchored() {
        var target = this.getTarget();
        return (target != null) && (target.right().isPresent());
    }

    public Set<Entity> getMarked(ServerLevel level) {
        var marked = GateManager.allMarked.getOrDefault(this.getGateIndex(), new HashSet<>());

        var out = new HashSet<Entity>();
        for (var mark : marked) {
            var markEntity = level.getEntity(mark);
            if (markEntity != null)
                out.add(markEntity);
        }
        return out;
    }

    public boolean isMarked(Entity entity) {
        var marked = GateManager.allMarked.getOrDefault(this.getGateIndex(), new HashSet<>());
        return marked.contains(entity.getUUID());
    }

    public int getNumMarked() {
        return GateManager.allMarked.getOrDefault(this.getGateIndex(), new HashSet<>()).size();
    }

    public void mark(Entity entity) {
        GateManager.mark(this.getGateIndex(), entity);
    }

    public void unmark(@NotNull Entity entity) {
        GateManager.unmark(this.getGateIndex(), entity);
    }

    public void clearMarked() {
        GateManager.clearMarked(this.getGateIndex());
    }

    @Override
    protected boolean toleratesOther(Iota that) {
        return typesMatch(this, that) &&
                that instanceof GateIota gthat &&
                this.getGateIndex() == gthat.getGateIndex();
    }

    @Override
    public Component display() {

        if (isDrifting()) {
            return Component.translatable("hexal.spelldata.gate", getGateIndex()).withStyle(ChatFormatting.LIGHT_PURPLE);
        }
        // if we get here then getTarget can't be null.
        //noinspection DataFlowIssue
        return getTarget().map(
                vec3 -> Component.translatable("hexal.spelldata.gate", getGateIndex()).append(String.format(" (%.2f, %.2f, %.2f)", vec3.x, vec3.y, vec3.z)).withStyle(ChatFormatting.LIGHT_PURPLE),
                entityAnchor -> {
                    var offsetStr = String.format("%.2f, %.2f, %.2f", entityAnchor.offset.x, entityAnchor.offset.y, entityAnchor.offset.z);
                    var anchorStr = String.format(" (%s, %s)", entityAnchor.name, offsetStr);

                    return Component.translatable("hexal.spelldata.gate", getGateIndex()).append(anchorStr).withStyle(ChatFormatting.LIGHT_PURPLE);
                });
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.target);
    }

    @Override
    public boolean isTruthy() {
        return true;
    }

    public static IotaType<GateIota> TYPE = new IotaType<>() {
        public static final MapCodec<GateIota> CODEC = Payload.CODEC.xmap(GateIota::new, a -> a.target).fieldOf("target");


        @Override
        public MapCodec<GateIota> codec() {
            return null;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, GateIota> streamCodec() {
            return null;
        }

        @Override
        public int color() {
            return 0xff_ff55ff;
        }
    };
}
