package dev.purifiedundead.client.echo;

import com.github.tartaricacid.simplebedrockmodel.client.bedrock.model.BedrockPart;
import com.github.tartaricacid.touhoulittlemaid.client.resource.CustomPackLoader;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntityMaidRenderer;
import com.github.tartaricacid.touhoulittlemaid.entity.backpack.BackpackManager;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.purifiedundead.combat.FerinBladeTrajectory;
import dev.purifiedundead.config.PurifiedUndeadConfig;
import dev.purifiedundead.entity.FerinEntity;
import dev.purifiedundead.nocturne.EchoItemAppearance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.IdentityHashMap;
import java.util.Map;

/** Only reached on the physical client with TLM present. Never add the surrogate to a Level or tick it. */
public final class MaidEchoRenderer {
    private final EntityRendererProvider.Context context;
    private EchoGeckoRenderer gecko;
    private EntityMaidRenderer nativeRenderer;
    private final Cache<FerinEntity, Proxy> proxies = CacheBuilder.newBuilder()
            .weakKeys().maximumSize(128).build();
    private boolean warned;

    public MaidEchoRenderer(EntityRendererProvider.Context context) { this.context = context; }

    public boolean render(FerinEntity release, double age, float partial, Vec3 anchor,
                       PoseStack parent, MultiBufferSource buffers) {
        var proxy = proxies.getIfPresent(release);
        // A private pose stack isolates even a broken skin renderer's unbalanced pushes.
        var poses = new PoseStack();
        poses.last().pose().set(parent.last().pose());
        poses.last().normal().set(parent.last().normal());
        try {
            if (proxy == null) {
                proxy = new Proxy(release.level());
                proxy.stage = release.getStage();
                proxies.put(release, proxy);
                var appearance = release.echoAppearance();
                var mapped = appearance.getString("geckoEcho");
                if (!mapped.isEmpty() && (proxy.stage >= 1 && proxy.stage <= 4) && appearance.contains("ysmModel")
                        && PurifiedUndeadConfig.ECHO_SPECIAL_ANIMATIONS.get()) {
                    var info = CustomPackLoader.MAID_MODELS.getInfo(mapped).orElse(null);
                    if (info != null && info.isGeckoModel() && mapped.equals(info.getModelId().toString())) {
                        if (gecko == null) gecko = new EchoGeckoRenderer(context);
                        if (gecko.canUseMappedSkin(proxy, info)) {
                            proxy.setModelId(mapped);
                            proxy.mappedSkin = true;
                        }
                    }
                }
                proxy.loadAppearance(appearance);
            }
            if (proxy.failed) return false;
            proxy.stage = release.getStage();
            proxy.age = (float)age;
            var direction = release.releaseDirection();
            float yaw = (float)Math.toDegrees(Math.atan2(-direction.x, direction.z));
            proxy.setPos(anchor);
            proxy.xo = proxy.xOld = anchor.x;
            proxy.yo = proxy.yOld = anchor.y;
            proxy.zo = proxy.zOld = anchor.z;
            proxy.setYRot(yaw);
            proxy.yRotO = proxy.yBodyRot = proxy.yBodyRotO = proxy.yHeadRot = proxy.yHeadRotO = yaw;
            proxy.setXRot((float)-Math.toDegrees(Math.asin(Math.max(-1, Math.min(1, direction.y)))));
            proxy.xRotO = proxy.getXRot();
            proxy.tickCount = 1 + (int)age;
            proxy.swingTime = (int)age;
            proxy.swinging = true;
            proxy.swingingArm = net.minecraft.world.InteractionHand.MAIN_HAND;
            proxy.attackAnim = proxy.oAttackAnim = (float)Math.min(.99, age / FerinBladeTrajectory.sweepEnd(proxy.stage));
            float scale = PurifiedUndeadConfig.ECHO_SCALE.get().floatValue();
            poses.scale(scale, scale, scale);
            poses.translate(0, -proxy.getBbHeight() * .52, 0);
            var models = CustomPackLoader.MAID_MODELS;
            var info = models.getInfo(proxy.getModelId()).orElse(null);
            if (!proxy.isYsmModel() && info != null && info.isGeckoModel()) {
                if (gecko == null) gecko = new EchoGeckoRenderer(context);
                return gecko.draw(proxy, info, yaw, partial, poses, buffers);
            } else {
                var model = proxy.isYsmModel() ? null : models.getModel(proxy.getModelId()).orElse(null);
                var saved = new IdentityHashMap<BedrockPart, Quaternionf>();
                try {
                    if (model != null && EchoAnimations.enabled())
                        poseClassic(model.getModelMap(), proxy, saved);
                    if (nativeRenderer == null) nativeRenderer = new EntityMaidRenderer(context) {
                        // Select the translucent body pass while allowing TLM to draw backpack layers.
                        @Override protected boolean isBodyVisible(net.minecraft.world.entity.Mob mob) { return false; }
                    };
                    net.minecraft.client.renderer.entity.EntityRenderer<net.minecraft.world.entity.Mob> renderer = nativeRenderer;
                    renderer.render(proxy, yaw, partial, poses, buffers, LightTexture.FULL_BRIGHT);
                } finally {
                    saved.forEach((part, rotation) -> part.additionalQuaternion = rotation);
                }
            }
        } catch (RuntimeException ex) {
            if (proxy != null) proxy.failed = true;
            if (!warned) {
                warned = true;
                com.mojang.logging.LogUtils.getLogger().warn("Maid apparition unavailable for this release", ex);
            }
        }
        return false;
    }

    private static void poseClassic(Map<String, BedrockPart> bones, Proxy proxy,
                                    Map<BedrockPart, Quaternionf> saved) {
        EchoAnimations.sample(new EchoAnimations.Skeleton() {
            public boolean root(String name) { return bones.containsKey(name) && bones.get(name).parent == null; }
            public boolean child(String name, String parent) {
                return bones.containsKey(name) && bones.containsKey(parent) && bones.get(name).parent == bones.get(parent);
            }
        }, proxy.stage, proxy.age, (name, x, y, z) -> {
            var bone = bones.get(name);
            if (bone == null) return;
            saved.putIfAbsent(bone, bone.additionalQuaternion);
            bone.additionalQuaternion = new Quaternionf(bone.additionalQuaternion).rotateXYZ(x, y, z);
        });
    }

    static final class Proxy extends EntityMaid {
        private final net.minecraft.world.item.ItemStack ironSword = net.minecraft.world.item.Items.IRON_SWORD.getDefaultInstance();
        boolean playingCut;
        boolean mappedSkin;
        private net.minecraft.world.item.ItemStack echoHead = net.minecraft.world.item.ItemStack.EMPTY;

        @Override public net.minecraft.world.item.ItemStack getItemBySlot(EquipmentSlot slot) {
            if (slot == EquipmentSlot.HEAD && echoHead != null && !echoHead.isEmpty()) return echoHead;
            return super.getItemBySlot(slot);
        }
        boolean failed;

        @Override public net.minecraft.world.item.ItemStack getMainHandItem() {
            if (!playingCut) return super.getMainHandItem();
            return spinInLeftHand() ? net.minecraft.world.item.ItemStack.EMPTY : ironSword;
        }

        @Override public net.minecraft.world.item.ItemStack getOffhandItem() {
            if (!playingCut) return super.getOffhandItem();
            return spinInLeftHand() ? ironSword : net.minecraft.world.item.ItemStack.EMPTY;
        }

        boolean spinInLeftHand() {
            return stage == 4 && FerinComboCut.ledAge(age) >= dev.purifiedundead.entity.FerinSlashMesh.SPIN_HANDOFF;
        }

        int stage;
        float age;
        EchoGeckoRenderer.Animation animation;

        Proxy(Level level) {
            super(level);
            noPhysics = true;
            setNoGravity(true);
            setOnGround(true);
            setNoAi(true);
            setSilent(true);
            setSwingingArms(true);
            getConfigManager().setChatBubbleShow(false);
        }

        @Override public boolean isInvisibleTo(Player viewer) { return false; }

        void loadAppearance(CompoundTag tag) {
            if (!mappedSkin && ResourceLocation.tryParse(tag.getString("model")) != null && !tag.getString("model").isEmpty())
                setModelId(tag.getString("model"));
            if (!mappedSkin && tag.contains("ysmModel")) {
                setYsmModel(tag.getString("ysmModel"), tag.getString("ysmTexture"), Component.empty());
                setIsYsmModel(true);
                var vars = tag.getCompound("variables");
                for (String key : vars.getAllKeys()) roamingVars.put(key, vars.getFloat(key));
                roamingVarsUpdateFlag++;
            }
            var backpack = ResourceLocation.tryParse(tag.getString("backpack"));
            if (backpack != null) BackpackManager.findBackpack(backpack).ifPresent(this::setMaidBackpackType);
            var item = ResourceLocation.tryParse(tag.getString("backpackItem"));
            if (tag.contains("displayStack")) setBackpackShowItem(EchoItemAppearance.restore(tag.getCompound("displayStack")));
            else if (item != null) BuiltInRegistries.ITEM.getOptional(item).ifPresent(i -> setBackpackShowItem(i.getDefaultInstance()));
            if (tag.contains("headStack")) echoHead = EchoItemAppearance.restore(tag.getCompound("headStack"));
            if (tag.contains("backpackFluid")) setBackpackFluid(tag.getString("backpackFluid"));
            if (tag.contains("showBackpack")) getConfigManager().setShowBackpack(tag.getBoolean("showBackpack"));
            if (tag.contains("showBackItem")) getConfigManager().setShowBackItem(tag.getBoolean("showBackItem"));
            setLeftHanded(tag.getBoolean("leftHanded"));
        }
    }
}
