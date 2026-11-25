package dev.purifiedundead.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.purifiedundead.entity.GuardianWaveEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Sweep each striking surface through its recent poses in the wave's rolled flight frame. */
final class GuardianWaveWake {
    static final float SPIN_CANT = -32, WEAPON_TWIST = -18, DISPLAY_SCALE = 1.25F;
    private static final float RAD = (float)Math.PI / 180;
    private static final int SEGMENTS = 14;
    private static final RenderType MATERIAL = RenderType.entityTranslucentEmissive(
            GuardianWaveWeapon.id("textures/effect/guardian_wave/wakes.png"));
    private final Vector3f a = new Vector3f(), b = new Vector3f(), c = new Vector3f(), d = new Vector3f();

    static float strength(float age) {
        return Math.max(.22F, Math.min(1, Math.min((age + .55F) / 1.4F, (GuardianWaveEntity.LIFETIME - age) / 2)));
    }

    void draw(Matrix4f pose, MultiBufferSource buffers, GuardianWaveForm form, float age) {
        var out = buffers.getBuffer(MATERIAL);
        int frame = Math.min(7, (int)(age * 8 / GuardianWaveEntity.LIFETIME));
        int alpha = Math.round(247 * strength(age));
        int rails = form.turns != 0 ? 2 : form == GuardianWaveForm.GROTH ? 4 : 3;
        for (int rail = 0; rail < rails; rail++) {
            section(form, age, rail, 0, a, b);
            for (int i = 1; i <= SEGMENTS; i++) {
                float t = (float)i / SEGMENTS;
                section(form, age, rail, t, c, d);
                float u0 = (frame * 32 + .5F + 31F * (i - 1) / SEGMENTS) / 256F;
                float u1 = (frame * 32 + .5F + 31F * t) / 256F;
                float v = (form.ordinal() * 16 + .5F) / 128F;
                vertex(out, pose, a, u0, v, alpha);
                vertex(out, pose, c, u1, v, alpha);
                vertex(out, pose, d, u1, v + 15F / 128, alpha);
                vertex(out, pose, b, u0, v + 15F / 128, alpha);
                a.set(c);
                b.set(d);
            }
        }
    }

    private static void section(GuardianWaveForm form, float age, int rail, float t, Vector3f left, Vector3f right) {
        float lag = 1 - t;
        if (form.turns != 0) {
            boolean sword = form == GuardianWaveForm.FERIN;
            float dt = lag * Math.min(age, sword ? 2.35F : 2.85F);
            float y = sword ? (rail == 0 ? 12.3F : 6.25F) : 20.1F;
            float x = sword ? .32F : (rail == 0 ? 4.54F : -4.54F);
            float width = sword ? (rail == 0 ? 4.45F : 3.35F) : 2.08F + .68F * lag;
            left.set(x, y - width, .08F);
            right.set(x, y + width, .08F);
            spinningPoint(left, form, age, dt);
            spinningPoint(right, form, age, dt);
        } else if (form == GuardianWaveForm.JULIUS) {
            float distance = Math.min(age * .80F, 2.24F) * lag;
            float y = 30.18F - distance / form.scale;
            float shoulder = Math.min(1, distance / .91F);
            float width = shoulder * (rail == 0 ? 0 : rail == 1 ? -2.95F : 2.95F);
            float drift = Math.max(0, distance - .91F);
            float half = .22F + .55F * shoulder + .38F * drift;
            float face = rail == 0 ? .81F : .43F;
            left.set(width - half, y, face);
            right.set(width + half, y, face);
            orient(left, form);
            orient(right, form);
        } else {
            boolean ball = form == GuardianWaveForm.SIEGRID;
            float dt = lag * Math.min(age, ball ? 2.35F : (rail % 2 == 0 ? 1.85F : 1.38F));
            float phase = (float)(2 * Math.PI * rail / (ball ? 3 : 4) + Math.PI / (ball ? 6 : 8));
            float curl = ball ? .10F * lag * (float)Math.sin(t * 8 - age * .31F + rail) : .035F * lag;
            float radius = (ball ? 3.18F : rail % 2 == 0 ? 3.81F : 3.17F) * form.scale;
            float y = ball ? 22.02F : rail % 2 == 0 ? 24.15F : 16.55F;
            float half = (ball ? .13F : .18F) * (.50F + .50F * t);
            float u = (float)Math.cos(phase), v = (float)Math.sin(phase);
            left.set(u * radius - v * (curl - half), (y - form.pivot) * form.scale, v * radius + u * (curl - half));
            right.set(u * radius - v * (curl + half), (y - form.pivot) * form.scale, v * radius + u * (curl + half));
            float dz = dt * (float)GuardianWaveEntity.SPEED / DISPLAY_SCALE;
            left.rotateY(WEAPON_TWIST * RAD).rotateX(90 * RAD).sub(0, 0, dz);
            right.rotateY(WEAPON_TWIST * RAD).rotateX(90 * RAD).sub(0, 0, dz);
        }
    }

    private static void orient(Vector3f p, GuardianWaveForm form) {
        p.sub(0, form.pivot, 0).mul(form.scale).rotateY(WEAPON_TWIST * RAD).rotateX(90 * RAD);
    }

    private static void spinningPoint(Vector3f p, GuardianWaveForm form, float age, float dt) {
        orient(p, form);
        p.rotateY(form.angle(age - dt, GuardianWaveEntity.LIFETIME) * RAD).rotateX(SPIN_CANT * RAD);
        p.z -= dt * (float)GuardianWaveEntity.SPEED / DISPLAY_SCALE;
    }

    private static void vertex(VertexConsumer out, Matrix4f pose, Vector3f p, float u, float v, int alpha) {
        out.vertex(pose, p.x, p.y, p.z).color(255, 255, 255, alpha).uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0, 1, 0).endVertex();
    }
}
