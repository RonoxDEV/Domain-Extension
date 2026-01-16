package net.domainexpansion.client.renderer;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.domainexpansion.DomainExpansionMod;

public class DomainRenderTypes extends RenderStateShard {
    public static ShaderInstance unlimitedVoidShader;

    private static final ResourceLocation NOISE = new ResourceLocation(DomainExpansionMod.MODID,
            "textures/environment/infinite_noise.png");
    private static final ResourceLocation VOID_BG = new ResourceLocation(DomainExpansionMod.MODID,
            "textures/environment/void_background.png");

    public static final RenderType UNLIMITED_VOID = RenderType.create("unlimited_void",
            DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.TRIANGLES, 256, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(new ShaderStateShard(() -> unlimitedVoidShader))
                    .setTextureState(new MultiTextureStateShard.Builder()
                            .add(NOISE, false, false)
                            .add(VOID_BG, false, false)
                            .build())
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .createCompositeState(false));

    public DomainRenderTypes(String name, Runnable setupState, Runnable clearState) {
        super(name, setupState, clearState);
    }
}
