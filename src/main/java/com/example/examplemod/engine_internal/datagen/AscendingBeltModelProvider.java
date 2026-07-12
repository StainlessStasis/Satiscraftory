package com.example.examplemod.engine_internal.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * Derives the ascending-belt model from the straight-belt model instead of hand-authoring
 * a separate asset: takes the straight model's elements as-is and adds a 45-degree rotation
 * with rescale=true - the same mechanism vanilla's own sloped rail models use - so the incline
 * is guaranteed to share the straight model's exact geometry/texture and can't drift out of
 * alignment at the seams the way two independently modeled assets can.
 */
public class AscendingBeltModelProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String STRAIGHT_MODEL_PATH = "block/belt_straight";
    private static final String OUTPUT_MODEL_PATH = "block/belt_ascending";
    // This base model represents "ascends toward south" (climbs away from the north edge) -
    // matches ASCENDING_SOUTH getting zero extra y-rotation in your blockstate, same as before.
    // Rotating around X tilts the Z extent, which is the correct axis for a north/south climb.
    private static final String ROTATION_AXIS = "x";
    private static final int PIVOT_X = 8;   // block center, in 0-16 model space
    private static final int PIVOT_Y = 8;   // floor height - the low end sits at y=0
    private static final int PIVOT_Z = 0;   // north edge - the low end of the incline

    private final PackOutput output;
    private final String modId;

    public AscendingBeltModelProvider(PackOutput output, String modId) {
        this.output = output;
        this.modId = modId;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        JsonObject straightModel = loadStraightModel();

        if (!straightModel.has("textures") || straightModel.getAsJsonObject("textures").isEmpty()) {
            System.err.println("[AscendingBeltModelProvider] WARNING: " + STRAIGHT_MODEL_PATH
                    + " has no \"textures\" block of its own - if it relies entirely on an inherited "
                    + "\"parent\" for textures, this derived model inherits the same parent reference "
                    + "too (deepCopy preserves \"parent\"), so that should still resolve - but worth "
                    + "double-checking the parent chain actually declares real texture paths somewhere.");
        }

        JsonObject ascendingModel = deriveAscendingModel(straightModel);

        Path targetPath = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                .resolve(modId).resolve("models").resolve(OUTPUT_MODEL_PATH + ".json");

        System.out.println("[AscendingBeltModelProvider] Writing " + targetPath.toAbsolutePath());
        System.out.println("[AscendingBeltModelProvider] Generated model content:\n" + GSON.toJson(ascendingModel));

        return DataProvider.saveStable(cache, GSON.toJsonTree(ascendingModel), targetPath);
    }

    @Override
    public @NonNull String getName() {
        return "AscendingBeltModelProvier";
    }

    private JsonObject loadStraightModel() {
        String resourcePath = "assets/" + modId + "/models/" + STRAIGHT_MODEL_PATH + ".json";
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalStateException("Could not find straight belt model at " + resourcePath);
            }
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read straight belt model", e);
        }
    }

    private JsonObject deriveAscendingModel(JsonObject straightModel) {
        JsonObject result = straightModel.deepCopy();
        JsonArray elements = result.getAsJsonArray("elements");
        if (elements == null) {
            throw new IllegalStateException(STRAIGHT_MODEL_PATH + " has no \"elements\" array - if it "
                    + "only has a \"parent\" reference, re-export it from Blockbench as a standalone "
                    + "model with real elements first, so there's actual geometry here to transform.");
        }

        for (var elementJson : elements) {
            JsonObject element = elementJson.getAsJsonObject();
            JsonObject rotation = new JsonObject();
            JsonArray origin = new JsonArray();
            origin.add(PIVOT_X);
            origin.add(PIVOT_Y);
            origin.add(PIVOT_Z);
            rotation.add("origin", origin);
            rotation.addProperty("axis", ROTATION_AXIS);
            rotation.addProperty("angle", -45);
            rotation.addProperty("rescale", true);
            element.add("rotation", rotation);
        }
        return result;
    }
}