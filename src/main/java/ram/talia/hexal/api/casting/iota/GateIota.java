package ram.talia.hexal.api.casting.iota;

import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import kotlin.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
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
import ram.talia.hexal.api.util.Anyone;
import ram.talia.hexal.common.lib.hex.HexalIotaTypes;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

// look away, look away
// every single line is nothing but dismay
// so look away
// look away
public class GateIota extends Iota {
    public static String TAG_INDEX = "index";
    public static String TAG_TARGET_TYPE = "target_type";
    public static String TAG_TARGET_POSITION = "target_position";
    public static String TAG_TARGET_UUID = "target_uuid";
    public static String TAG_TARGET_NAME = "target_name";
    public static String TAG_TARGET = "target";

    public record DriftingAnchor() {
        public static final DriftingAnchor INSTANCE = new DriftingAnchor();
        public static final Codec<DriftingAnchor> CODEC = Codec.unit(INSTANCE);
    }

    @ParametersAreNonnullByDefault
    public record EntityAnchor(UUID uuid, String name, Vec3 offset) {
        public static final Codec<EntityAnchor> CODEC = RecordCodecBuilder.create( entityAnchor -> entityAnchor.group(
                UUIDUtil.CODEC.fieldOf(TAG_TARGET_UUID).forGetter(EntityAnchor::uuid),
                Codec.STRING.fieldOf(TAG_TARGET_NAME).forGetter(EntityAnchor::name),
                Vec3.CODEC.fieldOf(TAG_TARGET_POSITION).forGetter(EntityAnchor::offset)
                ).apply(entityAnchor, EntityAnchor::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, EntityAnchor> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, EntityAnchor>() {
            /* Layout:
            uuid,
            name,
            offset
             */
            @Override
            @ParametersAreNonnullByDefault
            public @NotNull EntityAnchor decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
                UUID uuid = registryFriendlyByteBuf.readUUID();
                String name = ByteBufCodecs.STRING_UTF8.decode(registryFriendlyByteBuf);
                Vec3 pos = registryFriendlyByteBuf.readVec3();
                return new EntityAnchor(uuid, name, pos);
            }
            @Override
            @ParametersAreNonnullByDefault
            public void encode(RegistryFriendlyByteBuf buf, EntityAnchor entityAnchor) {
                buf.writeUUID(entityAnchor.uuid);
                ByteBufCodecs.STRING_UTF8.encode(buf, entityAnchor.name);
                buf.writeVec3(entityAnchor.offset);
            }
        };
    }

    private record Payload(int index, Anyone<Vec3, EntityAnchor, DriftingAnchor> target) {
        public static final Function<Anyone<Vec3, EntityAnchor, DriftingAnchor>, Integer> arghghhh = (Anyone<Vec3, EntityAnchor, DriftingAnchor> target) -> target.isA() ? 0 : (target.isB() ? 1 : 2);
        public static final Codec<Payload> CODEC = RecordCodecBuilder.create( payloadInstance -> payloadInstance.group(
                Codec.INT.fieldOf(TAG_INDEX).forGetter(Payload::index),
                Codec.INT.dispatch(arghghhh,
                        (integer -> switch (integer) {
                            case 0 -> Vec3.CODEC.xmap(Anyone.Companion::<Vec3, EntityAnchor, DriftingAnchor>first, Anyone::getA).fieldOf("target");
                            case 1 -> EntityAnchor.CODEC.xmap(Anyone.Companion::<Vec3, EntityAnchor, DriftingAnchor>second, Anyone::getB).fieldOf("target");
                            default -> DriftingAnchor.CODEC.xmap(Anyone.Companion::<Vec3, EntityAnchor, DriftingAnchor>third, Anyone::getC).fieldOf("target");
                        })).fieldOf("target").forGetter(Payload::target)
            ).apply(payloadInstance, Payload::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, Payload> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, Payload>() {
            /* Layout:
            tag index (int),
            type (int) (0 - location anchored, 1 - entity anchored, 2 - drifting)
            whatever that type demands
             */
            @Override
            @ParametersAreNonnullByDefault
            @MethodsReturnNonnullByDefault
            public Payload decode(RegistryFriendlyByteBuf buf) {
                int index = buf.readInt();
                int type = buf.readInt();
                Anyone<Vec3, EntityAnchor, DriftingAnchor> anchor = switch(type) {
                    case 0 -> Anyone.Companion.first(buf.readVec3());
                    case 1 -> Anyone.Companion.second(new EntityAnchor(buf.readUUID(), buf.readUtf(), buf.readVec3()));
                    default -> Anyone.Companion.third(DriftingAnchor.INSTANCE);
                };
                return new Payload(index, anchor);
            }

            @Override
            @ParametersAreNonnullByDefault
            @MethodsReturnNonnullByDefault
            public void encode(RegistryFriendlyByteBuf buf, Payload payload) {
                buf.writeInt(payload.index);
                Anyone<Vec3, EntityAnchor, DriftingAnchor> target = payload.target;
                int type = target.isA() ? 0 : (target.isB() ? 1 : 2);
                buf.writeInt(type);
                switch (type) {
                    case 0 -> buf.writeVec3(target.getA());
                    case 1 -> {
                        EntityAnchor anchor = target.getB();
                        buf.writeUUID(anchor.uuid);
                        buf.writeUtf(anchor.name);
                        buf.writeVec3(anchor.offset);
                    }
                    default -> {}
                }
            }
        };
    }

    public final Payload target;

    public GateIota(int index, @Nullable Either<Vec3, Pair<Entity, Vec3>> target) {
        super(() -> HexalIotaTypes.GATE);
        this.target = new Payload(index, target == null ? Anyone.Companion.third(DriftingAnchor.INSTANCE) :
                target.map(Anyone.Companion::first,
                        pair -> Anyone.Companion.second(new EntityAnchor(pair.getFirst().getUUID(), pair.getFirst().getName().getString(), pair.getSecond()))));
    }

    private GateIota(Payload payload) {
        super(() -> HexalIotaTypes.GATE);
        this.target = payload;
    }

    public int getGateIndex() {
        return target.index;
    }

    public @Nullable Anyone<Vec3, EntityAnchor, DriftingAnchor> getTarget() {
        return target.target;
    }

    public @Nullable Vec3 getTargetPos(ServerLevel level) {
        var target = this.getTarget();

        if (target == null)
            return null;

        return target.flatMap(vec3 -> vec3, entityAnchor -> {
            var entity = level.getEntity(entityAnchor.uuid);

            if (entity == null)
                return null;

            return entity.position().add(entityAnchor.offset);
        }, drifting -> null);
    }

    public boolean isDrifting() {
        return this.getTarget() == null || this.getTarget().isC();
    }

    public boolean isLocationAnchored() {
        var target = this.getTarget();
        return (target != null) && target.isA();
    }

    public boolean isEntityAnchored() {
        var target = this.getTarget();
        return (target != null) && target.isB();
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
        return getTarget().flatMap(
                vec3 -> Component.translatable("hexal.spelldata.gate", getGateIndex()).append(String.format(" (%.2f, %.2f, %.2f)", vec3.x, vec3.y, vec3.z)).withStyle(ChatFormatting.LIGHT_PURPLE),
                entityAnchor -> {
                    var offsetStr = String.format("%.2f, %.2f, %.2f", entityAnchor.offset.x, entityAnchor.offset.y, entityAnchor.offset.z);
                    var anchorStr = String.format(" (%s, %s)", entityAnchor.name, offsetStr);

                    return Component.translatable("hexal.spelldata.gate", getGateIndex()).append(anchorStr).withStyle(ChatFormatting.LIGHT_PURPLE);
                }, drifting -> Component.translatable("hexal.spelldata.gate", getGateIndex()).withStyle(ChatFormatting.LIGHT_PURPLE));
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
        public static final StreamCodec<RegistryFriendlyByteBuf, GateIota> STREAM_CODEC = Payload.STREAM_CODEC.map(GateIota::new, a -> a.target);

        @Override
        public MapCodec<GateIota> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, GateIota> streamCodec() {
            return STREAM_CODEC;
        }

        @Override
        public int color() {
            return 0xff_ff55ff;
        }
    };
}
