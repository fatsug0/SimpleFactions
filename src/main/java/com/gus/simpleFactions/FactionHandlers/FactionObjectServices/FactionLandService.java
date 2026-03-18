package com.gus.simpleFactions.FactionHandlers.FactionObjectServices;

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector3d;
import com.gus.simpleFactions.Enums.PlayerChunkState;
import com.gus.simpleFactions.SimpleFactions;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class FactionLandService {

    private final SimpleFactions plugin;
    public FactionLandService(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    public void ClaimLand(UUID playerUUID) {
        Player player = checkPlayer(playerUUID);
        if (player == null) {
            System.out.println("Something went wrong when trying to find the player with the UUID: " + playerUUID);
            return;
        }

        Chunk chunkToClaim = player.getLocation().getChunk();

        // Check if the Chunk is in faction land (needs to be beside another claimed chunk)
        if (!claimedChunks.isEmpty() && !isChunkInLand(chunkToClaim)) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You have to claim a chunk beside another claimed chunk !");
            return;
        }

        // Can only claim in the overworld
        if (!Objects.equals(Bukkit.getWorld("world"), player.getWorld())){
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You can only claim land in the overworld !");
            return;
        }


        // Check if the player is claiming in a valid area (not already claimed)
        if (factionManager.linkedChunks.containsKey(chunkToClaim)) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The chunk you are trying to claim is already claimed (by your faction or another) !");
            return;
        }

        boolean weakClaim = false;
        if (getClaimedChunks().size() < getPower()) {

            // Link Chunk to Faction
            getClaimedChunks().add(chunkToClaim);

        } else if (getWeakChunks().size() <= getMaxWeakChunks()) {
            if (!plugin.getConfig().getBoolean("faction.object.weak-claims-enabled")) return;

            // Link Raidable Chunk to Faction
            getWeakChunks().add(chunkToClaim);
            weakClaim = true;

        } else {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Your faction is out of chunks to claim !");
            return;
        }



        // Add Chunk in FactionManager claimedChunkCache (claimed or raidable doesn't matter)
        factionManager.linkedChunks.put(chunkToClaim, this);

        // Change player state
        if (weakClaim){
            factionManager.playerInProtectedChunks.put(playerUUID, PlayerChunkState.WEAK);
        } else {
            factionManager.playerInProtectedChunks.put(playerUUID, PlayerChunkState.HARD);
        }

        if (USE_BLUEMAP_ADDON) {
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
                        .detail(factionName + " claimed WEAK land")
                        .minDistance(10)
                        .maxDistance(10000)
                        .label(factionName + " claimed WEAK land")
                        .position(new Vector3d(chunkToClaim.getX() * 16, 64, chunkToClaim.getZ() * 16))
                        .build();
            } else {
                shapeMarker = ShapeMarker.builder()
                        .shape(new Shape(points), 64)
                        .depthTestEnabled(false)
                        .lineWidth(0)
                        .fillColor(new Color("#7f7f7f"))
                        .detail(factionName + " claimed FULL land")
                        .minDistance(10)
                        .maxDistance(10000)
                        .label(factionName + " claimed FULL land")
                        .position(new Vector3d(chunkToClaim.getX() * 16, 64, chunkToClaim.getZ() * 16))
                        .build();
            }

            // FOR INFO, is use markerSet.getMarkers().size() as the ID of the ShapeMarker
            // Add the ShapeMarker ID in the related Hashmap (to eb able to remove it later more easily)
            factionManager.bluemapClaimedChunk.put(chunkToClaim, markerSet.getMarkers().size());

            // Put the new ShapeMarker in the MarkerSet
            markerSet.getMarkers().put("claimedLand " + markerSet.getMarkers().size(), shapeMarker);

            // Redraw all the marker on the map
            System.out.println(BlueMapAPI.getInstance().isPresent());
            BlueMapAPI.onEnable(api -> {
                api.getWorld(Bukkit.getWorld("world")).ifPresent(world -> {
                    for (BlueMapMap map : world.getMaps()) {
                        map.getMarkerSets().put("FactionMarkerSet" + factionName + " claims", markerSet);
                    }
                });
            });
        }
        drawFactionOutsideLine();

        player.sendMessage(ChatColor.GREEN + "You have, claimed this chunk for your faction !");
    }

    public void UnClaimLand(UUID playerUUID) {
        Player player = checkPlayer(playerUUID);
        if (player == null) {
            System.out.println("Something went wrong when trying to find the player with the UUID: " + playerUUID);
            return;
        }

        Chunk chunkToCheck = player.getLocation().getChunk();

        // Can only unclaim in the overworld
        if (!Objects.equals(Bukkit.getWorld("world"), player.getWorld())) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You can only unclaim land in the overworld !");
            return;
        }

        // Check if the player is unclaiming in a valid area (standing on a claimed chunk and claimed by his faction)
        if (!getClaimedChunks().contains(chunkToCheck)) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The chunk you are trying to unclaim is not claimed by your faction !");
            return;
        }

        // Remove Chunk from the FactionManagers claimedChunkCache
        factionManager.linkedChunks.remove(chunkToCheck);

        // Change player state
        factionManager.playerInProtectedChunks.put(playerUUID, PlayerChunkState.WILDERNESS);

        // Unlink Chunk to Faction
        claimedChunks.remove(chunkToCheck);

        if (USE_BLUEMAP_ADDON) {

            // Remove the ShapeMarker from the MarkerSet
            markerSet.remove("claimedLand " + factionManager.bluemapClaimedChunk.get(chunkToCheck));

            // Remove the ShapeMarker from the related Hashmap
            factionManager.bluemapClaimedChunk.remove(chunkToCheck);

            try {
                // Redraw all the marker on the map
                BlueMapAPI.onEnable(api -> {
                    api.getWorld(Bukkit.getWorld("world")).ifPresent(world -> {
                        for (BlueMapMap map : world.getMaps()) {
                            map.getMarkerSets().put("FactionMarkerSet" + factionName + " claims", markerSet);
                        }
                    });
                });
            } catch (Exception e) {
                plugin.getLogger().severe("BlueMap integration failed: " + e.getMessage());
            }
        }
        player.sendMessage(ChatColor.GREEN + "You have, unclaimed this chunk for your faction !");
    }
}
