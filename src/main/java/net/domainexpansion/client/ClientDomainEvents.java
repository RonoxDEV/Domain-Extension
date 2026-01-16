package net.domainexpansion.client;

import net.domainexpansion.entity.DomainEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import net.domainexpansion.DomainExpansionMod;

import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE, modid = DomainExpansionMod.MODID)
public class ClientDomainEvents {

    private static DomainEntity nearbyDomain = null;

    private static DomainEntity findNearbyDomain() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null)
            return null;
        // Rayon de détection (un peu plus large que le domaine pour la transition)
        AABB box = mc.player.getBoundingBox().inflate(25.0);
        List<DomainEntity> domains = mc.level.getEntitiesOfClass(DomainEntity.class, box);

        // On cherche le domaine le plus proche
        for (DomainEntity domain : domains) {
            if (domain.getState() == DomainEntity.STATE_INCANTATION ||
                    domain.getState() == DomainEntity.STATE_OPENING ||
                    domain.getState() == DomainEntity.STATE_ACTIVE) {
                return domain;
            }
        }
        return null;
    }

    private static boolean isInsideDomain() {
        nearbyDomain = findNearbyDomain();
        return nearbyDomain != null;
    }

    // 1. COULEUR DU BROUILLARD : NOIR TOTAL
    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        if (isInsideDomain()) {
            event.setRed(0.0f);
            event.setGreen(0.0f);
            event.setBlue(0.0f);
        }
    }

    // 2. DENSITÉ DU BROUILLARD (Masque les nuages lointains)
    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (isInsideDomain() && nearbyDomain != null) {
            // PHASE D'INCANTATION : Brouillard épais et proche
            if (nearbyDomain.getState() == DomainEntity.STATE_INCANTATION) {
                event.setNearPlaneDistance(0.0f);
                event.setFarPlaneDistance(5.0f); // Brouillard TRÈS proche pour l'effet d'oppression
            }
            // PHASE ACTIVE : Brouillard normal pour voir l'intérieur
            else {
                event.setNearPlaneDistance(20.0f);
                event.setFarPlaneDistance(60.0f);
            }
            event.setCanceled(true); // Annule le rendu par défaut
        }
    }

    // 3. LE "TUEUR DE CIEL" (Cache le Soleil, la Lune et le Dégradé)
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // On intervient juste après que le ciel soit dessiné pour le recouvrir de noir
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY && isInsideDomain()) {
            renderBlackSkyCover(event.getPoseStack().last().pose());
        }
    }

    // Fonction technique pour dessiner une boite noire autour de la caméra
    private static void renderBlackSkyCover(Matrix4f matrix) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F); // Noir Pur
        RenderSystem.disableDepthTest(); // On dessine par-dessus le ciel existant

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        // On dessine un cube géant noir
        float size = 100.0F;

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // Face du Bas (Sol)
        bufferbuilder.vertex(matrix, -size, -20.0f, -size).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex(matrix, size, -20.0f, -size).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex(matrix, size, -20.0f, size).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex(matrix, -size, -20.0f, size).color(0, 0, 0, 255).endVertex();

        // Face du Haut (Ciel)
        bufferbuilder.vertex(matrix, -size, size, -size).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex(matrix, -size, size, size).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex(matrix, size, size, size).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex(matrix, size, size, -size).color(0, 0, 0, 255).endVertex();

        // Ajout des faces latérales pour boucher l'horizon si nécessaire
        // Nord
        bufferbuilder.vertex(matrix, -size, -20.0f, -size).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex(matrix, -size, size, -size).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex(matrix, size, size, -size).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex(matrix, size, -20.0f, -size).color(0, 0, 0, 255).endVertex();

        // Sud
        bufferbuilder.vertex(matrix, -size, -20.0f, size).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex(matrix, size, -20.0f, size).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex(matrix, size, size, size).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex(matrix, -size, size, size).color(0, 0, 0, 255).endVertex();

        // Est
        bufferbuilder.vertex(matrix, size, -20.0f, -size).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex(matrix, size, size, -size).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex(matrix, size, size, size).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex(matrix, size, -20.0f, size).color(0, 0, 0, 255).endVertex();

        // Ouest
        bufferbuilder.vertex(matrix, -size, -20.0f, -size).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex(matrix, -size, -20.0f, size).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex(matrix, -size, size, size).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex(matrix, -size, size, -size).color(0, 0, 0, 255).endVertex();

        tesselator.end();

        RenderSystem.enableDepthTest(); // On remet la profondeur pour le reste du jeu
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
