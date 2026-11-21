package dev.purifiedundead.client.echo;

import com.github.tartaricacid.touhoulittlemaid.client.entity.GeckoMaidEntity;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.GeckoEntityMaidRenderer;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.event.predicate.AnimationEvent;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.molang.context.AnimationContext;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.util.Color;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.animated.AnimatedGeoBone;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.util.RenderUtils;
import dev.purifiedundead.combat.FerinBladeTrajectory;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.IdentityHashMap;

/** Uses TLM's own Gecko runtime (distinct from the mod's GeckoLib 4 dependency). */
final class EchoGeckoRenderer extends GeckoEntityMaidRenderer<MaidEchoRenderer.Proxy> {
    private final FirstComboCut firstCut;

    EchoGeckoRenderer(EntityRendererProvider.Context context) {
        super(context);
        firstCut = FirstComboCut.load(context.getResourceManager());
    }

    boolean canUseMappedSkin(MaidEchoRenderer.Proxy maid, MaidModelInfo info) {
        if (!firstCut.available()) return false;
        var candidate = new Animation(maid, firstCut);
        candidate.setMaidInfo(info);
        if (!info.getModelId().equals(candidate.getModelLocation()) || !candidate.updateModel() || !candidate.supportsMappedArms()) return false;
        maid.animation = candidate;
        return true;
    }

    void draw(MaidEchoRenderer.Proxy maid, MaidModelInfo info, float yaw, float partial,
              PoseStack poses, MultiBufferSource buffers) {
        if (maid.animation == null) {
            maid.animation = new Animation(maid, firstCut);
            maid.animation.setMaidInfo(info);
        }
        try {
            maid.firstCut = maid.stage == 1 && firstCut.available()
                    && dev.purifiedundead.config.PurifiedUndeadConfig.ECHO_SPECIAL_ANIMATIONS.get()
                    && maid.animation.updateModel() && (maid.mappedSkin || EchoAnimations.wineFoxGecko(maid.animation.skeleton()))
                    && !maid.animation.getCurrentModel().rightHandBones().isEmpty();
            if (maid.firstCut) {
                // Aim the entire sweep plane around the launch point, including its vertical pitch.
                poses.translate(0, maid.getBbHeight() * .52, 0);
                double heading = Math.toRadians(yaw);
                poses.mulPose(new org.joml.Quaternionf().rotationAxis((float)Math.toRadians(-maid.getXRot()),
                        (float)-Math.cos(heading), 0, (float)-Math.sin(heading)));
                double unit = info.getRenderEntityScale() / 16.0;
                var center = maid.animation.cutCenter;
                poses.translate((center.x * Math.cos(heading) - center.z * Math.sin(heading)) * unit,
                        -center.y * unit, (center.x * Math.sin(heading) + center.z * Math.cos(heading)) * unit);
            }
            geoRender(maid, yaw, partial, poses, buffers, LightTexture.FULL_BRIGHT);
        } finally {
            maid.animation.restore();
            maid.firstCut = false;
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
        private final FirstComboCut firstCut;
        private final Vector3f cutCenter = new Vector3f(0, 26.7F, 0);
        private AnimatedGeoBone translatedRoot;
        private Vector3d rootPosition;

        Animation(MaidEchoRenderer.Proxy maid, FirstComboCut firstCut) {
            super(maid, maid);
            this.firstCut = firstCut;
        }

        // Called once for an explicitly mapped built-in skin. Costume/size bones may sit
        // between joints; the sword locator must still inherit the articulated right arm.
        boolean supportsMappedArms() {
            var chain = getCurrentModel().rightHandBones();
            if (chain.isEmpty() || !chain.get(chain.size() - 1).getName().equals("RightHandLocator")) return false;
            String[] required = {"UpperBody", "Arm", "RightArm", "RightForeArm", "RightHandLocator"};
            int next = 0;
            for (var bone : chain) {
                if (bone.getName().equals(required[next]) && ++next == required.length) {
                    var bones = getCurrentModel().bones();
                    var right = bones.get("RightArm");
                    var left = bones.get("LeftArm");
                    if (left == null) return false;
                    // Same shoulder-relative launch center as the authored Wine Fox, scaled to this costume.
                    float width = Math.abs(right.getPivotX() - left.getPivotX());
                    if (!(width > .01F && width < 32)) return false;
                    cutCenter.set((right.getPivotX() + left.getPivotX()) / 2,
                            right.getPivotY() - 2.4F * width / 6.5F, right.getPivotZ());
                    return cutCenter.isFinite();
                }
            }
            return false;
        }

        EchoAnimations.Skeleton skeleton() {
            var bones = getCurrentModel().bones();
            return new EchoAnimations.Skeleton() {
                public boolean root(String name) {
                    return bones.containsKey(name) && bones.get(name).geoBone().parent() == null;
                }
                public boolean child(String name, String parent) {
                    return bones.containsKey(name) && bones.containsKey(parent)
                            && bones.get(name).geoBone().parent() == bones.get(parent).geoBone();
                }
            };
        }

        @Override public boolean setCustomAnimations(AnimationContext context, AnimationEvent event) {
            boolean result = super.setCustomAnimations(context, event);
            if (getCurrentModel() == null) return result;
            var bones = getCurrentModel().bones();
            if (getEntity().firstCut) {
                // Own the arm chain for this draw. Native breathing/holding rotations on its helper
                // parents would otherwise pull the grip off the left shoulder.
                for (String name : REST_PARENTS) setFromRest(name, 0, 0, 0);
                float age = FirstComboCut.ledAge(getEntity().age);
                firstCut.sample(age, this::setFromRest);
                alignBlade(age);
            }
            if (!EchoAnimations.enabled()) return result;
            EchoAnimations.sample(skeleton(), getEntity().stage, getEntity().age, (name, x, y, z) -> {
                var bone = bones.get(name);
                if (bone == null) return;
                saved.computeIfAbsent(bone, AnimatedGeoBone::getRotation);
                bone.addRotation(x, y, z);
            });
            return result;
        }

        private static final String[] REST_PARENTS = {
                "MRoot", "Root", "MAllbody", "MAllBody", "MUpBody", "UpBody", "MUpperBody", "Arm", "MHead",
                "AllHead", "RightHandLocator", "DownBody"
        };

        private void setFromRest(String name, float x, float y, float z) {
            var bone = getCurrentModel().bones().get(name);
            if (bone == null && name.equals("RightHand") && getEntity().mappedSkin)
                bone = getCurrentModel().bones().get("RightHandLocator");
            if (bone == null) return;
            saved.computeIfAbsent(bone, AnimatedGeoBone::getRotation);
            var rest = bone.getInitialSnapshot();
            bone.setRotation(rest.rotationValueX + x, rest.rotationValueY + y, rest.rotationValueZ + z);
        }

        private void alignBlade(float age) {
            var chain = getCurrentModel().rightHandBones();
            var grip = chain.get(chain.size() - 1);
            if (!"RightHandLocator".equals(grip.getName())) return;
            var local = new PoseStack();
            if (RenderUtils.prepMatrixForLocator(local, chain)) return;
            var matrix = local.last().pose();
            if (!(Math.abs(matrix.determinant3x3()) > 1.0e-6F)) return;
            var blade = FerinBladeTrajectory.blade(1, FerinBladeTrajectory.progress(1, 1 + age), 0);
            var ray = new Vector3f((float)blade.tip().right(), (float)(blade.tip().up() - blade.root().up()),
                    (float)-blade.tip().forward()).normalize();
            var goal = new org.joml.Matrix4f(matrix).invert().transformDirection(new Vector3f(ray)).normalize();
            if (!goal.isFinite()) return;
            // Native item/handheld diagonal, after the maid's right-hand item transform.
            var sword = new Vector3f(0, -.17364818F, -.98480775F);
            var rotation = new Quaternionf().rotationZYX(grip.getRotationZ(), grip.getRotationY(), grip.getRotationX())
                    .mul(new Quaternionf().rotationTo(sword, goal)).getEulerAnglesZYX(new Vector3f());
            saved.computeIfAbsent(grip, AnimatedGeoBone::getRotation);
            grip.setRotation(rotation.x, rotation.y, rotation.z);

            // Project the actual blade center onto the launch ray. Moving the entire pose keeps
            // the hand closed around the hilt even when a compatible skin has longer arms.
            var root = chain.get(0);
            var aimed = new PoseStack();
            RenderUtils.prepMatrixForLocator(aimed, chain);
            var radius = aimed.last().pose().transformPosition(new Vector3f(0, -.5F / 16, -5.6F / 16))
                    .sub(cutCenter.x / 16, cutCenter.y / 16, cutCenter.z / 16);
            var correction = new Vector3f(ray).mul(radius.dot(ray)).sub(radius).mul(16);
            // Bound translation for skins with exaggerated proportions.
            if (!correction.isFinite()) return;
            if (correction.lengthSquared() > 16) correction.normalize(4);
            translatedRoot = root;
            rootPosition = root.getPosition();
            root.addPositionX(-correction.x);
            root.addPositionY(correction.y);
            root.addPositionZ(correction.z);
        }

        void restore() {
            if (translatedRoot != null) {
                translatedRoot.setPosition(rootPosition);
                translatedRoot = null;
            }
            saved.forEach(AnimatedGeoBone::setRotation);
            saved.clear();
        }
    }
}
