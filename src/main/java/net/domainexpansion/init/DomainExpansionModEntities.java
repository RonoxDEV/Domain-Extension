package net.domainexpansion.init;

import net.domainexpansion.DomainExpansionMod;
import net.domainexpansion.entity.DomainEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DomainExpansionModEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES,
            DomainExpansionMod.MODID);

    public static final RegistryObject<EntityType<DomainEntity>> DOMAIN_ENTITY = REGISTRY.register("domain_entity",
            () -> EntityType.Builder.<DomainEntity>of(DomainEntity::new, MobCategory.MISC)
                    .sized(1.0f, 1.0f) // Technical entity dimensions
                    .build("domain_entity"));
}
