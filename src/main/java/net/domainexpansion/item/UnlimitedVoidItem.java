package net.domainexpansion.item;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundSource;
import net.domainexpansion.entity.DomainEntity;
import net.domainexpansion.init.DomainExpansionModEntities;
import net.domainexpansion.init.DomainExpansionModSounds;

import java.util.Comparator;

public class UnlimitedVoidItem extends Item {
	public UnlimitedVoidItem() {
		super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		if (!level.isClientSide()) {
			DomainEntity domain = new DomainEntity(DomainExpansionModEntities.DOMAIN_ENTITY.get(), level);
			domain.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
			domain.setOwner(player.getUUID());

			// Find nearest target (nearest living entity within 20 blocks, not the player)
			LivingEntity target = level
					.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(20.0), e -> e != player)
					.stream()
					.min(Comparator.comparingDouble(player::distanceToSqr))
					.orElse(null);

			if (target != null) {
				domain.setTarget(target.getUUID());
			}

			level.addFreshEntity(domain);

			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					DomainExpansionModSounds.DOMAINOPEN.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

			if (!player.getAbilities().instabuild) {
				itemstack.shrink(1);
			}

			player.getCooldowns().addCooldown(this, 100); // 5 seconds cooldown
		}
		return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isFoil(ItemStack itemstack) {
		return true;
	}
}