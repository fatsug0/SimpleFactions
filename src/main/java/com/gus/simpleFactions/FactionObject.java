package com.gus.simpleFactions;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class FactionObject {

    public FactionObject(SimpleFactions plugin, UUID player, String factionName){
        this.factionName = factionName;
        this.factionOwner = player;
        this.factionMembers.add(player);

        this.plugin = plugin;
        this.factionManager = plugin.factionManager;
    }

    private final SimpleFactions plugin;
    private final FactionManager factionManager;

    private final UUID factionOwner;
    public UUID getOwner(){
        return this.factionOwner;
    }

    private Location factionHome;
    public Location getFactionHome(){
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


    public void InvitePlayer(String invitingPlayer) {
        Player player = Bukkit.getPlayer(invitingPlayer);

        // Player already has a pending invite for the same faction
        assert player != null;
        if (factionManager.pendingFactionInvites.contains(new FactionManager.FactionInvite(player.getUniqueId(), this))) return;

        // Invite Plater in the faction
        factionManager.pendingFactionInvites.add(new FactionManager.FactionInvite(player.getUniqueId(), this));

        player.sendMessage("§4You have been invited to join the faction " + this.getFactionName() + " !");
    }

    public void KickPlayer(UUID player){
        // Check if the player is in the faction
        if (!factionManager.playerFactionLink.containsKey(player)) return;

        // Remove him from the faction, Faction side
        this.getFactionMembers().remove(player);

        // Remove him from the faction, Manager side
        factionManager.playerFactionLink.remove(player);

        Objects.requireNonNull(Bukkit.getPlayer(player)).sendMessage("§2You have been kicked of the faction: " +  this.getFactionName() + " !");
    }

    public void TeleportHome(UUID player){
        // Use a Bukkit Runnable
        plugin.teleportManager.StartTeleport(player, 5, getFactionHome());
    }

    public void SetHome(UUID player){
        // Same home set, cancel set home
        if (Objects.requireNonNull(Bukkit.getPlayer(player)).getLocation().equals(getFactionHome())) return;

        // Remove and set the new Faction home
        this.factionHome = Objects.requireNonNull(Bukkit.getPlayer(player)).getLocation();

        Objects.requireNonNull(Bukkit.getPlayer(player)).sendMessage("§2You have, set the home of your faction !");
    }

    public void ClaimLand(UUID player){
        Chunk chunkToCheck = Objects.requireNonNull(Bukkit.getPlayer(player)).getLocation().getChunk();
        // Can only claim in the overworld
        if (!Objects.equals(Bukkit.getWorld("world"), Objects.requireNonNull(Bukkit.getPlayer(player)).getWorld())) return;

        // Check if the player is claiming in a valid area (not already claimed and not admin protected)
        if (factionManager.protectedChunks.contains(chunkToCheck)) return;
        if (factionManager.claimedChunks.containsKey(chunkToCheck)) return;

        // Add Chunk (record data type) in FactionManager claimedChunkCache
        factionManager.claimedChunks.put(chunkToCheck, this);

        // Link Chunk to Faction
        this.getClaimedChunks().add(chunkToCheck);

        Objects.requireNonNull(Bukkit.getPlayer(player)).sendMessage("§2You have, claimed this chunk for your faction !");
    }

    public void UnClaimLand(UUID player){
        Chunk chunkToCheck = Objects.requireNonNull(Bukkit.getPlayer(player)).getLocation().getChunk();

        // Can only unclaim in the overworld
        if (!Objects.equals(Bukkit.getWorld("world"), Objects.requireNonNull(Bukkit.getPlayer(player)).getWorld())) return;

        // Check if the player is unclaiming in a valid area (standing on a claimed chunk and claimed by his faction)
        if (!this.getClaimedChunks().contains(chunkToCheck)) return;

        // Remove Chunk (record data type) from the FactionManagers claimedChunkCache
        factionManager.claimedChunks.remove(chunkToCheck);

        // Unlink Chunk to Faction
        this.getClaimedChunks().remove(chunkToCheck);

        Objects.requireNonNull(Bukkit.getPlayer(player)).sendMessage("§4You have, unclaimed this chunk for your faction !");

    }

    public void SendFactionInfo(UUID player){
        // Send Important information of the Faction to the player for a quick view:
        // - FactionName CHECK
        // - FactionOwner CHECK
        // - NumberOfMembers CHECK
        // - And more if needed ...
        Objects.requireNonNull(Bukkit.getPlayer(player)).sendMessage(
                "Here is the information about your faction: §a§l" + this.getFactionName() +
                        "The owner of this faction is : §l" + Objects.requireNonNull(Bukkit.getPlayer(player)).getName() +
                        "The faction currently count " + this.getFactionMembers().size() + " members"
        );
    }

    public void LeaveFaction(UUID player){
        // Check if the player is not the Owner of the Faction
        if (player.equals(this.getOwner())) return;

        // Remove player from FactionManagers FactionPlayerLink ArrayList
        factionManager.playerFactionLink.remove(player);

        // Remove player from the Factions member list
        this.getFactionMembers().remove(player);

        Objects.requireNonNull(Bukkit.getPlayer(player)).sendMessage("§4You have left faction the faction " + this.getFactionName());
    }

    public void DisbandFaction(UUID player){
        // Check if the player is the Owner of the Faction
        if (!player.equals(this.getOwner())) return;

        // Remove EVERY player of the Faction from FactionManagers FactionPlayerLink ArrayList
        for (UUID uuid : factionManager.playerFactionLink.keySet()){
            if (this.getFactionMembers().contains(uuid)){
                factionManager.playerFactionLink.remove(uuid);
            }
        }

        // Remove EVERY player from the Factions member list
        for (UUID uuid : this.getFactionMembers()){
            this.getFactionMembers().remove(uuid);
        }

        // Unclaim every claimed land
        for (Chunk chunk : factionManager.claimedChunks.keySet()){
            if (factionManager.claimedChunks.get(chunk).equals(this)){
                factionManager.claimedChunks.remove(chunk);
            }
        }

        // Remove this faction from the factions lists in the FactionManager
        factionManager.existingFactions.remove(this);

        Objects.requireNonNull(Bukkit.getPlayer(player)).sendMessage("§4You have, as faction owner, disbanded your faction " + this.getFactionName());

    }
}
