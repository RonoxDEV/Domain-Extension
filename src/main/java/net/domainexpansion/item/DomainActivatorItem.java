package net.domainexpansion.item;

import net.domainexpansion.entity.DomainEntity;
import net.domainexpansion.init.DomainExpansionModEntities;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Comparator;

public class DomainActivatorItem extends Item {
    public DomainActivatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                    new AABB(player.getX() - 20, player.getY() - 20, player.getZ() - 20,
                            player.getX() + 20, player.getY() + 20, player.getZ() + 20),
                    entity -> entity != player && entity.distanceTo(player) >= 5);

            LivingEntity target = targets.stream()
                    .min(Comparator.comparingDouble(entity -> entity.distanceTo(player)))
                    .orElse(null);

            if (target != null) {
                Vec3 midPoint = player.position().add(target.position()).scale(0.5);

                DomainEntity domain = new DomainEntity(DomainExpansionModEntities.DOMAIN_ENTITY.get(), level);
                domain.setPos(midPoint.x, midPoint.y, midPoint.z);
                domain.setOwner(player.getUUID());
                domain.setTarget(target.getUUID());
                level.addFreshEntity(domain);

                /*
                 * level.playSound(null, player.getX(), player.getY(), player.getZ(),
                 * ForgeRegistries.SOUND_EVENTS.getValue(new
                 * ResourceLocation("domain_expansion", "domain_open")),
                 * SoundSource.PLAYERS, 1.0f, 1.0f);
                 */

                player.getCooldowns().addCooldown(this, 100);
            }
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
