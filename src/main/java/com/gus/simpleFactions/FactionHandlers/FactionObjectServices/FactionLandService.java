package com.gus.simpleFactions.FactionHandlers.FactionObjectServices;

import com.gus.simpleFactions.Enums.PlayerChunkState;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.util.*;

public class FactionLandService {

    private final SimpleFactions plugin;
    public FactionLandService(SimpleFactions plugin) {
        this.plugin = plugin;
        MAX_WEAK_CHUNKS = plugin.getConfig().getInt("weak-amount-coefficient");
    }


    private Map<Chunk, FactionObject> linkedChunks = new HashMap<>();
    public Map<Chunk, FactionObject> getLinkedChunks() {
        return this.linkedChunks;
    }
    public void addLinkedChunk(Chunk chunk, FactionObject faction) {
        this.linkedChunks.put(chunk, faction);
    }
    public void removeLinkedChunk(Chunk chunk) {
        this.linkedChunks.remove(chunk);
    }


    private Map<UUID, PlayerChunkState> playerChunkState = new HashMap<>();
    public Map<UUID, PlayerChunkState> getPlayerChunkState() {
        return this.playerChunkState;
    }
    public void addPlayerInProtectedChunks(UUID playerUUID, PlayerChunkState state) {
        this.playerChunkState.put(playerUUID, state);
    }
    public void removePlayerInProtectedChunks(UUID playerUUID) {
        this.playerChunkState.remove(playerUUID);
    }


    private final int MAX_WEAK_CHUNKS;
    public int getMAX_WEAK_CHUNKS(FactionObject faction) {
        return this.MAX_WEAK_CHUNKS * faction.getPower();
    }


    public void ClaimLand(FactionObject faction, UUID playerUUID) {
        Player player = plugin.factionManager.factionHelperService.checkPlayer(playerUUID);
        if (player == null) {
            System.out.println("Something went wrong when trying to find the player with the UUID: " + playerUUID);
            return;
        }

        Chunk chunkToClaim = player.getLocation().getChunk();

        // Check if the Chunk is in faction land (needs to be beside another claimed chunk)
        if (!faction.getHardClaimedChunks().isEmpty() && !plugin.factionManager.factionHelperService.isChunkInLand(faction.getHardClaimedChunks(), chunkToClaim)) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You have to claim a chunk beside another claimed chunk !");
            return;
        }

        // Can only claim in the overworld
        if (!Objects.equals(Bukkit.getWorld("world"), player.getWorld())){
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You can only claim land in the overworld !");
            return;
        }


        // Check if the player is claiming in a valid area (not already claimed)
        if (getLinkedChunks().containsKey(chunkToClaim)) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The chunk you are trying to claim is already claimed (by your faction or another) !");
            return;
        }

        boolean weakClaim = false;
        if (faction.getHardClaimedChunks().size() < faction.getPower()) {

            // Link Chunk to Faction
            faction.getHardClaimedChunks().add(chunkToClaim);

        } else if (faction.getWeakClaimedChunks().size() <= plugin.getConfig().getDouble("weak-amount-coefficient") * faction.getPower()) {
            if (!plugin.getConfig().getBoolean("faction.object.weak-claims-enabled")) {
                return;
            }

            // Link Raidable Chunk to Faction
            faction.getWeakClaimedChunks().add(chunkToClaim);
            weakClaim = true;

        } else {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Your faction is out of chunks to claim !");
            return;
        }

        // Add Chunk in FactionManager claimedChunkCache (claimed or raidable doesn't matter)
        addLinkedChunk(chunkToClaim, faction);

        // Change player state
        if (weakClaim){
            addPlayerInProtectedChunks(playerUUID, PlayerChunkState.WEAK);
        } else {
            addPlayerInProtectedChunks(playerUUID, PlayerChunkState.HARD);
        }

        if (plugin.factionManager.factionMapRenderService.getUSE_BLUEMAP_ADDON()) {
            plugin.factionManager.factionMapRenderService.DrawChunks(faction, chunkToClaim, weakClaim);
        }

        player.sendMessage(ChatColor.GREEN + "You have, claimed this chunk for your faction !");
    }

    public void UnClaimLand(FactionObject faction, UUID playerUUID) {
        Player player = plugin.factionManager.factionHelperService.checkPlayer(playerUUID);
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
        if (!faction.getHardClaimedChunks().contains(chunkToCheck)) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The chunk you are trying to unclaim is not claimed by your faction !");
            return;
        }

        // Remove Chunk from the FactionManagers claimedChunkCache
        removeLinkedChunk(chunkToCheck);

        // Change player state
        addPlayerInProtectedChunks(playerUUID, PlayerChunkState.WILDERNESS);

        // Unlink Chunk to Faction
        faction.removeHardClaimedChunks(chunkToCheck);

        if (plugin.factionManager.factionMapRenderService.getUSE_BLUEMAP_ADDON()) {
            plugin.factionManager.factionMapRenderService.RemoveChunks(faction, chunkToCheck);
        }
        player.sendMessage(ChatColor.GREEN + "You have, unclaimed this chunk for your faction !");
    }
}
