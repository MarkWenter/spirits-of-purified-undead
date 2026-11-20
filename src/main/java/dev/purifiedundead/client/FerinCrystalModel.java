package dev.purifiedundead.client;

import com.google.gson.Gson;
import dev.purifiedundead.combat.FerinBladeTrajectory;
import dev.purifiedundead.entity.FerinSlashMesh.Quad;
import dev.purifiedundead.entity.FerinSlashMesh.Vertex;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec3;

/** Blockbench mesh baked into angular, radial and normal coordinates of the historical sweep. */
public final class FerinCrystalModel {
    private record Face(String layer, int facet, double[][] points) {}
    private record Model(Face[] faces, double[][] scale) {}
    private static final Model MODEL = load();
    private static final int[][] EDGE = {{255,204,163},{255,246,215},{255,142,142},{255,172,138}};
    private static final int[][] BODY = {{225,71,240},{255,126,229},{141,40,210},{186,48,232}};

    private static Model load() {
        try (var reader = new InputStreamReader(FerinCrystalModel.class.getResourceAsStream(
                "/assets/purified_undead/models/effect/ferin_crystal.json"), StandardCharsets.UTF_8)) {
            return new Gson().fromJson(reader, Model.class);
        } catch (java.io.IOException e) { throw new IllegalStateException(e); }
    }

    private static Vec3 tip(int stage, double t) {
        var p = FerinBladeTrajectory.blade(stage, t, 0).tip();
        return new Vec3(p.right(), p.up()-1.05, p.forward());
    }

    public static List<Quad> sample(int stage, double age, int life) {
        double forming = Math.min(life, FerinBladeTrajectory.sweepEnd(stage)-1);
        double progress = Math.min(1, age/forming);
        double fade = Math.min(1, Math.min(age*2, (life+1-age)/2));
        var keys = MODEL.scale;
        double t = age % keys[2][0];
        int i = t < keys[1][0] ? 0 : 1;
        double pulse = keys[i][1] + (keys[i+1][1]-keys[i][1])*(t-keys[i][0])/(keys[i+1][0]-keys[i][0]);
        var quads = new ArrayList<Quad>(MODEL.faces.length);
        for (Face face : MODEL.faces) {
            boolean edge = face.layer.equals("cutting_edge");
            int[] rgb = (edge ? EDGE : BODY)[face.facet];
            double scale = face.layer.startsWith("shard") ? pulse : 1;
            Vertex[] v = new Vertex[4];
            for (int j=0;j<4;j++) {
                double[] p = face.points[j];
                double u = p[0]*progress;
                Vec3 radial = tip(stage,u);
                Vec3 tangent = tip(stage,Math.min(1,u+.001)).subtract(tip(stage,Math.max(0,u-.001)));
                double radius = p[1] * scale;
                if (stage >= 4) radius = 1 - (1-radius)*.42;
                Vec3 point = radial.scale(radius).add(radial.cross(tangent).normalize()
                        .scale((p[2]+.025)*FerinBladeTrajectory.reach(stage)));
                double weight = stage <= 2 ? .4+.6*p[0] : 1;
                v[j]=new Vertex(point.x,point.y+1.05,point.z,rgb[0],rgb[1],rgb[2],
                        (int)((edge ? 238 : 172)*fade*weight));
            }
            quads.add(new Quad(v[0],v[1],v[2],v[3]));
        }
        return quads;
    }
}
