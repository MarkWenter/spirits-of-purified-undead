package dev.purifiedundead.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.purifiedundead.entity.FerinEntity;
import dev.purifiedundead.entity.FerinSlashMesh;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/** Initial release: only the independent slash mesh is visible; the knight rig is archived. */
public final class FerinRenderer extends EntityRenderer<FerinEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("purified_undead", "textures/entity/ferin_slash_white.png");

    public FerinRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(
            FerinEntity entity,
            float yaw,
            float partialTick,
            PoseStack poses,
            MultiBufferSource buffers,
            int packedLight) {
        double age = entity.level().getGameTime() - entity.getStageStartedAt() + partialTick;
        // Undo the old GeckoLib +180-degree correction to match the captured combat basis.
        double angle = Math.toRadians(entity.getYRot() - 180.0);
        double cos = Math.cos(angle), sin = Math.sin(angle);
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
        Matrix4f matrix = poses.last().pose();
        for (FerinSlashMesh.Quad quad : FerinSlashMesh.sample(entity.getStage(), age)) {
            vertex(vertices, matrix, quad.a(), cos, sin);
            vertex(vertices, matrix, quad.b(), cos, sin);
            vertex(vertices, matrix, quad.c(), cos, sin);
            vertex(vertices, matrix, quad.d(), cos, sin);
        }
    }

    private static void vertex(
            VertexConsumer out, Matrix4f matrix, FerinSlashMesh.Vertex v, double cos, double sin) {
        out.vertex(
                        matrix,
                        (float) (-v.right() * cos - v.forward() * sin),
                        (float) v.up(),
                        (float) (-v.right() * sin + v.forward() * cos))
                .color(v.red(), v.green(), v.blue(), v.alpha())
                .uv(0.5F, 0.5F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(0, 1, 0)
                .endVertex();
    }

    @Override
    public boolean shouldRender(FerinEntity entity, Frustum frustum, double x, double y, double z) {
        return entity.shouldRender(x, y, z)
                && frustum.isVisible(entity.getBoundingBox().inflate(12));
    }

    @Override
    public ResourceLocation getTextureLocation(FerinEntity entity) {
        return TEXTURE;
    }
}
