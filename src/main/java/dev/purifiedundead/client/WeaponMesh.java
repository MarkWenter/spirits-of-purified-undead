package dev.purifiedundead.client;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import org.joml.Vector3f;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Client-side quad data shared by the Guardian wave and the optional maid's sword layer. */
public final class WeaponMesh {
    // The largest bundled mesh has 580 quads and uses just over 100 KiB.
    static final int MAX_BYTES = 512 * 1024;
    static final int MAX_FACES = 2048;

    public record Face(float[][] vertices, Vector3f normal) {}

    private WeaponMesh() {}

    public static List<Face> load(ResourceManager resources, ResourceLocation id) {
        // Minecraft orders this list from the lowest to the highest priority.
        List<Resource> versions;
        try {
            versions = resources.getResourceStack(id);
        } catch (RuntimeException failure) {
            LogUtils.getLogger().warn("Could not locate weapon mesh {}", id, failure);
            return List.of();
        }
        for (int i = versions.size() - 1; i >= 0; i--) {
            var resource = versions.get(i);
            try (var stream = resource.open()) {
                return read(stream);
            } catch (IOException | RuntimeException failure) {
                LogUtils.getLogger()
                        .warn(
                                "Skipping invalid weapon mesh {} from {}",
                                id,
                                resource.sourcePackId(),
                                failure);
            }
        }
        if (versions.isEmpty()) LogUtils.getLogger().warn("Missing weapon mesh {}", id);
        return List.of();
    }

    static List<Face> read(InputStream stream) throws IOException {
        byte[] data = stream.readNBytes(MAX_BYTES + 1);
        if (data.length > MAX_BYTES) throw new IOException("Weapon mesh exceeds 512 KiB");
        try (var reader =
                new JsonReader(
                        new InputStreamReader(
                                new ByteArrayInputStream(data), StandardCharsets.UTF_8))) {
            var result = new ArrayList<Face>();
            reader.beginArray();
            while (reader.hasNext()) {
                if (result.size() >= MAX_FACES)
                    throw new IOException("Weapon mesh exceeds 2048 quads");
                reader.beginArray();
                float[][] quad = new float[4][5];
                for (float[] vertex : quad) {
                    reader.beginArray();
                    for (int i = 0; i < vertex.length; i++) {
                        if (reader.peek() != JsonToken.NUMBER)
                            throw new IOException("Expected numeric XYZ and UV");
                        float value = (float) reader.nextDouble();
                        if (!Float.isFinite(value))
                            throw new IOException("Non-finite weapon vertex");
                        vertex[i] = value;
                    }
                    reader.endArray();
                }
                reader.endArray();
                var first = new Vector3f(quad[0]);
                var normal =
                        new Vector3f(quad[1]).sub(first).cross(new Vector3f(quad[2]).sub(first));
                float lengthSquared = normal.lengthSquared();
                if (!Float.isFinite(lengthSquared) || lengthSquared <= 0)
                    throw new IOException("Invalid weapon face normal");
                result.add(new Face(quad, normal.normalize()));
            }
            reader.endArray();
            if (reader.peek() != JsonToken.END_DOCUMENT)
                throw new IOException("Trailing weapon mesh data");
            return List.copyOf(result);
        } catch (IllegalStateException | NumberFormatException failure) {
            throw new IOException("Invalid weapon mesh structure", failure);
        }
    }
}
