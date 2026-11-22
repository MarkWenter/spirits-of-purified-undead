package dev.purifiedundead.client.echo;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Bedrock joint rotations and optional vertical root motion exported from the articulated Wine Fox rig. */
final class FerinComboCut {
    private record Key(float time, float x, float y, float z) {}
    private record Track(String name, boolean rootHeight, List<Key> keys) {}
    private final List<Track> tracks;

    private FerinComboCut(List<Track> tracks) { this.tracks = tracks; }

    static FerinComboCut load(ResourceManager resources, String combo) {
        var path = ResourceLocation.tryParse("purified_undead:animations/ferin_" + combo + ".animation.json");
        try (var reader = resources.openAsReader(path)) {
            return read(reader, combo);
        } catch (IOException | RuntimeException e) {
            LogUtils.getLogger().warn("Could not read Ferin animation {}", combo, e);
            return new FerinComboCut(List.of());
        }
    }

    static FerinComboCut read(java.io.Reader reader, String combo) {
        var bones = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("animations")
                .getAsJsonObject("animation.ferin." + combo).getAsJsonObject("bones");
        List<Track> tracks = new ArrayList<>();
        for (var entry : bones.entrySet()) {
            var bone = entry.getValue().getAsJsonObject();
            String name = entry.getKey();
            boolean lift = name.equals("MRoot") && bone.has("position");
            if (lift) tracks.add(readTrack(name, bone.getAsJsonObject("position"), true));
            if (!lift || bone.has("rotation"))
                tracks.add(readTrack(name, bone.getAsJsonObject("rotation"), false));
        }
        return new FerinComboCut(List.copyOf(tracks));
    }

    private static Track readTrack(String name, JsonObject frames, boolean rootHeight) {
        var keys = new ArrayList<Key>();
        for (var frame : frames.entrySet()) {
            float time = Float.parseFloat(frame.getKey());
            var v = frame.getValue().getAsJsonArray();
            // TLM converts Bedrock degrees to local renderer radians by negating X and Y.
            var key = rootHeight ? new Key(time, 0, v.get(1).getAsFloat(), 0)
                    : new Key(time, -radians(v.get(0).getAsFloat()), -radians(v.get(1).getAsFloat()),
                    radians(v.get(2).getAsFloat()));
            if (!Float.isFinite(time) || time < 0 || !Float.isFinite(key.x)
                    || !Float.isFinite(key.y) || (rootHeight && Math.abs(key.y) > 64) || !Float.isFinite(key.z))
                throw new IllegalArgumentException("Invalid key on " + name);
            keys.add(key);
        }
        keys.sort(Comparator.comparingDouble(Key::time));
        if (keys.isEmpty()) throw new IllegalArgumentException("Missing keys on " + name);
        return new Track(name, rootHeight, List.copyOf(keys));
    }

    private static float radians(float degrees) { return (float)Math.toRadians(degrees); }

    boolean available() { return !tracks.isEmpty(); }

    static float ledAge(float releaseTicks) {
        // Retain the first cross-body guard at t=0, then lead the launch mesh by up to 8 ms.
        return releaseTicks + .16F * Math.min(1, releaseTicks / .35F);
    }

    float sample(float ledTicks, EchoAnimations.Rotations pose) {
        float seconds = ledTicks / 20;
        float height = 0;
        for (var track : tracks) {
            var keys = track.keys;
            int low = 0, high = keys.size() - 1;
            while (low < high) {
                int mid = (low + high) >>> 1;
                if (keys.get(mid).time < seconds) low = mid + 1;
                else high = mid;
            }
            Key a = keys.get(Math.max(0, low - 1)), b = keys.get(low);
            float u = b.time > a.time ? Math.max(0, Math.min(1, (seconds - a.time) / (b.time - a.time))) : 0;
            float y = a.y + (b.y - a.y) * u;
            if (track.rootHeight) height = y;
            else pose.rotate(track.name, a.x + (b.x - a.x) * u, y, a.z + (b.z - a.z) * u);
        }
        return height;
    }
}
