package dev.purifiedundead.client.echo;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Rotation-only Bedrock clip exported from the articulated Wine Fox rig. */
final class FerinComboCut {
    private record Key(float time, float x, float y, float z) {}
    private record Track(String name, List<Key> keys) {}
    private final List<Track> tracks;

    private FerinComboCut(List<Track> tracks) { this.tracks = tracks; }

    static FerinComboCut load(ResourceManager resources, String combo) {
        var path = ResourceLocation.tryParse("purified_undead:animations/ferin_" + combo + ".animation.json");
        try (var reader = resources.openAsReader(path)) {
            var bones = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("animations")
                    .getAsJsonObject("animation.ferin." + combo).getAsJsonObject("bones");
            List<Track> tracks = new ArrayList<>();
            for (var entry : bones.entrySet()) {
                var keys = new ArrayList<Key>();
                for (var frame : entry.getValue().getAsJsonObject().getAsJsonObject("rotation").entrySet()) {
                    float time = Float.parseFloat(frame.getKey());
                    var v = frame.getValue().getAsJsonArray();
                    // TLM converts Bedrock degrees to local renderer radians by negating X and Y.
                    var key = new Key(time, -radians(v.get(0).getAsFloat()), -radians(v.get(1).getAsFloat()),
                            radians(v.get(2).getAsFloat()));
                    if (!Float.isFinite(time) || time < 0 || !Float.isFinite(key.x)
                            || !Float.isFinite(key.y) || !Float.isFinite(key.z))
                        throw new IllegalArgumentException("Invalid rotation on " + entry.getKey());
                    keys.add(key);
                }
                keys.sort(Comparator.comparingDouble(Key::time));
                if (keys.isEmpty()) throw new IllegalArgumentException("Missing keys on " + entry.getKey());
                tracks.add(new Track(entry.getKey(), List.copyOf(keys)));
            }
            return new FerinComboCut(List.copyOf(tracks));
        } catch (IOException | RuntimeException e) {
            LogUtils.getLogger().warn("Could not read Ferin animation {}", combo, e);
            return new FerinComboCut(List.of());
        }
    }

    private static float radians(float degrees) { return (float)Math.toRadians(degrees); }

    boolean available() { return !tracks.isEmpty(); }

    static float ledAge(float releaseTicks) {
        // Retain the first cross-body guard at t=0, then lead the launch mesh by up to 8 ms.
        return releaseTicks + .16F * Math.min(1, releaseTicks / .35F);
    }

    void sample(float ledTicks, EchoAnimations.Rotations pose) {
        float seconds = ledTicks / 20;
        for (var track : tracks) {
            var keys = track.keys;
            Key a = keys.get(0), b = a;
            for (var next : keys) {
                b = next;
                if (b.time >= seconds) break;
                a = b;
            }
            float u = b.time > a.time ? Math.max(0, (seconds - a.time) / (b.time - a.time)) : 0;
            pose.rotate(track.name, a.x + (b.x - a.x) * u, a.y + (b.y - a.y) * u, a.z + (b.z - a.z) * u);
        }
    }
}
