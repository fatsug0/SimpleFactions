package com.gus.simpleFactions;

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector3d;
import com.gus.simpleFactions.Enums.PlayerChunkState;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.LineMarker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Line;
import de.bluecolored.bluemap.api.math.Shape;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;


import java.util.*;

public class FactionObject {

    public FactionObject(SimpleFactions plugin, UUID player, String factionName, Color factionClaimColor, Integer factionStartPower, double maxRaidableChunksCoefficient) {
        this.factionName = factionName;
        this.factionOwner = player;
        this.factionMembers.add(player);
        this.power = factionStartPower;
        this.maxWeakChunks = Math.round(factionStartPower * maxRaidableChunksCoefficient);

        // Classes instances
        this.plugin = plugin;
        this.factionManager = plugin.factionManager;

        // BlueMap stuff
        this.markerSet = MarkerSet.builder().label("FactionMarkerSet " + factionName).build();
        this.factionClaimColor = factionClaimColor;

//        createTabTeam();
    }

    private final SimpleFactions plugin;
    private final FactionManager factionManager;

    private final UUID factionOwner;
    public UUID getOwner() {
        return this.factionOwner;
    }

    private Location factionHome;
    public Location getFactionHome() {
        return this.factionHome;
    }

    private final String factionName;
    public String getFactionName() {
        return this.factionName;
    }

    private ArrayList<UUID> factionMembers = new ArrayList<>();
    public ArrayList<UUID> getFactionMembers() {
        return this.factionMembers;
    }

    private ArrayList<Chunk> claimedChunks = new ArrayList<>();
    public ArrayList<Chunk> getClaimedChunks() {
        return this.claimedChunks;
    }

    private ArrayList<Chunk> weakChunks = new ArrayList<>();
    public ArrayList<Chunk> getWeakChunks() {
        return this.weakChunks;
    }

    private ArrayList<FactionRank> existingFactionRanks = new ArrayList<>();
    public Map<UUID, FactionRank> factionRanks = new HashMap<>();

    private int power;
    public int getPower() {
        return this.power;
    }
    public void setPower(int power) {
        this.power = power;
    }

    private final double maxWeakChunks;
    public double getMaxWeakChunks() {
        return this.maxWeakChunks;
    }

    // For now, using a boolean, but while use a config file for this (when setup)
    // Is used to display on bluemap the claimed chunks of the faction
    private final boolean USE_BLUEMAP_ADDON = true;
    private final MarkerSet markerSet;
    private final Color factionClaimColor;

    public String teamPrefix;

    /*
    FACTION RELATED
     */
    public void KickPlayer(UUID playerUUID) {
        Player player = checkPlayer(playerUUID);
        if (player == null) {
            System.out.println("Something went wrong when trying to find the player with the UUID: " + playerUUID);
            return;
        }

        // Check if the player is in the faction
        if (!factionMembers.contains(playerUUID) || !factionManager.playerFactionLink.containsKey(playerUUID)) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The player you are trying to kick is not in your faction !");
            return;
        }
        // Remove him from the faction, Faction side
        factionMembers.remove(playerUUID);

        // Remove him from the faction, Manager side
        factionManager.playerFactionLink.remove(playerUUID);

        player.sendMessage("§2You have been kicked of the faction: " + getFactionName() + " !");
    }

    public void TeleportHome(UUID playerUUID) {
        Player player = checkPlayer(playerUUID);
        if (player == null) {
            System.out.println("Something went wrong when trying to find the player with the UUID: " + playerUUID);
            return;
        }

        // There is no home setup yet
        if (getFactionHome() == null) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You don't have a home set for your faction !\n Set one with /f home set");
            return;
        }

        // Use a Bukkit Runnable
        plugin.teleportManager.StartTeleport(playerUUID, 5, getFactionHome());
    }

    public void SetHome(UUID playerUUID) {
        Player player = checkPlayer(playerUUID);
        if (player == null) {
            System.out.println("Something went wrong when trying to find the player with the UUID: " + playerUUID);
            return;
        }

        // Same home set, cancel set home
        if (player.getLocation().equals(getFactionHome())) {
            Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The new home you are trying to set is the same as your current one !");
            return;
        }

        // Heck if the wanted home is in the claimed chunks of the faction
        if (claimedChunks.isEmpty() || !claimedChunks.contains(player.getLocation().getChunk())) {
            Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The home you are trying to set is not in the claimed chunks of your faction !");
            return;
        }

        // Remove and set the new Faction home
        factionHome = player.getLocation();

        player.sendMessage("§2You have, set the home of your faction !");
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
                        .shape(new Shape(points), 62)
                        .depthTestEnabled(false)
                        .lineWidth(0)
                        .fillColor(new Color(factionClaimColor.getRed(), factionClaimColor.getGreen(), factionClaimColor.getBlue(), 0.35f))
                        .detail(factionName + " claimed WEAK land")
                        .minDistance(10)
                        .maxDistance(10000)
                        .label(factionName + " claimed WEAK land")
                        .position(new Vector3d(chunkToClaim.getX() * 16, 64, chunkToClaim.getZ() * 16))
                        .build();
            } else {
                shapeMarker = ShapeMarker.builder()
                        .shape(new Shape(points), 62)
                        .depthTestEnabled(false)
                        .lineWidth(0)
                        .fillColor(new Color(factionClaimColor.getRed(), factionClaimColor.getGreen(), factionClaimColor.getBlue(), 0.65f))
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
            markerSet.put("claimedLand " + markerSet.getMarkers().size(), shapeMarker);

            try {
                // Redraw all the marker on the map
                BlueMapAPI.onEnable(api -> {
                    api.getWorld(Bukkit.getWorld("world")).ifPresent(world -> {
                        for (BlueMapMap map : world.getMaps()) {
                            map.getMarkerSets().put("my-marker-set-id", markerSet);
                        }
                    });
                });
            } catch (Exception e) {
                plugin.getLogger().severe("BlueMap integration failed: " + e.getMessage());

            }
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

            // Redraw all the marker on the map
            BlueMapAPI.onEnable(api -> {
                api.getWorld(Bukkit.getWorld("world")).ifPresent(world -> {
                    for (BlueMapMap map : world.getMaps()) {
                        map.getMarkerSets().put("my-marker-set-id", markerSet);
                    }
                });
            });
        }
        player.sendMessage(ChatColor.GREEN + "You have, unclaimed this chunk for your faction !");
    }

    public void LeaveFaction(UUID playerUUID) {
        Player player = checkPlayer(playerUUID);
        if (player == null) {
            System.out.println("Something went wrong when trying to find the player with the UUID: " + playerUUID);
            return;
        }

        // Check if the player is not the Owner of the Faction
        if (playerUUID.equals(this.getOwner())) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You can't leave your own faction !");
            return;
        }


        // Remove player from FactionManagers FactionPlayerLink ArrayList
        factionManager.playerFactionLink.remove(playerUUID);

        //Remove player from team
        Objects.requireNonNull(Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard().getTeam(toTeamName(this.getFactionName()))).removeEntry(player.getName());

        // Remove player from the Factions member list
        factionMembers.remove(playerUUID);

        player.sendMessage(ChatColor.RED + ChatColor.ITALIC.toString() + "You have left faction the faction " + this.getFactionName());
    }

    public void DisbandFaction(UUID playerUUID) {
        Player player = checkPlayer(playerUUID);
        if (player == null) {
            System.out.println("Something went wrong when trying to find the player with the UUID: " + playerUUID);
            return;
        }

        // Check if the player is the Owner of the Faction
        if (!playerUUID.equals(getOwner())) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You can only disband your faction if you are its owner !");
            return;
        }

        // Remove EVERY player of the Faction from FactionManagers FactionPlayerLink ArrayList
        for (UUID uuid : factionManager.playerFactionLink.keySet()){
            if (getFactionMembers().contains(uuid)){
                factionManager.playerFactionLink.remove(uuid);
            }
        }

        // Remove EVERY player from the Factions member list
        for (UUID uuid : getFactionMembers()){
            this.factionMembers.remove(uuid);
        }

        // Unclaim every claimed land (and weak chunks)
        for (Chunk chunk : factionManager.linkedChunks.keySet()){
            if (factionManager.linkedChunks.get(chunk).equals(this)){
                factionManager.linkedChunks.remove(chunk);
            }
        }

        Objects.requireNonNull(Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard().getTeam(toTeamName(this.getFactionName()))).unregister();

        // Remove this faction from the factions lists in the FactionManager
        factionManager.existingFactions.remove(this);

        player.sendMessage(ChatColor.RED + ChatColor.ITALIC.toString() + "You have, as faction owner, disbanded your faction " + this.getFactionName());
    }



    /*
    FACTION RANK RELATED
     */
    // Create & Delete Rank
    public void CreateFactionRank(String rankName, Player player){
        for (FactionRank rank : factionRanks.values()){
            if (rank.getRankName().equals(rankName)){
                player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The rank you are trying to create already exists !");
                return;
            }
        }
        existingFactionRanks.add(new FactionRank(rankName));
    }
    public void DeleteFactionRank(String rankName, Player player){
        if (existingFactionRanks.isEmpty()) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "There is no rank to delete !");
            return;
        }

        if (!existingFactionRanks.contains(new FactionRank(rankName))) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The rank you are trying to delete does not exist !");
            return;
        }

        for (UUID playerUUID : factionRanks.keySet()){
            if (factionRanks.get(playerUUID).getRankName().equals(rankName)){
                factionRanks.remove(playerUUID);

                // Remove his perms
                if (checkPlayer(playerUUID) == null) return;
                plugin.permissionManager.RemovePerm(checkPlayer(playerUUID), factionRanks.get(playerUUID).getPermissions());
            }
        }

        existingFactionRanks.removeIf(rank -> rank.getRankName().equals(rankName));
    }

    // Add & Remove Player to Rank
    public void RemovePlayerFromRank(Player player, String rankName){
        if (!factionRanks.containsKey(player.getUniqueId())){
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Player has no a rank ! Cant remove him from it !");
            return;
        }

        for (FactionRank rank : existingFactionRanks){
            if (rank.getRankName().equals(rankName)){
                factionRanks.remove(player.getUniqueId(), rank);
                plugin.permissionManager.RemovePerm(player, rank.getPermissions());
            }
        }
        System.out.println(ChatColor.GREEN + ChatColor.ITALIC.toString() + "Added player " + player.getName() + " to rank " + rankName);
        System.out.println(player.getName() + " now has the following permissions: " + player.getEffectivePermissions().toString());
    }
    public void AddPlayerToRank(Player player, String rankName){
        if (factionRanks.containsKey(player.getUniqueId())){
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Player has already a rank ! Remove it first !");
            return;
        }
        for (FactionRank rank : existingFactionRanks){
            if (rank.getRankName().equals(rankName)){
                factionRanks.put(player.getUniqueId(), rank);
                plugin.permissionManager.AddPerm(player, rank.getPermissions());
            }
        }
        System.out.println(ChatColor.GREEN + ChatColor.ITALIC.toString() + "Added player " + player.getName() + " to rank " + rankName);
        System.out.println(player.getName() + " now has the following permissions: " + player.getEffectivePermissions().toString());
    }

    // Add & Remove permission from rank
    public void AddPermissionRank(String rankName, ArrayList<String> permission) {
        for (FactionRank rank : factionRanks.values()) {
            if (rank.getRankName().equals(rankName)) {
                for (String perm : permission) {
                    rank.addPermission(perm);
                }
                System.out.println("Added the following permissions to the rank " + rankName + ": " + permission.toString());
                return;
            }
        }
    }
    public void RemovePermissionRank(String rankName, ArrayList<String> permission) {
        for (FactionRank rank : factionRanks.values()) {
            if (rank.getRankName().equals(rankName)) {
                for (String perm : permission) {
                    rank.removePermission(perm);
                }
                System.out.println("Removed the following permissions from the rank " + rankName + ": " + permission.toString());
                return;
            }
        }
    }



    /*
    INFORMATION COMMANDS
     */
    public String SendFactionInfo(UUID playerUUID) {
        Player player = checkPlayer(playerUUID);
        if (player == null) {
            System.out.println("Something went wrong when trying to find the player with the UUID: " + playerUUID);
            return ChatColor.RED + "Something went wrong when trying to find the player with the UUID: " + playerUUID;
        }

        // Enhanced with colors, styles, and more details
        StringBuilder info = new StringBuilder();
        info.append("§e§l=== Faction Info: §a§l").append(this.getFactionName()).append(" §e§l===\n");
        info.append("§7Owner: §b§l").append(Objects.requireNonNull(Bukkit.getPlayer(this.getOwner())).getName()).append("\n");
        info.append("§7Members: §a").append(this.getFactionMembers().size()).append(" §7(total)\n");
        info.append("§7Power: §6").append(this.getPower()).append(" §7/ Max Weak Chunks: §c").append((int) this.getMaxWeakChunks()).append("\n");
        info.append("§7Claimed Chunks: §2").append(this.getClaimedChunks().size()).append(" §7(Strong) + §4").append(this.getWeakChunks().size()).append(" §7(Weak)\n");
        info.append("§7Home: §d").append(this.getFactionHome() != null ? "Set at " + this.getFactionHome().getBlockX() + ", " + this.getFactionHome().getBlockY() + ", " + this.getFactionHome().getBlockZ() : "Not set").append("\n");
        info.append("§e§l=======================");

        return info.toString();
    }

    public String getAllRankInfo(){
        if (existingFactionRanks.isEmpty()) {
            return "§cNo ranks exist in this faction.";
        }

        StringBuilder info = new StringBuilder();
        info.append("§e§l=== All Ranks in §a§l").append(this.getFactionName()).append(" §e§l===\n");
        for (FactionRank rank : existingFactionRanks) {
            int memberCount = 0;
            for (FactionRank playerRank : factionRanks.values()) {
                if (playerRank.getRankName().equals(rank.getRankName())) {
                    memberCount++;
                }
            }
            info.append("§b§l").append(rank.getRankName()).append("§7: §a").append(memberCount).append(" members, §6").append(rank.getPermissions().size()).append(" permissions\n");
        }
        info.append("§e§l=======================");

        return info.toString();
    }

    public String getRankInfo(String rankName){
        FactionRank targetRank = null;
        for (FactionRank rank : existingFactionRanks) {
            if (rank.getRankName().equalsIgnoreCase(rankName)) {
                targetRank = rank;
                break;
            }
        }
        if (targetRank == null) {
            return "§cRank '" + rankName + "' does not exist.";
        }

        int memberCount = 0;
        for (FactionRank playerRank : factionRanks.values()) {
            if (playerRank.getRankName().equals(rankName)) {
                memberCount++;
            }
        }

        StringBuilder info = new StringBuilder();
        info.append("§e§l=== Rank Info: §b§l").append(rankName).append(" §e§l===\n");
        info.append("§7Members: §a").append(memberCount).append("\n");
        info.append("§7Permissions: §6").append(targetRank.getPermissions().size()).append("\n");
        info.append("§e§l=======================");

        return info.toString();
    }

    public String SendRankPlayerInfo(String rankName){
        FactionRank targetRank = null;
        for (FactionRank rank : existingFactionRanks) {
            if (rank.getRankName().equalsIgnoreCase(rankName)) {
                targetRank = rank;
                break;
            }
        }
        if (targetRank == null) {
            return "§cRank '" + rankName + "' does not exist.";
        }

        StringBuilder info = new StringBuilder();
        info.append("§e§l=== Players in Rank: §b§l").append(rankName).append(" §e§l===\n");
        boolean hasMembers = false;
        for (Map.Entry<UUID, FactionRank> entry : factionRanks.entrySet()) {
            if (entry.getValue().getRankName().equals(rankName)) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null) {
                    info.append("§a- ").append(player.getName()).append("\n");
                    hasMembers = true;
                }
            }
        }
        if (!hasMembers) {
            info.append("§7No players in this rank.\n");
        }
        info.append("§e§l=======================");

        return info.toString();
    }

    public String SendRankPermissionsInfo(String rankName){
        FactionRank targetRank = null;
        for (FactionRank rank : existingFactionRanks) {
            if (rank.getRankName().equalsIgnoreCase(rankName)) {
                targetRank = rank;
                break;
            }
        }
        if (targetRank == null) {
            return "§cRank '" + rankName + "' does not exist.";
        }

        StringBuilder info = new StringBuilder();
        info.append("§e§l=== Permissions for Rank: §b§l").append(rankName).append(" §e§l===\n");
        if (targetRank.getPermissions().isEmpty()) {
            info.append("§7No permissions assigned.\n");
        } else {
            for (String perm : targetRank.getPermissions()) {
                info.append("§6- ").append(perm).append("\n");
            }
        }
        info.append("§e§l=======================");

        return info.toString();
    }



    /*
    HELPER METHODS
     */
    private boolean isChunkInLand(Chunk chunkToClaim) {
        World world = Bukkit.getWorld("world");
        if (world == null) return false;

        // Check on X axis
        if (this.getClaimedChunks().contains(world.getChunkAt(chunkToClaim.getX() - 1, chunkToClaim.getZ())))
            return true;
        if (this.getClaimedChunks().contains(world.getChunkAt(chunkToClaim.getX() + 1, chunkToClaim.getZ())))
            return true;

        // Check on Z axis
        if (this.getClaimedChunks().contains(world.getChunkAt(chunkToClaim.getX(), chunkToClaim.getZ() - 1)))
            return true;
        if (this.getClaimedChunks().contains(world.getChunkAt(chunkToClaim.getX(), chunkToClaim.getZ() + 1)))
            return true;

        return false;
    }

    private void drawFactionOutsideLine(){
        World world = Bukkit.getWorld("world");
        if (world == null) return;

        // Remove all previous border markers to start fresh
        markerSet.getMarkers().keySet().removeIf(key -> key.startsWith(factionName + "_border_"));

        // Just to keep track of each border (with ID's)
        int lineIdCount = 0;

        // Check each chunk of the faction
        for (Chunk chunk : this.getClaimedChunks()) {

            // Get the upper left corner
            int x0 = chunk.getX() * 16;
            int z0 = chunk.getZ() * 16;

            // Get the lower right corner
            int x1 = x0 + 16;
            int z1 = z0 + 16;

            // Define the 4 edges of the chunk
            // North: (x0, z0) to (x1, z0)
            if (!this.getClaimedChunks().contains(world.getChunkAt(chunk.getX(), chunk.getZ() - 1))) {
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

        // Redraw all the markers on the map
        BlueMapAPI.onEnable(api -> {
            api.getWorld(Bukkit.getWorld("world")).ifPresent(blueWorld -> {
                for (BlueMapMap map : blueWorld.getMaps()) {
                    map.getMarkerSets().put("my-marker-set-id", markerSet);
                }
            });
        });
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

    public boolean isChunkHardClaimed(Chunk chunk) {
        return this.getClaimedChunks().contains(chunk);
    }

    public void createTabTeam() {
        Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();

        Team newTeam = scoreboard.registerNewTeam(toTeamName(this.getFactionName()));
        String teamPrefix = useMiniMessage(" [" + factionName + "] ", new ArrayList<>(List.of("7f7f7f")));
        newTeam.setPrefix(teamPrefix);

        Player owner = Bukkit.getPlayer(this.getOwner());
        if (owner == null) return;

        newTeam.addEntry(owner.getName());
    }

    private String toTeamName(String factionName) {
        // This is used to ensure the right format in the team naming (basic characters and 16 characters limit)
        String base = "f_" + factionName.toLowerCase().replaceAll("[^a-z0-9_]", "");
        return base.substring(0, Math.min(16, base.length()));
    }

    private String useLegacyText(String text){
        final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
                .character('§')
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .build();

        Component parsed = Component.text(text)
                .color(TextColor.color(100, 100, 100));
        return LEGACY.serialize(parsed);
    }

    private String useMiniMessage(String text, ArrayList<String> colors){
        final MiniMessage MM = MiniMessage.miniMessage();
        final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
                .character('§')
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .build();

        Component parsed;
        if (colors.size() == 1) {
            parsed = MM.deserialize("<shadow:#000000FF><b><color:#" + colors.getFirst() + ">" + text + "</color>");
        } else {
            StringBuilder colorCode = new StringBuilder();
            for (String color : colors) colorCode.append(":#").append(color);
            parsed = MM.deserialize("<shadow:#000000FF><b><gradient" + colorCode + ">" + text + "</gradient>");
        }
        return LEGACY.serialize(parsed);
    }

    public void setTeamPrefix(String prefixName, ArrayList<String> colors){
        Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
        Team team = scoreboard.getTeam(toTeamName(this.getFactionName()));
        team.setPrefix(useMiniMessage(" [" + prefixName + "] ", colors));
    }

    private Player checkPlayer(UUID playerUUID) {
        if (playerUUID == null) {
            plugin.getLogger().warning("Null UUID passed to checkPlayer");
            return null;
        }
        Player onlinePlayer = Bukkit.getPlayer(playerUUID);
        if (onlinePlayer != null) {
            return onlinePlayer;
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
        if (offlinePlayer.hasPlayedBefore()) {
            return offlinePlayer.getPlayer();  // This might still be null if not loaded
        } else {
            plugin.getLogger().warning("Player with UUID " + playerUUID + " has never played on this server.");
            return null;
        }
    }
}
