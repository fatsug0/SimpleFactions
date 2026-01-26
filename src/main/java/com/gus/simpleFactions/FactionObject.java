package com.gus.simpleFactions;

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector3d;
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
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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
    private Map<UUID, FactionRank> factionRank = new HashMap<>();
    public Map<UUID, FactionRank> getFactionRank() {
        return this.factionRank;
    }

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


    /*
    FACTION RELATED
     */
    public void InvitePlayer(String invitingPlayer) {
        Player player = Bukkit.getPlayer(invitingPlayer);

        // Player already has a pending invite for the same faction
        if (player == null) return;
        if (factionManager.pendingFactionInvites.contains(new FactionManager.FactionInvite(player.getUniqueId(), this)))
            return;

        // Invite Plater in the faction
        factionManager.pendingFactionInvites.add(new FactionManager.FactionInvite(player.getUniqueId(), this));

        player.sendMessage("§4You have been invited to join the faction " + this.getFactionName() + " !");
    }

    public void KickPlayer(UUID playerUUID) {
        if (playerUUID == null) return;

        // Check if the player is in the faction
        if (!factionManager.playerFactionLink.containsKey(playerUUID)) return;

        // Remove him from the faction, Faction side
        this.factionMembers.remove(playerUUID);

        // Remove him from the faction, Manager side
        factionManager.playerFactionLink.remove(playerUUID);

        Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).sendMessage("§2You have been kicked of the faction: " + this.getFactionName() + " !");
    }

    public void TeleportHome(UUID playerUUID) {
        if (playerUUID == null) return;

        // Use a Bukkit Runnable
        plugin.teleportManager.StartTeleport(playerUUID, 5, getFactionHome());
    }

    public void SetHome(UUID playerUUID) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) return;

        // Same home set, cancel set home
        if (player.getLocation().equals(getFactionHome())) return;

        // Remove and set the new Faction home
        this.factionHome = player.getLocation();

        player.sendMessage("§2You have, set the home of your faction !");
    }

    public void ClaimLand(UUID playerUUID) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) return;

        Chunk chunkToClaim = player.getLocation().getChunk();

        // Check if the Chunk is faction land (needs to be beside another claimed chunk)
        if (!claimedChunks.isEmpty() && !isChunkInLand(chunkToClaim)) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§4You have to claim a chunk beside another claimed chunk !"));
            return;
        }

        boolean weakClaim = false;
        if (this.getClaimedChunks().size() < this.getPower()) {

            // Link Chunk to Faction
            this.getClaimedChunks().add(chunkToClaim);

        } else if (this.getWeakChunks().size() <= this.getMaxWeakChunks()) {
            if (!plugin.getConfig().getBoolean("faction.object.weak-claims-enabled")) return;

            // Link Raidable Chunk to Faction
            this.getWeakChunks().add(chunkToClaim);
            weakClaim = true;

        } else {
            player.sendMessage("MAX AMOUNT OF CHUNKS REACHED");
            return;
        }

        // Can only claim in the overworld
        if (!Objects.equals(Bukkit.getWorld("world"), player.getWorld()))
            return;


        // Check if the player is claiming in a valid area (not already claimed)
        if (factionManager.linkedChunks.containsKey(chunkToClaim)) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§4The chunk you are trying to claim is already claimed (by your faction or another) !"));
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

            // Redraw all the marker on the map
            BlueMapAPI.onEnable(api -> {
                api.getWorld(Bukkit.getWorld("world")).ifPresent(world -> {
                    for (BlueMapMap map : world.getMaps()) {
                        map.getMarkerSets().put("my-marker-set-id", markerSet);
                    }
                });
            });
        }
        drawFactionOutsideLine();

        player.sendMessage("§2You have, claimed this chunk for your faction !");
    }

    public void UnClaimLand(UUID playerUUID) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) return;

        Chunk chunkToCheck = player.getLocation().getChunk();

        // Can only unclaim in the overworld
        if (!Objects.equals(Bukkit.getWorld("world"), player.getWorld()))
            return;

        // Check if the player is unclaiming in a valid area (standing on a claimed chunk and claimed by his faction)
        if (!this.getClaimedChunks().contains(chunkToCheck)) return;

        // Remove Chunk (record data type) from the FactionManagers claimedChunkCache
        factionManager.linkedChunks.remove(chunkToCheck);

        // Change player state
        factionManager.playerInProtectedChunks.put(playerUUID, PlayerChunkState.WILDERNESS);

        // Unlink Chunk to Faction
        this.getClaimedChunks().remove(chunkToCheck);

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
        player.sendMessage("§4You have, unclaimed this chunk for your faction !");
    }

    public void LeaveFaction(UUID playerUUID) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) return;

        // Check if the player is not the Owner of the Faction
        if (playerUUID.equals(this.getOwner())) return;

        // Remove player from FactionManagers FactionPlayerLink ArrayList
        factionManager.playerFactionLink.remove(playerUUID);

        //Remove player from team
        Objects.requireNonNull(Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard().getTeam(toTeamName(this.getFactionName()))).removeEntry(player.getName());

        // Remove player from the Factions member list
        this.factionMembers.remove(playerUUID);

        player.sendMessage("§4You have left faction the faction " + this.getFactionName());
    }

    public void DisbandFaction(UUID playerUUID) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) return;

        // Check if the player is the Owner of the Faction
        if (!playerUUID.equals(this.getOwner())) return;

        // Remove EVERY player of the Faction from FactionManagers FactionPlayerLink ArrayList
        for (UUID uuid : factionManager.playerFactionLink.keySet()){
            if (this.getFactionMembers().contains(uuid)){
                factionManager.playerFactionLink.remove(uuid);
            }
        }

        // Remove EVERY player from the Factions member list
        for (UUID uuid : this.getFactionMembers()){
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

        player.sendMessage("§4You have, as faction owner, disbanded your faction " + this.getFactionName());

    }



    /*
    FACTION RANK RELATED
     */
    public void CreateFactionRank(String rankName){
        for (FactionRank rank : existingFactionRanks){
            if (rank.getRankName().equals(rankName)){
                return;
            }
        }
        existingFactionRanks.add(new FactionRank(rankName));
        System.out.println("Created the rank " + rankName);
    }

    public void DeleteFactionRank(String rankName){
        for (UUID player : factionRank.keySet()){
            if (factionRank.get(player).getRankName().equals(rankName)){
                factionRank.remove(player);
            }
        }
        existingFactionRanks.removeIf(rank -> rank.getRankName().equals(rankName));
        System.out.println("Deleted the rank " + rankName);
    }

    public void RemovePlayerFromRank(Player player, String rankName){
        if (factionRank.get(player.getUniqueId()).getRankName().equals(rankName)) {
            factionRank.remove(player.getUniqueId());
            System.out.println("Removed player " + player.getName() + " from rank " + rankName);
        }
    }

    public void AddPlayerToRank(Player player, String rankName){
        if (factionRank.containsKey(player.getUniqueId())){
            player.sendMessage("Player has already a rank ! Remove if first !");
            return;
        }
        for (FactionRank rank : existingFactionRanks){
            if (rank.getRankName().equals(rankName)){
                factionRank.put(player.getUniqueId(), rank);
            }
        }
        System.out.println("Added player " + player.getName() + " to rank " + rankName);
    }

    public void AddPermissionRank(String rankName, ArrayList<String> permission){
        for (FactionRank rank : factionRank.values()){
            if (rank.getRankName().equals(rankName)){
                for (String perm : permission){
                    rank.addPermission(perm);
                }
            }
        }
        System.out.println("Added the following permissions to the rank " + rankName + ": " + permission.toString());
    }

    public void RemovePermissionRank(String rankName, ArrayList<String> permission){
        for (FactionRank rank : factionRank.values()){
            if (rank.getRankName().equals(rankName)){
                for (String perm : permission){
                    rank.removePermission(perm);
                }
            }
        }
        System.out.println("Removed the following permissions from the rank " + rankName + ": " + permission.toString());
    }



    /*
    INFORMATION COMMANDS
     */
    public String SendFactionInfo(UUID playerUUID) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) {
            System.out.println("The player has no faction");
            return null;
        }

        // Send Important information of the Faction to the player for a quick view:
        return "Here is the information about your faction: §a§l" + this.getFactionName() +
                "The owner of this faction is : §l" + Objects.requireNonNull(Bukkit.getPlayer(this.getOwner())).getName() +
                "The faction currently count " + this.getFactionMembers().size() + " members";
    }

    public String getAllRankInfo(){
        return "";
    }

    public String getRankInfo(String rankName){
        return "";
    }

    public String SendRankPlayerInfo(String rankName){
        return "";
    }

    public String SendRankPermissionsInfo(String RankName){
        return "";
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
        String teamPrefix = useMiniMessage(" [" + this.getFactionName() + "] ");
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

        net.kyori.adventure.text.Component parsed = Component.text(text)
                .color(TextColor.color(100, 100, 100));
        return LEGACY.serialize(parsed);
    }

    private String useMiniMessage(String text){
        final MiniMessage MM = MiniMessage.miniMessage();
        final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
                .character('§')
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .build();

        Component parsed = MM.deserialize("<gradient:#0434f2:#f21004>" + text + "</gradient>");
        return LEGACY.serialize(parsed);
    }
}
