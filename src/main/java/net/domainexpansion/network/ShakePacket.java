package net.domainexpansion.network;

import net.domainexpansion.client.CameraShakeHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ShakePacket {
    private final float intensity;
    private final int durationTicks;

    public ShakePacket(float intensity, int durationTicks) {
        this.intensity = intensity;
        this.durationTicks = durationTicks;
    }

    public static void encode(ShakePacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.intensity);
        buf.writeInt(msg.durationTicks);
    }

    public static ShakePacket decode(FriendlyByteBuf buf) {
        return new ShakePacket(buf.readFloat(), buf.readInt());
    }

    public static void handle(ShakePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-side only
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                CameraShakeHandler.triggerShake(msg.intensity, msg.durationTicks);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
