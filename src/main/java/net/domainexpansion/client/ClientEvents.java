package net.domainexpansion.client;

import net.domainexpansion.DomainExpansionMod;
import net.domainexpansion.entity.DomainEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DomainExpansionMod.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onCameraCompute(ViewportEvent.ComputeCameraAngles event) {
        if (CameraShakeHandler.isShaking()) {
            // Get different random values for each axis for true chaotic shake
            float shakePitch = CameraShakeHandler.getShakeOffset((float) event.getPartialTick());
            float shakeYaw = CameraShakeHandler.getShakeOffset((float) event.getPartialTick() + 100f); // Offset seed
            float shakeRoll = CameraShakeHandler.getShakeOffset((float) event.getPartialTick() + 200f); // Different
                                                                                                        // offset

            event.setPitch(event.getPitch() + shakePitch);
            event.setYaw(event.getYaw() + shakeYaw);
            event.setRoll(event.getRoll() + shakeRoll * 0.3f); // Less roll intensity
        }

        // Keep existing domain target shake as fallback
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null)
            return;

        // Find if player is the target of a DomainEntity
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof DomainEntity domain && domain.getState() == DomainEntity.STATE_ACTIVE) {
                if (mc.player.getUUID().equals(domain.getTargetUUID())) {
                    // Apply persistent subtle shake for target
                    float shake = (float) Math.sin((mc.level.getGameTime() + event.getPartialTick()) * 0.8f) * 2.0f;
                    event.setPitch(event.getPitch() + shake);
                    event.setYaw(event.getYaw() + shake);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onInput(InputEvent.MouseButton event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null)
            return;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof DomainEntity domain && domain.getState() == DomainEntity.STATE_ACTIVE) {
                if (mc.player.getUUID().equals(domain.getTargetUUID())) {
                    event.setCanceled(true);
                }
            }
        }
    }
}
