/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.domainexpansion.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.Block;

import net.domainexpansion.block.DomainOcculteBlock;
import net.domainexpansion.DomainExpansionMod;

public class DomainExpansionModBlocks {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, DomainExpansionMod.MODID);
	public static final RegistryObject<Block> DOMAIN_OCCULTE;
	static {
		DOMAIN_OCCULTE = REGISTRY.register("domain_occulte", DomainOcculteBlock::new);
	}
	// Start of user code block custom blocks
	// End of user code block custom blocks
}