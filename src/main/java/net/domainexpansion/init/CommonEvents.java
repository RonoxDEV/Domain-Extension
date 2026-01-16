package net.domainexpansion.init;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.domainexpansion.entity.DomainEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import java.util.List;

@Mod.EventBusSubscriber
public class CommonEvents {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        // 1. Check if the block broken is Domain Occulte
        if (event.getState().getBlock() == DomainExpansionModBlocks.DOMAIN_OCCULTE.get()) {

            // 2. Check if a Domain is active nearby
            if (event.getLevel() instanceof Level level) {
                // Search for DomainEntity within 30 blocks
                AABB searchBox = new AABB(event.getPos()).inflate(30);
                List<DomainEntity> domains = level.getEntitiesOfClass(DomainEntity.class, searchBox);

                // If a domain is found and is in active phase (not vanishing)
                // Active timing: 280 to 900 (280 + 620)
                boolean isDomainActive = domains.stream().anyMatch(d -> d.tickCount >= 280 && d.tickCount < 900);

                if (isDomainActive) {
                    // 3. FULL CANCELLATION
                    // Prevents block from disappearing in Creative and Survival
                    event.setCanceled(true);
                }
            }
        }
    }
}
