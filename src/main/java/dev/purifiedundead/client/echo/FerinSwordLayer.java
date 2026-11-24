package dev.purifiedundead.client.echo;

import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.GeoLayerRenderer;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.purifiedundead.client.WeaponMesh;
import dev.purifiedundead.client.WeaponMesh.Face;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Vector3f;

import java.util.List;

/** Render-only Blockbench prop. Positions are model pixels relative to the primary hand. */
final class FerinSwordLayer extends GeoLayerRenderer<MaidEchoRenderer.Proxy, EchoGeckoRenderer> {
    private static final ResourceLocation MODEL = ResourceLocation.tryParse("purified_undead:echo/ferin_sword.json");
    private static final RenderType MATERIAL = RenderType.entityCutout(ResourceLocation.tryParse(
            "purified_undead:textures/effect/ferin_sword.png"));
    private final List<Face> faces;

    FerinSwordLayer(EchoGeckoRenderer renderer, ResourceManager resources) {
        this(renderer, WeaponMesh.load(resources, MODEL));
    }

    private FerinSwordLayer(EchoGeckoRenderer renderer, List<Face> faces) {
        super(renderer);
        this.faces = faces;
    }

    @Override public FerinSwordLayer copy(EchoGeckoRenderer renderer) {
        return new FerinSwordLayer(renderer, faces);
    }

    @Override public void render(PoseStack poses, MultiBufferSource buffers, int light, MaidEchoRenderer.Proxy maid,
                                 float limbSwing, float limbAmount, float partial, float age, float yaw, float pitch) {
        if (!maid.playingCut || !maid.animation.swordReady || faces.isEmpty()) return;
        var model = maid.animation.getCurrentModel();
        var chain = maid.cutInLeftHand() ? model.leftHandBones() : model.rightHandBones();
        var locator = new PoseStack();
        if (RenderUtils.prepMatrixForLocator(locator, chain)) return;
        var grip = locator.last().pose().getTranslation(new Vector3f());
        poses.pushPose();
        try {
            poses.translate(grip.x, grip.y, grip.z);
            poses.mulPose(maid.animation.swordFrame);
            poses.scale(1F / 16, 1F / 16, 1F / 16);
            var pose = poses.last();
            var buffer = buffers.getBuffer(MATERIAL);
            for (var face : faces) {
                var n = face.normal();
                for (var v : face.vertices()) {
                    buffer.vertex(pose.pose(), v[0], v[1], v[2]).color(255, 255, 255, 255)
                            .uv(v[3], v[4]).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                            .normal(pose.normal(), n.x, n.y, n.z).endVertex();
                }
            }
        } finally {
            poses.popPose();
        }
    }
}
