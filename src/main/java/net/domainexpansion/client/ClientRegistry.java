package net.domainexpansion.client;

import net.domainexpansion.DomainExpansionMod;
import net.domainexpansion.client.renderer.DomainEntityRenderer;
import net.domainexpansion.init.DomainExpansionModEntities;
import net.domainexpansion.client.renderer.DomainRenderTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.client.renderer.ShaderInstance;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraft.resources.ResourceLocation;
import java.io.IOException;

@Mod.EventBusSubscriber(modid = DomainExpansionMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRegistry {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(DomainExpansionModEntities.DOMAIN_ENTITY.get(), DomainEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                new ResourceLocation(DomainExpansionMod.MODID, "unlimited_void"),
                DefaultVertexFormat.POSITION_TEX_COLOR),
                shaderInstance -> {
                    DomainRenderTypes.unlimitedVoidShader = shaderInstance;
                });
    }
}
