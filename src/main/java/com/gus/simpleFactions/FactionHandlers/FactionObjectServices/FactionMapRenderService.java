package com.gus.simpleFactions.FactionHandlers.FactionObjectServices;

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector3d;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.SimpleFactions;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.LineMarker;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Line;
import de.bluecolored.bluemap.api.math.Shape;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FactionMapRenderService {

    private final SimpleFactions plugin;
    public FactionMapRenderService(SimpleFactions plugin) {
        this.plugin = plugin;
        USE_BLUEMAP_ADDON = plugin.getConfig().getBoolean("enable-bluemap-addon");
    }

    private Map<Chunk, Integer> bluemapClaimedChunk = new HashMap<>();
    public Map<Chunk, Integer> getBluemapClaimedChunk() {
        return bluemapClaimedChunk;
    }
    public void addBlueMapChunk(Chunk chunk, int id) {
        bluemapClaimedChunk.put(chunk, id);
    }
    public void removeBlueMapChunk(Chunk chunk) {
        bluemapClaimedChunk.remove(chunk);
    }
    public Map<String, Integer> getWrappedBluemapClaimedChunk() {
        Map<String, Integer> returnMap = new HashMap<>();
        for (Map.Entry<Chunk, Integer> entry : bluemapClaimedChunk.entrySet()) {
            returnMap.put(chunkKey(entry.getKey().getX(), entry.getKey().getZ()), entry.getValue());
        }
        return returnMap;
    }
    public void unWrapBluemapClaimedChunk(Map<String, ? extends Number> wrappedBluemapClaimedChunk) {
        bluemapClaimedChunk.clear();
        if (wrappedBluemapClaimedChunk == null) return;
        for (Map.Entry<String, ? extends Number> entry : wrappedBluemapClaimedChunk.entrySet()) {
            Chunk chunk = chunkFromKey(entry.getKey());
            if (chunk == null) continue;
            addBlueMapChunk(chunk, entry.getValue().intValue());
        }
    }

    private String chunkKey(int x, int z) {
        return x + "," + z;
    }

    private Chunk chunkFromKey(String key) {
        ArrayList<Double> numbers = new ArrayList<>();
        Matcher matcher = Pattern.compile("-?\\d+(?:\\.\\d+)?").matcher(key);
        while (matcher.find()) {
            numbers.add(Double.parseDouble(matcher.group()));
        }
        if (numbers.size() < 2) return null;
        if (Bukkit.getWorld("world") == null) return null;
        return Bukkit.getWorld("world").getChunkAt(numbers.get(0).intValue(), numbers.get(1).intValue());
    }

    private final boolean USE_BLUEMAP_ADDON;
    public boolean getUSE_BLUEMAP_ADDON() {
        return USE_BLUEMAP_ADDON;
    }

    public void DrawChunks(FactionObject faction, Chunk chunkToClaim, boolean weakClaim) {
        // Get the corners of the claimed chunk
        ArrayList<Vector2d> points = new ArrayList<>();
        points.add(new Vector2d(chunkToClaim.getX() * 16, chunkToClaim.getZ() * 16));
        points.add(new Vector2d(chunkToClaim.getX() * 16 + 16, chunkToClaim.getZ() * 16));
        points.add(new Vector2d(chunkToClaim.getX() * 16 + 16, chunkToClaim.getZ() * 16 + 16));
        points.add(new Vector2d(chunkToClaim.getX() * 16, chunkToClaim.getZ() * 16 + 16));

        // Create the ShapeMarker who represent the claimed chunk
        ShapeMarker shapeMarker;
        if (weakClaim) {
            shapeMarker = ShapeMarker.builder()
                    .shape(new Shape(points), 64)
                    .depthTestEnabled(false)
                    .lineWidth(0)
                    .fillColor(new Color("#7f7f7f"))
                    .detail(faction.getFactionName() + " claimed WEAK land")
                    .minDistance(10)
                    .maxDistance(10000)
                    .label(faction.getFactionName() + " claimed WEAK land")
                    .position(new Vector3d(chunkToClaim.getX() * 16, 64, chunkToClaim.getZ() * 16))
                    .build();
        } else {
            shapeMarker = ShapeMarker.builder()
                    .shape(new Shape(points), 64)
                    .depthTestEnabled(false)
                    .lineWidth(0)
                    .fillColor(new Color("#7f7f7f"))
                    .detail(faction.getFactionName() + " claimed FULL land")
                    .minDistance(10)
                    .maxDistance(10000)
                    .label(faction.getFactionName() + " claimed FULL land")
                    .position(new Vector3d(chunkToClaim.getX() * 16, 64, chunkToClaim.getZ() * 16))
                    .build();
        }

        // FOR INFO, is use markerSet.getMarkers().size() as the ID of the ShapeMarker
        // Add the ShapeMarker ID in the related Hashmap (to eb able to remove it later more easily)
        addBlueMapChunk(chunkToClaim, faction.getFactionMarkerSet().getMarkers().size());

        // Put the new ShapeMarker in the MarkerSet
        faction.getFactionMarkerSet().getMarkers().put("claimedLand " + faction.getFactionMarkerSet().getMarkers().size(), shapeMarker);

        // Redraw all the marker on the map
        BlueMapAPI.onEnable(api -> {
            api.getWorld(Bukkit.getWorld("world")).ifPresent(world -> {
                for (BlueMapMap map : world.getMaps()) {
                    map.getMarkerSets().put("FactionMarkerSet" + faction.getFactionName() + " claims", faction.getFactionMarkerSet());
                }
            });
        });

        drawFactionOutsideLine(faction);
    }

    public void RemoveChunks(FactionObject faction, Chunk chunkToUnclaim) {
        // Remove the ShapeMarker from the MarkerSet
        faction.getFactionMarkerSet().remove("claimedLand " + getBluemapClaimedChunk().get(chunkToUnclaim));

        // Remove the ShapeMarker from the related Hashmap
        removeBlueMapChunk(chunkToUnclaim);

        try {
            // Redraw all the marker on the map
            BlueMapAPI.onEnable(api -> {
                api.getWorld(Bukkit.getWorld("world")).ifPresent(world -> {
                    for (BlueMapMap map : world.getMaps()) {
                        map.getMarkerSets().put("FactionMarkerSet" + faction.getFactionName() + " claims", faction.getFactionMarkerSet());
                    }
                });
            });
        } catch (Exception e) {
            plugin.getLogger().severe("BlueMap integration failed: " + e.getMessage());
        }
    }
    private void createLineMarker(FactionObject faction, double x1, double z1, double x2, double z2, int id) {
        Line borderLine = new Line(
                new Vector3d(x1, 64, z1),
                new Vector3d(x2, 64, z2)
        );

        LineMarker lineMarker = LineMarker.builder()
                .label(faction.getFactionName() + " Border")
                .line(borderLine) // Pass the Line object here
                .lineWidth(3)
                .lineColor(new Color(255, 255, 255, 1f))
                .depthTestEnabled(false)
                .minDistance(10)
                .maxDistance(10000)
                .build();

        faction.getFactionMarkerSet().put(faction.getFactionName() + "_border_" + id, lineMarker);
    }

    public void drawFactionOutsideLine(FactionObject faction){
        World world = Bukkit.getWorld("world");
        if (world == null) return;

        // Remove all previous border markers to start fresh
        faction.getFactionMarkerSet().getMarkers().keySet().removeIf(key -> key.startsWith(faction.getFactionName() + "_border_"));

        // Just to keep track of each border (with ID's)
        int lineIdCount = 0;

        // Check each chunk of the faction
        for (Chunk chunk : faction.getHardClaimedChunks()) {

            // Get the upper left corner
            int x0 = chunk.getX() * 16;
            int z0 = chunk.getZ() * 16;

            // Get the lower right corner
            int x1 = x0 + 16;
            int z1 = z0 + 16;

            // Define the 4 edges of the chunk
            // North: (x0, z0) to (x1, z0)
            if (!faction.getHardClaimedChunks().contains(world.getChunkAt(chunk.getX(), chunk.getZ() - 1))) {
                createLineMarker(faction, x0, z0, x1, z0, lineIdCount++);
            }

            // South: (x0, z1) to (x1, z1)
            if (!faction.getHardClaimedChunks().contains(world.getChunkAt(chunk.getX(), chunk.getZ() + 1))) {
                createLineMarker(faction, x0, z1, x1, z1, lineIdCount++);
            }

            // West: (x0, z0) to (x0, z1)
            if (!faction.getHardClaimedChunks().contains(world.getChunkAt(chunk.getX() - 1, chunk.getZ()))) {
                createLineMarker(faction, x0, z0, x0, z1, lineIdCount++);
            }

            // East: (x1, z0) to (x1, z1)
            if (!faction.getHardClaimedChunks().contains(world.getChunkAt(chunk.getX() + 1, chunk.getZ()))) {
                createLineMarker(faction, x1, z0, x1, z1, lineIdCount++);
            }
        }

        try {
            // Redraw all the markers on the map
            BlueMapAPI.onEnable(api -> {
                api.getWorld(Bukkit.getWorld("world")).ifPresent(blueWorld -> {
                    for (BlueMapMap map : blueWorld.getMaps()) {
                        map.getMarkerSets().put("my-marker-set-id", faction.getFactionMarkerSet());
                    }
                });
            });
        } catch (Exception e) {
            plugin.getLogger().severe("BlueMap integration failed: " + e.getMessage());
        }
    }

}
