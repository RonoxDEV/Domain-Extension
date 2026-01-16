package net.domainexpansion.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
// import foundry.veil.api.client.render.shader.program.ShaderProgram;
import net.domainexpansion.DomainExpansionMod;
import net.domainexpansion.entity.DomainEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.RenderStateShard;
import org.joml.Matrix4f;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import net.minecraft.client.renderer.ShaderInstance;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.BufferBuilder;
import org.lwjgl.opengl.GL11;

public class DomainEntityRenderer extends EntityRenderer<DomainEntity> {
    private static final ResourceLocation VOID_BG = new ResourceLocation(DomainExpansionMod.MODID,
            "textures/environment/void_background.png");

    public DomainEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DomainEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        if (DomainRenderTypes.unlimitedVoidShader == null)
            return; // Sécurité

        // 1. Animation de taille (Interpolation pour fluidité)
        // Assure-toi d'avoir ajouté getRadius(partialTicks) dans ton Entité
        float radius = entity.getRadius(partialTicks);
        if (radius < 0.1f)
            return;

        poseStack.pushPose();
        poseStack.scale(radius, radius, radius);

        // 2. Configuration RenderSystem
        RenderSystem.disableCull(); // Voir l'intérieur
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false); // Le domaine est un peu fantomatique

        // --- CORRECTION VISIBILITÉ DES BLOCS ---
        RenderSystem.enableDepthTest(); // ACTIVE LE TEST DE PROFONDEUR
        RenderSystem.depthFunc(GL11.GL_LEQUAL); // "Si je suis derrière un mur, ne me dessine pas"
        // --------------------------------------

        // 3. Liaison de la texture et FORCAGE du mode "Repeat" (Crucial pour le shader
        // liquide)
        RenderSystem.setShaderTexture(0,
                new ResourceLocation("domain_expansion", "textures/environment/infinite_noise.png"));
        // Ces deux lignes empêchent les bords moches :
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

        // 4. Activation du Shader
        RenderSystem.setShader(() -> DomainRenderTypes.unlimitedVoidShader);

        // Envoi du temps au shader
        float time = (entity.tickCount + partialTicks);
        if (DomainRenderTypes.unlimitedVoidShader.getUniform("GameTime") != null) {
            DomainRenderTypes.unlimitedVoidShader.getUniform("GameTime").set(time);
        }

        // 5. Dessin de la Sphère (UV Sphere)
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        // Utilisation de POSITION_TEX_COLOR_NORMAL pour correspondre au shader
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);

        Matrix4f matrix = poseStack.last().pose();

        // Densité de la sphère (Plus c'est haut, plus c'est rond)
        int stacks = 40;
        int slices = 40;
        float r = 1.0f;

        for (int i = 0; i < stacks; i++) {
            double lat0 = Math.PI * (-0.5 + (double) (i) / stacks);
            double z0 = Math.sin(lat0);
            double zr0 = Math.cos(lat0);

            double lat1 = Math.PI * (-0.5 + (double) (i + 1) / stacks);
            double z1 = Math.sin(lat1);
            double zr1 = Math.cos(lat1);

            for (int j = 0; j < slices; j++) {
                double lng0 = 2 * Math.PI * (double) (j) / slices;
                double x0 = Math.cos(lng0);
                double y0 = Math.sin(lng0);

                double lng1 = 2 * Math.PI * (double) (j + 1) / slices;
                double x1 = Math.cos(lng1);
                double y1 = Math.sin(lng1);

                // UVs qui se répètent 6 fois autour de la sphère pour la densité du bruit
                float u0 = (float) j / slices * 6.0f;
                float u1 = (float) (j + 1) / slices * 6.0f;
                float v0 = (float) i / stacks * 6.0f;
                float v1 = (float) (i + 1) / stacks * 6.0f;

                // On ajoute les vertices AVEC les normales (même si le shader ne les utilise
                // pas, elles sont dans le format)
                bufferBuilder.vertex(matrix, (float) (x0 * zr0 * r), (float) (y0 * zr0 * r), (float) (z0 * r))
                        .uv(u0, v0)
                        .color(255, 255, 255, 255).normal((float) (x0 * zr0), (float) (y0 * zr0), (float) z0)
                        .endVertex();
                bufferBuilder.vertex(matrix, (float) (x0 * zr1 * r), (float) (y0 * zr1 * r), (float) (z1 * r))
                        .uv(u0, v1)
                        .color(255, 255, 255, 255).normal((float) (x0 * zr1), (float) (y0 * zr1), (float) z1)
                        .endVertex();
                bufferBuilder.vertex(matrix, (float) (x1 * zr1 * r), (float) (y1 * zr1 * r), (float) (z1 * r))
                        .uv(u1, v1)
                        .color(255, 255, 255, 255).normal((float) (x1 * zr1), (float) (y1 * zr1), (float) z1)
                        .endVertex();
                bufferBuilder.vertex(matrix, (float) (x1 * zr0 * r), (float) (y1 * zr0 * r), (float) (z0 * r))
                        .uv(u1, v0)
                        .color(255, 255, 255, 255).normal((float) (x1 * zr0), (float) (y1 * zr0), (float) z0)
                        .endVertex();
            }
        }

        tesselator.end();

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        poseStack.popPose();
    }

    // N'oublie pas les Overrides pour le Culling !
    @Override
    public boolean shouldRender(DomainEntity pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return true; // Toujours visible
    }

    @Override
    public ResourceLocation getTextureLocation(DomainEntity entity) {
        return VOID_BG;
    }
}
