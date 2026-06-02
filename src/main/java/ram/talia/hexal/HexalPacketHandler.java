package ram.talia.hexal;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.logging.log4j.util.TriConsumer;
import ram.talia.hexal.common.network.MsgParticleLines;
import ram.talia.hexal.common.network.MsgSingleParticleAck;
import ram.talia.hexal.common.network.MsgWispCastSoundS2C;

import java.util.function.Consumer;

public class HexalPacketHandler {
    public static void init(IEventBus modBus) {
        modBus.addListener((RegisterPayloadHandlersEvent event) -> {
            final PayloadRegistrar registrar = event.registrar("1");
            registrar.playToClient(MsgParticleLines.TYPE, MsgParticleLines.STREAM_CODEC,
                    makeClientBoundHandler(MsgParticleLines::handle));
            registrar.playToClient(MsgSingleParticleAck.TYPE, MsgSingleParticleAck.STREAM_CODEC,
                    makeClientBoundHandler(MsgSingleParticleAck::handle));
            registrar.playToClient(MsgWispCastSoundS2C.TYPE, MsgWispCastSoundS2C.STREAM_CODEC,
                    makeClientBoundHandler(MsgWispCastSoundS2C::handle));
        });
    }

    private static <T extends CustomPacketPayload> IPayloadHandler<T> makeServerBoundHandler(
            TriConsumer<T, MinecraftServer, ServerPlayer> handler) {
        return (m, ctx) -> {
            handler.accept(m, ctx.player().getServer(), (ServerPlayer) ctx.player());
        };
    }

    private static <T extends CustomPacketPayload> IPayloadHandler<T> makeClientBoundHandler(Consumer<T> consumer) {
        return (m, ctx) -> {
            consumer.accept(m);
        };
    }
}
