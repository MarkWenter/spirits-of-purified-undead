package dev.purifiedundead.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.purifiedundead.PurifiedUndead;
import dev.purifiedundead.entity.FerinEntity;
import dev.purifiedundead.entity.FerinVisualModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtils;

/** Independent weapon geometry rendered at the character's animated grip socket. */
public final class FerinSwordLayer extends GeoRenderLayer<FerinEntity> {
    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(
                    PurifiedUndead.MOD_ID, "geo/ferin_sword.geo.json");

    public FerinSwordLayer(GeoRenderer<FerinEntity> renderer) {
        super(renderer);
    }

    @Override
    public void renderForBone(
            PoseStack poseStack,
            FerinEntity entity,
            GeoBone bone,
            RenderType renderType,
            MultiBufferSource buffers,
            VertexConsumer buffer,
            float partialTick,
            int light,
            int overlay) {
        String socket = FerinVisualModel.swordSocketForStage(entity.getStage());
        if (!socket.equals(bone.getName()) || bone.isHidden()) return;
        var weapon = GeckoLibCache.getBakedModels().get(MODEL);
        if (weapon == null) return;
        var color = getRenderer().getRenderColor(entity, partialTick, light);
        poseStack.pushPose();
        // The callback includes all socket transforms but is in model coordinates.
        // Weapon vertices are relative to its grip, so restore the socket pivot.
        RenderUtils.translateToPivotPoint(poseStack, bone);
        for (GeoBone root : weapon.topLevelBones()) {
            getRenderer()
                    .renderRecursively(
                            poseStack,
                            entity,
                            root,
                            renderType,
                            buffers,
                            buffer,
                            true,
                            partialTick,
                            light,
                            overlay,
                            color.getRedFloat(),
                            color.getGreenFloat(),
                            color.getBlueFloat(),
                            color.getAlphaFloat());
        }
        poseStack.popPose();
    }
}
