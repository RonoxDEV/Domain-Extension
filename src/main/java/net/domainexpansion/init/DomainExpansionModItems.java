/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.domainexpansion.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

import net.domainexpansion.item.UnlimitedVoidItem;
import net.domainexpansion.DomainExpansionMod;

public class DomainExpansionModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, DomainExpansionMod.MODID);
	public static final RegistryObject<Item> UNLIMITED_VOID;
	public static final RegistryObject<Item> DOMAIN_OCCULTE;
	static {
		UNLIMITED_VOID = REGISTRY.register("unlimited_void", UnlimitedVoidItem::new);
		DOMAIN_OCCULTE = block(DomainExpansionModBlocks.DOMAIN_OCCULTE, new Item.Properties().rarity(Rarity.EPIC).fireResistant());
	}

	// Start of user code block custom items
	// End of user code block custom items
	private static RegistryObject<Item> block(RegistryObject<Block> block) {
		return block(block, new Item.Properties());
	}

	private static RegistryObject<Item> block(RegistryObject<Block> block, Item.Properties properties) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
	}
}