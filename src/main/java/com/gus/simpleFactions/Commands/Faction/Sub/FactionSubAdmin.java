package com.gus.simpleFactions.Commands.Faction.Sub;

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector3d;
import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.Enums.PlayerChunkState;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.SimpleFactions;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class FactionSubAdmin implements CommandInterface {

    private SimpleFactions plugin;
    public FactionSubAdmin(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "admin";
    }

    @Override
    public String getDescription() {
        return """
                This is the faction plugin admin commands
                """;
    }

    @Override
    public String getPermission() {
        return "simplefactions.admin";
    }

    @Override
    public String getUsage() {
        return "/faction admin <options>";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length <= 1 || !(sender instanceof Player player)) {
            sender.sendMessage(sendUsageError());
            return;
        }

        if (getPermission() != null && !player.hasPermission(getPermission())) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "vanish":
                break;

            case "claim":
                if (args.length != 3) return;
                for (FactionObject factionObject : plugin.factionManager.factionMembershipService.getExistingFactions()){
                    if (factionObject.getFactionName().equals(args[1])) {
                        ForceClaim(factionObject, Bukkit.getWorld("world").getChunkAt(Integer.parseInt(args[2]), Integer.parseInt(args[2].substring(2))));
                        return;
                    }
                }
                break;

            case "unclaim":
                if (args.length != 3) return;
                for (FactionObject factionObject : plugin.factionManager.factionMembershipService.getExistingFactions()){
                    if (factionObject.getFactionName().equals(args[1])) {
                        ForceUnClaim(factionObject, Bukkit.getWorld("world").getChunkAt(Integer.parseInt(args[2]), Integer.parseInt(args[2].substring(2))));
                        return;
                    }
                }
                break;

            case "power":
                if (args.length != 3) return;
                for (FactionObject factionObject : plugin.factionManager.factionMembershipService.getExistingFactions()){
                    if (factionObject.getFactionName().equals(args[1])) {
                        // Check if a string is an int
//                        if (Integer.parseInt(args[2]) )
                        factionObject.setPower(Integer.parseInt(args[2]));
                    }
                }
                break;
        }
    }

    private void ForceClaim(FactionObject factionObject, Chunk chunkToClaim) {
        // Can only claim in the overworld
        if (!Objects.equals(Bukkit.getWorld("world"), chunkToClaim.getWorld())){
            return;
        }

        // If the chunk is already claimed, force unclaim it for them and claim it for the new faction
        if (plugin.factionManager.factionLandService.getLinkedChunks().containsKey(chunkToClaim)) {
            ForceUnClaim(plugin.factionManager.factionLandService.getLinkedChunks().get(chunkToClaim), chunkToClaim);
        }

        boolean weakClaim = false;
        if (factionObject.getHardClaimedChunks().size() < factionObject.getPower()) {

            // Link Chunk to Faction
            factionObject.getHardClaimedChunks().add(chunkToClaim);

        } else if (factionObject.getWeakClaimedChunks().size() <= plugin.factionManager.factionLandService.getMAX_WEAK_CHUNKS(factionObject)) {
            if (!plugin.getConfig().getBoolean("faction.object.weak-claims-enabled")) return;

            // Link Raidable Chunk to Faction
            factionObject.getWeakClaimedChunks().add(chunkToClaim);
            weakClaim = true;

        }

        // Add Chunk in FactionManager claimedChunkCache (claimed or raidable doesn't matter)
        plugin.factionManager.factionLandService.getLinkedChunks().put(chunkToClaim, factionObject);

        // Change player state
        for (Player player : Bukkit.getOnlinePlayers()){
            if (player.getLocation().getChunk().equals(chunkToClaim)){
                if (weakClaim){
                    plugin.factionManager.factionLandService.getPlayerChunkState().put(player.getUniqueId(), PlayerChunkState.WEAK);
                } else {
                    plugin.factionManager.factionLandService.getPlayerChunkState().put(player.getUniqueId(), PlayerChunkState.HARD);
                }
            }
        }

        if (plugin.getConfig().getBoolean("enable-bluemap-addon")) {
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
                        .detail(factionObject.getFactionName() + " claimed WEAK land")
                        .minDistance(10)
                        .maxDistance(10000)
                        .label(factionObject.getFactionName() + " claimed WEAK land")
                        .position(new Vector3d(chunkToClaim.getX() * 16, 64, chunkToClaim.getZ() * 16))
                        .build();
            } else {
                shapeMarker = ShapeMarker.builder()
                        .shape(new Shape(points), 64)
                        .depthTestEnabled(false)
                        .lineWidth(0)
                        .fillColor(new Color("#7f7f7f"))
                        .detail(factionObject.getFactionName() + " claimed FULL land")
                        .minDistance(10)
                        .maxDistance(10000)
                        .label(factionObject.getFactionName() + " claimed FULL land")
                        .position(new Vector3d(chunkToClaim.getX() * 16, 64, chunkToClaim.getZ() * 16))
                        .build();
            }

            // FOR INFO, is use markerSet.getMarkers().size() as the ID of the ShapeMarker
            // Add the ShapeMarker ID in the related Hashmap (to eb able to remove it later more easily)
            plugin.factionManager.factionMapRenderService.getBluemapClaimedChunk().put(chunkToClaim, factionObject.getFactionMarkerSet().getMarkers().size());

            // Put the new ShapeMarker in the MarkerSet
            factionObject.getFactionMarkerSet().getMarkers().put("claimedLand " + factionObject.getFactionMarkerSet().getMarkers().size(), shapeMarker);

            // Redraw all the marker on the map
            System.out.println(BlueMapAPI.getInstance().isPresent());
            BlueMapAPI.onEnable(api -> {
                api.getWorld(Bukkit.getWorld("world")).ifPresent(world -> {
                    for (BlueMapMap map : world.getMaps()) {
                        map.getMarkerSets().put("FactionMarkerSet" + factionObject.getFactionName() + " claims", factionObject.getFactionMarkerSet());
                    }
                });
            });
        }
        plugin.factionManager.factionMapRenderService.drawFactionOutsideLine(factionObject);
    }

    private void ForceUnClaim(FactionObject factionObject, Chunk chunkToCheck) {
        // Can only unclaim in the overworld
        if (!Objects.equals(Bukkit.getWorld("world"), chunkToCheck.getWorld())) {
            return;
        }

        // Remove Chunk from the FactionManagers claimedChunkCache
        plugin.factionManager.factionLandService.getLinkedChunks().remove(chunkToCheck);

        // Change player state
        for (Player player : Bukkit.getOnlinePlayers()){
            if (player.getLocation().getChunk().equals(chunkToCheck)) {
                plugin.factionManager.factionLandService.getPlayerChunkState().put(player.getUniqueId(), PlayerChunkState.WILDERNESS);
            }
        }

        // Unlink Chunk to Faction
        factionObject.getHardClaimedChunks().remove(chunkToCheck);

        if (plugin.getConfig().getBoolean("enable-bluemap-addon")) {

            // Remove the ShapeMarker from the MarkerSet
            factionObject.getFactionMarkerSet().remove("claimedLand " + plugin.factionManager.factionMapRenderService.getBluemapClaimedChunk().get(chunkToCheck));

            // Remove the ShapeMarker from the related Hashmap
            plugin.factionManager.factionMapRenderService.getBluemapClaimedChunk().remove(chunkToCheck);

            try {
                // Redraw all the marker on the map
                BlueMapAPI.onEnable(api -> {
                    api.getWorld(Bukkit.getWorld("world")).ifPresent(world -> {
                        for (BlueMapMap map : world.getMaps()) {
                            map.getMarkerSets().put("FactionMarkerSet" + factionObject.getFactionName() + " claims", factionObject.getFactionMarkerSet());
                        }
                    });
                });
            } catch (Exception e) {
                plugin.getLogger().severe("BlueMap integration failed: " + e.getMessage());
            }
        }
    }
}
