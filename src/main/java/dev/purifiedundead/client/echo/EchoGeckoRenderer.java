package dev.purifiedundead.client.echo;

import com.github.tartaricacid.touhoulittlemaid.client.entity.GeckoMaidEntity;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.GeckoEntityMaidRenderer;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.event.predicate.AnimationEvent;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.molang.context.AnimationContext;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.util.Color;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.animated.AnimatedGeoBone;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3d;

import java.util.IdentityHashMap;

/** Uses TLM's own Gecko runtime (distinct from the mod's GeckoLib 4 dependency). */
final class EchoGeckoRenderer extends GeckoEntityMaidRenderer<MaidEchoRenderer.Proxy> {
    EchoGeckoRenderer(EntityRendererProvider.Context context) { super(context); }

    void draw(MaidEchoRenderer.Proxy maid, MaidModelInfo info, float yaw, float partial,
              PoseStack poses, MultiBufferSource buffers) {
        if (maid.animation == null) {
            maid.animation = new Animation(maid);
            maid.animation.setMaidInfo(info);
        }
        try {
            geoRender(maid, yaw, partial, poses, buffers, LightTexture.FULL_BRIGHT);
        } finally {
            maid.animation.restore();
        }
    }

    @Override public GeckoMaidEntity<MaidEchoRenderer.Proxy> getAnimatableEntity(MaidEchoRenderer.Proxy maid) {
        return maid.animation;
    }

    @Override public Color getRenderColor(MaidEchoRenderer.Proxy maid, float partial, PoseStack poses,
            MultiBufferSource buffers, VertexConsumer vertex, int light) {
        return Color.ofRGBA(222, 218, 255, 160);
    }

    @Override public RenderType getRenderType(MaidEchoRenderer.Proxy maid, float partial, PoseStack poses,
            MultiBufferSource buffers, VertexConsumer vertex, int light, ResourceLocation texture) {
        return RenderType.entityTranslucent(texture);
    }

    static final class Animation extends GeckoMaidEntity<MaidEchoRenderer.Proxy> {
        private final IdentityHashMap<AnimatedGeoBone, Vector3d> saved = new IdentityHashMap<>();
        Animation(MaidEchoRenderer.Proxy maid) { super(maid, maid); }

        @Override public boolean setCustomAnimations(AnimationContext context, AnimationEvent event) {
            boolean result = super.setCustomAnimations(context, event);
            if (getCurrentModel() == null || !EchoAnimations.enabled()) return result;
            var bones = getCurrentModel().bones();
            EchoAnimations.sample(new EchoAnimations.Skeleton() {
                public boolean root(String name) {
                    return bones.containsKey(name) && bones.get(name).geoBone().parent() == null;
                }
                public boolean child(String name, String parent) {
                    return bones.containsKey(name) && bones.containsKey(parent)
                            && bones.get(name).geoBone().parent() == bones.get(parent).geoBone();
                }
            }, getEntity().stage, getEntity().age, (name, x, y, z) -> {
                var bone = bones.get(name);
                if (bone == null) return;
                saved.computeIfAbsent(bone, AnimatedGeoBone::getRotation);
                bone.addRotation(x, y, z);
            });
            return result;
        }

        void restore() {
            saved.forEach(AnimatedGeoBone::setRotation);
            saved.clear();
        }
    }
}
