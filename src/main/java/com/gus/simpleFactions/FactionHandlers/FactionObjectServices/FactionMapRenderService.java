package com.gus.simpleFactions.FactionHandlers.FactionObjectServices;

import com.flowpowered.math.vector.Vector3d;
import com.gus.simpleFactions.SimpleFactions;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.LineMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Line;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

public class FactionMapRenderService {

    private final SimpleFactions plugin;
    public FactionMapRenderService(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    private void createLineMarker(double x1, double z1, double x2, double z2, int id) {
        Line borderLine = new Line(
                new Vector3d(x1, 64, z1),
                new Vector3d(x2, 64, z2)
        );

        LineMarker lineMarker = LineMarker.builder()
                .label(factionName + " Border")
                .line(borderLine) // Pass the Line object here
                .lineWidth(3)
                .lineColor(new Color(255, 255, 255, 1f))
                .depthTestEnabled(false)
                .minDistance(10)
                .maxDistance(10000)
                .build();

        markerSet.put(factionName + "_border_" + id, lineMarker);
    }

    public void drawFactionOutsideLine(){
        World world = Bukkit.getWorld("world");
        if (world == null) return;

        // Remove all previous border markers to start fresh
        markerSet.getMarkers().keySet().removeIf(key -> key.startsWith(factionName + "_border_"));

        // Just to keep track of each border (with ID's)
        int lineIdCount = 0;

        // Check each chunk of the faction
        for (Chunk chunk : getClaimedChunks()) {

            // Get the upper left corner
            int x0 = chunk.getX() * 16;
            int z0 = chunk.getZ() * 16;

            // Get the lower right corner
            int x1 = x0 + 16;
            int z1 = z0 + 16;

            // Define the 4 edges of the chunk
            // North: (x0, z0) to (x1, z0)
            if (!getClaimedChunks().contains(world.getChunkAt(chunk.getX(), chunk.getZ() - 1))) {
                createLineMarker(x0, z0, x1, z0, lineIdCount++);
            }

            // South: (x0, z1) to (x1, z1)
            if (!this.getClaimedChunks().contains(world.getChunkAt(chunk.getX(), chunk.getZ() + 1))) {
                createLineMarker(x0, z1, x1, z1, lineIdCount++);
            }

            // West: (x0, z0) to (x0, z1)
            if (!this.getClaimedChunks().contains(world.getChunkAt(chunk.getX() - 1, chunk.getZ()))) {
                createLineMarker(x0, z0, x0, z1, lineIdCount++);
            }

            // East: (x1, z0) to (x1, z1)
            if (!this.getClaimedChunks().contains(world.getChunkAt(chunk.getX() + 1, chunk.getZ()))) {
                createLineMarker(x1, z0, x1, z1, lineIdCount++);
            }
        }

        try {
            // Redraw all the markers on the map
            BlueMapAPI.onEnable(api -> {
                api.getWorld(Bukkit.getWorld("world")).ifPresent(blueWorld -> {
                    for (BlueMapMap map : blueWorld.getMaps()) {
                        map.getMarkerSets().put("my-marker-set-id", markerSet);
                    }
                });
            });
        } catch (Exception e) {
            plugin.getLogger().severe("BlueMap integration failed: " + e.getMessage());
        }
    }

}
