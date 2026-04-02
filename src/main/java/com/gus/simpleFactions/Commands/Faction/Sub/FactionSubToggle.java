package com.gus.simpleFactions.Commands.Faction.Sub;

import com.flowpowered.math.vector.Vector3d;
import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.*;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Line;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class FactionSubToggle implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubToggle(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    MarkerSet markerSet = MarkerSet.builder().label("ToggleGrid").build();


    @Override
    public String getName() {
        return "toggle";
    }

    @Override
    public String getDescription() {
        return """
                This is the toggle grid command
                This is a helper command to show the chunk coordinates of a specified faction
                You can see weak, hard, or all chunks of a faction
                """;
    }

    @Override
    public String getPermission() {
        return "simplefactions.toggle";
    }

    @Override
    public String getUsage() {
        return "/faction toggle <factionName> <weak|hard|all> <enable|disable>";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
//        if (args.length != 4 || !(sender instanceof Player player) || !plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(player.getUniqueId())) {
//            sender.sendMessage(sendUsageError());
//            return;
//        }
//
//        // Get the chunks to draw depending on the player input
//        if (getPermission() != null && !player.hasPermission(getPermission())) {
//            player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
//            return;
//        }
//
//        ArrayList<Chunk> chunksToDraw = new ArrayList<>();
//        switch (args[2]) {
//            case "weak":
//                chunksToDraw = plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId()).getWeakClaimedChunks();
//                break;
//
//            case "hard":
//                chunksToDraw = plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId()).getHardClaimedChunks();
//                break;
//
//            case "all":
//                chunksToDraw.addAll(plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId()).getWeakClaimedChunks());
//                chunksToDraw.addAll(plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId()).getHardClaimedChunks());
//                break;
//        }
//
//        // Draw the edges for each chunk and add a label
//        for (Chunk chunk : chunksToDraw) {
//
//            // Get the center of the chunk and draw the chunk label with coordinates
//            Vector3d center = new Vector3d(chunk.getX() * 16 + 7.5, 65, chunk.getZ() * 16 + 7.5);
//            HtmlMarker htmlMarker = HtmlMarker.builder()
//                    .position(center)
//                    .label(chunk.getX() + ", " + chunk.getZ())
//                    .html("\"<div style='line-height: 1em; font-size: 1em; color: white; transform: translate(-50%, -50%);'>" + chunk.getX() + " ," + chunk.getZ() + "</div>\"")
//                    .minDistance(1)
//                    .maxDistance(100)
//                    .build();
//
//            markerSet.put("toggle-grid " + markerSet.getMarkers().size(), htmlMarker);
//
//            // Draw the chunk borders
//            drawChunkBorder(chunk);
//        }
//
//
//        try {
//            // Redraw all the markers on the map
//            BlueMapAPI.onEnable(api -> {
//                api.getWorld(Bukkit.getWorld("world")).ifPresent(blueWorld -> {
//                    for (BlueMapMap map : blueWorld.getMaps()) {
//                        map.getMarkerSets().put("my-marker-set-id0", markerSet);
//                    }
//                });
//            });
//        } catch (Exception e) {
//            plugin.getLogger().severe("BlueMap integration failed: " + e.getMessage());
//        }
//    }
//
//    private void createLineMarker(int x1, int z1, int x2, int z2, int id, String factionName) {
//        Line borderLine = new Line(
//                new Vector3d(x1, 64, z1),
//                new Vector3d(x2, 64, z2)
//        );
//
//        LineMarker lineMarker = LineMarker.builder()
//                .label(" ")
//                .line(borderLine) // Pass the Line object here
//                .lineWidth(3)
//                .lineColor(new Color(255, 255, 255, 1f))
//                .depthTestEnabled(false)
//                .minDistance(1)
//                .maxDistance(100)
//                .build();
//
//        markerSet.put(factionName + "_border_" + id, lineMarker);
//    }
//
//    private void drawChunkBorder(Chunk chunk){
//
//        // Top Left
//        int x1 = chunk.getX() * 16;
//        int z1 = chunk.getZ() * 16;
//
//        // Top Right
//        int x2 = chunk.getX() * 16 + 16;
//        int z2 = chunk.getZ() * 16;
//
//        // Bottom Left
//        int x3 = chunk.getX() * 16;
//        int z3 = chunk.getZ() * 16 + 16;
//
//        // Bottom Right
//        int x4 = chunk.getX() * 16 + 16;
//        int z4 = chunk.getZ() * 16 + 16;
//
//        // Left Border
//        createLineMarker(x1, z1, x3, z3, markerSet.getMarkers().size(), chunk.getWorld().getName());
//
//        // Right Border
//        createLineMarker(x2, z2, x4, z4, markerSet.getMarkers().size(), chunk.getWorld().getName());
//
//        // Upper Border
//        createLineMarker(x1, z1, x2, z2, markerSet.getMarkers().size(), chunk.getWorld().getName());
//
//        // Lower Border
//        createLineMarker(x3, z3, x4, z4, markerSet.getMarkers().size(), chunk.getWorld().getName());
//    }
    }
}
