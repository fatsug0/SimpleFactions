package com.gus.simpleFactions.Json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.gus.simpleFactions.Enums.PlayerChunkState;
import com.gus.simpleFactions.FactionHandlers.FactionObjectServices.FactionMembershipService;
import com.gus.simpleFactions.SimpleFactions;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class JsonHandler {

    private final SimpleFactions plugin;
    public JsonHandler(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void CreateJsonFile(String fileName) {
        try {
            plugin.getDataFolder().mkdirs();
            Path dataPath = Path.of(plugin.getDataFolder() + "/data");
            if (!Files.exists(dataPath)) Files.createDirectories(dataPath);

            Path filePath = Path.of(plugin.getDataFolder() + "/data/" + fileName + ".json");
            if (!Files.exists(filePath)) Files.createFile(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    public void DeleteJsonFile(String fileName) {
//        File file = new File(plugin.getDataFolder() + "/data/" + fileName + ".json");
//        if (file.exists()) file.delete();
//    }

    public <T> HashMap<String, T> ReadWholeJson(String fileName) {
        try {
            File file = new File(plugin.getDataFolder() + "/data/" + fileName + ".json");
            if (!file.exists()) return null;

            HashMap readData;
            try (Reader reader = new FileReader(file)) {
                readData = gson.fromJson(reader, HashMap.class);
            }
            if (readData == null) return null;
            return (HashMap<String, T>) readData;

        } catch (JsonParseException e) {
            plugin.getLogger().warning("Could not parse " + fileName + ".json. It will be treated as empty: " + e.getMessage());
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T ReadJson(String fileName, String dataName, Type readType) {
        try {
            File file = new File(plugin.getDataFolder() + "/data/" + fileName + ".json");
            if (!file.exists()) return null;

            HashMap readData;
            try (Reader reader = new FileReader(file)) {
                readData = gson.fromJson(reader, HashMap.class); // The file is globally structured in a HashMap
            }

            if (readData == null) return null; // File is empty
            if (!readData.containsKey(dataName)) return null; // File doesn't contain the dataName

            return gson.fromJson(gson.toJson(readData.get(dataName)), readType);

        } catch (JsonParseException e) {
            plugin.getLogger().warning("Could not read " + dataName + " from " + fileName + ".json: " + e.getMessage());
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> void WriteJson(String fileName, String dataName, T writeData) {
        try {
            File file = new File(plugin.getDataFolder() + "/data/" + fileName + ".json");
            if (!file.exists()) CreateJsonFile(fileName);

            HashMap<String, T> read = ReadWholeJson(fileName);
            if (read == null) read = new HashMap<>();

            read.put(dataName, writeData);
            try (Writer writer = new FileWriter(file, false)) {
                gson.toJson(read, writer);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void LoadSequence() {
        // Load Faction Membership Service
        plugin.factionManager.factionMembershipService.unWrapExistingFaction(ReadJson("faction-membership-service", "existingFactions", new TypeToken<ArrayList<FactionObjectWrapper>>() {}.getType()));
        plugin.factionManager.factionMembershipService.unWrapPlayerFactionLink(ReadJson("faction-membership-service", "playerFactionLink", new TypeToken<Map<String, String>>() {}.getType()));
        plugin.factionManager.factionMembershipService.unWrapPendingFactionInvites(ReadJson("faction-membership-service", "pendingFactionInvites", new TypeToken<ArrayList<FactionMembershipService.FactionInviteWrapper>>() {}.getType()));

        // Load Faction Land Service
        plugin.factionManager.factionLandService.unWrapLinkedChunks(ReadJson("land-service", "linkedChunks", new TypeToken<Map<String, String>>() {}.getType()));
        plugin.factionManager.factionLandService.unWrapPlayerChunkStateStrings(ReadJson("land-service", "playerChunkState", new TypeToken<Map<String, PlayerChunkState>>() {}.getType()));

        // Load Map Render Service
        plugin.factionManager.factionMapRenderService.unWrapBluemapClaimedChunk(ReadJson("map-render-service", "bluemapClaimedChunk", new TypeToken<Map<String, Double>>() {}.getType()));

        // Load Faction Rank Service
        plugin.factionManager.factionRankService.unWrapPerms(ReadJson("faction-rank-service", "perms", new TypeToken<Map<String, ArrayList<Map<String, Boolean>>>>() {}.getType()));
    }

    public void SaveSequence() {
        WriteJson("faction-membership-service", "existingFactions", plugin.factionManager.factionMembershipService.getWrappedExistingFaction());
        WriteJson("faction-membership-service", "playerFactionLink", plugin.factionManager.factionMembershipService.getWrappedPlayerFactionLink());
        WriteJson("faction-membership-service", "pendingFactionInvites", plugin.factionManager.factionMembershipService.getWrappedPendingFactionInvites());

        // Save Faction Land Service
        WriteJson("land-service", "linkedChunks", plugin.factionManager.factionLandService.getWrappedLinkedChunks());
        WriteJson("land-service", "playerChunkState", plugin.factionManager.factionLandService.getWrappedPlayerChunkState());

        // Save Map Render Service
        WriteJson("map-render-service", "bluemapClaimedChunk", plugin.factionManager.factionMapRenderService.getWrappedBluemapClaimedChunk());

        // PermissionAttachment objects are runtime Bukkit state; rank permissions are persisted inside factions.
        WriteJson("faction-rank-service", "perms", new HashMap<String, ArrayList<Map<String, Boolean>>>());
    }
}
