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
import ram.talia.hexal.Hexal;
import ram.talia.hexal.api.FunUtilsKt;

import java.util.ArrayList;
import java.util.List;

public record MsgParticleLines(List<Vec3> positions, FrozenPigment colouriser) implements CustomPacketPayload {

    public static final Type<MsgParticleLines> TYPE = new Type<>(Hexal.modLoc("prtlns"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgParticleLines> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, MsgParticleLines>() {
        @Override
        public @NotNull MsgParticleLines decode(RegistryFriendlyByteBuf byteBuf) {
            int amount = byteBuf.readInt();
            List<Vec3> posList = new ArrayList<>();
            for (int i = 0; i < amount; i++) {
                posList.add(new Vec3(byteBuf.readDouble(), byteBuf.readDouble(), byteBuf.readDouble()));
            }
            FrozenPigment pigment = FrozenPigment.STREAM_CODEC.decode(byteBuf);
            return new MsgParticleLines(posList, pigment);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, MsgParticleLines msgSingleParticleAck) {
            int size = msgSingleParticleAck.positions.size();
            buf.writeInt(size);
            for (int i = 0; i < size; i++) {
                Vec3 pos = msgSingleParticleAck.positions.get(i);
                buf.writeDouble(pos.x);
                buf.writeDouble(pos.y);
                buf.writeDouble(pos.z);
            }
            FrozenPigment.STREAM_CODEC.encode(buf, msgSingleParticleAck.colouriser);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

   /* public void handle() {
        handle(this);
    }*/

    public static void handle(MsgParticleLines self) {
        Minecraft.getInstance().execute(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            int size = self.positions.size()-1;
            List<Vec3> positions= self.positions;
            for (int i = 0; i < size; i++) {
                Vec3 start = positions.get(i);
                Vec3 end = positions.get(i+1);
                int steps = (int) ((start.subtract(end)).length() * 10);
                for (int step = 0; step <= steps; step++) {
                    // jank
                    Vec3 pos = start.add((end.subtract(start)).scale(((double)step / (double)steps)));
                    int colour = FunUtilsKt.nextColour(self.colouriser, level.random);
                    level.addParticle(new ConjureParticleOptions(colour),
                            pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
                }
            }
        });
    }
}
