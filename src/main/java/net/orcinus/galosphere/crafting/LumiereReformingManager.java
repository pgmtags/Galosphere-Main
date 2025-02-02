package net.orcinus.galosphere.crafting;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.orcinus.galosphere.Galosphere;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.NoSuchElementException;

public class LumiereReformingManager extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloadListener {
    private static final Gson GSON_INSTANCE = (new GsonBuilder()).create();
    private static final Map<Block, Block> REFORMING_TABLE = Maps.newHashMap();

    public LumiereReformingManager() {
        super(GSON_INSTANCE, "loot_tables/gameplay");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager resourceManagerIn, ProfilerFiller pProfiler) {
        ResourceLocation resourceLocation = Galosphere.id("loot_tables/gameplay/lumiere_reforming_table.json");
        try {
            for (Resource iResource : resourceManagerIn.getResourceStack(resourceLocation)) {
                try (Reader reader = new BufferedReader(new InputStreamReader(iResource.open(), StandardCharsets.UTF_8))) {
                    JsonObject jsonObject = GsonHelper.fromJson(GSON_INSTANCE, reader, JsonObject.class);
                    if (jsonObject != null) {
                        JsonArray entryList = jsonObject.get("entries").getAsJsonArray();
                        for (JsonElement entry : entryList) {
                            REFORMING_TABLE.put(BuiltInRegistries.BLOCK.get(new ResourceLocation(entry.getAsJsonObject().get("accepted_block").getAsString())), BuiltInRegistries.BLOCK.get(new ResourceLocation(entry.getAsJsonObject().get("returned_block").getAsString())));
                        }
                    }
                } catch (RuntimeException | IOException exception) {
                    Galosphere.LOGGER.error("Couldn't read lumiere reforming table list {} in data pack {}", resourceLocation, iResource.sourcePackId(), exception);
                }
            }
        } catch (NoSuchElementException exception) {
            Galosphere.LOGGER.error("Couldn't read lumiere reforming table from {}", resourceLocation, exception);
        }

    }

    public static Map<Block, Block> getReformingTable() {
        return REFORMING_TABLE;
    }

    @Override
    public ResourceLocation getFabricId() {
        return Galosphere.id("loot_tables/gameplay");
    }
}