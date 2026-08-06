package ram.talia.hexal.common.network;

import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.particles.ConjureParticleOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import static ram.talia.hexal.Hexal.modLoc;
import static ram.talia.hexal.api.FunUtilsKt.nextColour;

public record MsgSingleParticleAck(Vec3 pos, FrozenPigment colouriser) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MsgSingleParticleAck> TYPE = new CustomPacketPayload.Type<>(modLoc("sngprt"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgSingleParticleAck> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, MsgSingleParticleAck>() {
        @Override
        public @NotNull MsgSingleParticleAck decode(RegistryFriendlyByteBuf byteBuf) {
            Vec3 position = new Vec3(byteBuf.readDouble(), byteBuf.readDouble(), byteBuf.readDouble());
            FrozenPigment pigment = FrozenPigment.STREAM_CODEC.decode(byteBuf);
            return new MsgSingleParticleAck(position, pigment);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, MsgSingleParticleAck msgSingleParticleAck) {
            buf.writeDouble(msgSingleParticleAck.pos.x);
            buf.writeDouble(msgSingleParticleAck.pos.y);
            buf.writeDouble(msgSingleParticleAck.pos.z);
            FrozenPigment.STREAM_CODEC.encode(buf, msgSingleParticleAck.colouriser);
        }
    };

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MsgSingleParticleAck self) {
        Minecraft.getInstance().execute(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) {
                return;
            }
            int colour = nextColour(self.colouriser, level.random);
            level.addParticle(new ConjureParticleOptions(colour),
                    self.pos.x, self.pos.y, self.pos.z, 0.0, 0.0, 0.0);
            for (int i = 0; i < 11; i++) {
                colour = nextColour(self.colouriser,level.random);
                double offsetX = level.random.nextFloat() * 0.1 - 0.05;
                double offsetY = level.random.nextFloat() * 0.1 - 0.05;
                double offsetZ = level.random.nextFloat() * 0.1 - 0.05;
                level.addParticle(new ConjureParticleOptions(colour),
                        self.pos.x + offsetX, self.pos.y + offsetY, self.pos.z + offsetZ, 0.0, 0.0, 0.0);
            }
        });
    }
}
