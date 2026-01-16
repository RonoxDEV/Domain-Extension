/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.domainexpansion.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;

import net.domainexpansion.DomainExpansionMod;

public class DomainExpansionModSounds {
	public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS,
			DomainExpansionMod.MODID);
	public static final RegistryObject<SoundEvent> DOMAINOPEN = REGISTRY.register("domainopen",
			() -> SoundEvent.createVariableRangeEvent(new ResourceLocation("domain_expansion", "domainopen")));
	public static final RegistryObject<SoundEvent> DOMAIN_AMBIANCE = REGISTRY.register("domainambient1",
			() -> SoundEvent.createVariableRangeEvent(new ResourceLocation("domain_expansion", "domainambient1")));
	public static final RegistryObject<SoundEvent> DOMAIN_OUT = REGISTRY.register("outdomain",
			() -> SoundEvent.createVariableRangeEvent(new ResourceLocation("domain_expansion", "outdomain")));
}