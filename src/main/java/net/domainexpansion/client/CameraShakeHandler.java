package net.domainexpansion.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class CameraShakeHandler {
    private static float shakeIntensity = 0.0f;
    private static long shakeEndTime = 0;
    private static long shakeStartTime = 0;
    private static Random random = new Random();

    public static void triggerShake(float intensity, int durationTicks) {
        shakeIntensity = intensity;
        shakeStartTime = System.currentTimeMillis();
        shakeEndTime = shakeStartTime + (durationTicks * 50L); // 50ms per tick
    }

    public static float getShakeOffset(float partialTick) {
        long currentTime = System.currentTimeMillis();

        if (currentTime > shakeEndTime) {
            shakeIntensity = 0.0f;
            return 0.0f;
        }

        // Calculate fade-out based on time remaining
        float timeProgress = (float) (currentTime - shakeStartTime) / (float) (shakeEndTime - shakeStartTime);
        float fadeMultiplier = 1.0f - timeProgress; // Fade from 1.0 to 0.0

        // Generate TRUE random shake (changes direction randomly)
        float randomShake = (random.nextFloat() * 2.0f - 1.0f) * shakeIntensity * fadeMultiplier;

        return randomShake;
    }

    public static boolean isShaking() {
        return System.currentTimeMillis() < shakeEndTime;
    }
}
