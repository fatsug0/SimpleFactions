package com.gus.simpleFactions.FactionHandlers.FactionObjectServices;

import com.gus.simpleFactions.Enums.PlayerChunkState;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FactionLandService {

    private final SimpleFactions plugin;
    public FactionLandService(SimpleFactions plugin) {
        this.plugin = plugin;
        // Nested under faction.object in config.yml - and it's a fractional coefficient, not a whole number.
        WEAK_AMOUNT_COEFFICIENT = plugin.getConfig().getDouble("faction.object.weak-amount-coefficient");
    }


    private Map<Chunk, FactionObject> linkedChunks = new HashMap<>();
    public Map<Chunk, FactionObject> getLinkedChunks() {
        return linkedChunks;
    }
    public void addLinkedChunk(Chunk chunk, FactionObject faction) {
        linkedChunks.put(chunk, faction);
    }
    public void removeLinkedChunk(Chunk chunk) {
        linkedChunks.remove(chunk);
    }
    public Map<String, String> getWrappedLinkedChunks() {
            Map<String, String> returnMap = new HashMap<>();
            for (Map.Entry<Chunk, FactionObject> entry : linkedChunks.entrySet()) {
                returnMap.put(chunkKey(entry.getKey().getX(), entry.getKey().getZ()), entry.getValue().getFactionName());
            }
            return returnMap;
    }
    public void unWrapLinkedChunks(Map<String, String> wrappedLinkedChunks) {
        linkedChunks.clear();
        if (wrappedLinkedChunks == null) return;
        for (Map.Entry<String, String> entry : wrappedLinkedChunks.entrySet()) {
            Chunk chunk = chunkFromKey(entry.getKey());
            if (chunk == null) continue;
            Optional<FactionObject> faction = plugin.factionManager.factionMembershipService.getExistingFactions().stream().filter(existingFaction -> existingFaction.getFactionName().equals(entry.getValue())).findFirst();
            faction.ifPresent(factionObject -> addLinkedChunk(chunk, factionObject));
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


    private Map<UUID, PlayerChunkState> playerChunkState = new HashMap<>();
    public Map<UUID, PlayerChunkState> getPlayerChunkState() {
        return playerChunkState;
    }
    public void addPlayerInProtectedChunks(UUID playerUUID, PlayerChunkState state) {
        this.playerChunkState.put(playerUUID, state);
    }
    public void removePlayerInProtectedChunks(UUID playerUUID) {
        playerChunkState.remove(playerUUID);
    }
    public void unWrapPlayerChunkState(Map<UUID, PlayerChunkState> wrappedPlayerChunkState) {
        playerChunkState.clear();
        if (wrappedPlayerChunkState == null) return;
        for (Map.Entry<UUID, PlayerChunkState> entry : wrappedPlayerChunkState.entrySet()) {
            addPlayerInProtectedChunks(entry.getKey(), entry.getValue());
        }
    }
    public Map<String, PlayerChunkState> getWrappedPlayerChunkState() {
        Map<String, PlayerChunkState> returnMap = new HashMap<>();
        for (Map.Entry<UUID, PlayerChunkState> entry : playerChunkState.entrySet()) {
            returnMap.put(entry.getKey().toString(), entry.getValue());
        }
        return returnMap;
    }
    public void unWrapPlayerChunkStateStrings(Map<String, PlayerChunkState> wrappedPlayerChunkState) {
        playerChunkState.clear();
        if (wrappedPlayerChunkState == null) return;
        for (Map.Entry<String, PlayerChunkState> entry : wrappedPlayerChunkState.entrySet()) {
            try {
                addPlayerInProtectedChunks(UUID.fromString(entry.getKey()), entry.getValue());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Skipped invalid saved player chunk state UUID: " + entry.getKey());
            }
        }
    }

    private final double WEAK_AMOUNT_COEFFICIENT;
    public int getMAX_WEAK_CHUNKS(FactionObject faction) {
        return (int) Math.round(WEAK_AMOUNT_COEFFICIENT * faction.getPower());
    }


    public void ClaimLand(FactionObject faction, UUID playerUUID) {
        Player player = plugin.factionManager.factionHelperService.checkPlayer(playerUUID);
        if (player == null) {
            plugin.getLogger().warning("Something went wrong when trying to find the player with the UUID: " + playerUUID);
            return;
        }

        Chunk chunkToClaim = player.getLocation().getChunk();

        // Check if the Chunk is in faction land (needs to be beside another claimed chunk)
        if (!faction.getHardClaimedChunks().isEmpty() && !plugin.factionManager.factionHelperService.isChunkInLand(faction.getHardClaimedChunks(), chunkToClaim)) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You have to claim a chunk beside another claimed chunk!");
            return;
        }

        // Can only claim in the overworld
        if (!Objects.equals(Bukkit.getWorld("world"), player.getWorld())){
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You can only claim land in the overworld!");
            return;
        }


        // Check if the player is claiming in a valid area (not already claimed)
        if (getLinkedChunks().containsKey(chunkToClaim)) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The chunk you are trying to claim is already claimed!");
            return;
        }

        boolean weakClaim = false;
        if (faction.getHardClaimedChunks().size() < faction.getPower()) {

            // Link Chunk to Faction
            faction.getHardClaimedChunks().add(chunkToClaim);

        } else if (faction.getWeakClaimedChunks().size() <= getMAX_WEAK_CHUNKS(faction)) {
            if (!plugin.getConfig().getBoolean("faction.object.weak-claims-enabled")) {
                return;
            }

            // Link Raidable Chunk to Faction
            faction.getWeakClaimedChunks().add(chunkToClaim);
            weakClaim = true;

        } else {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Your faction is out of chunks to claim!");
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
            plugin.factionManager.factionMapRenderService.RedrawClaims(faction);
        }

        // Update chunk state of all online players, since it now changed
        for (Player onlinePlayer : chunkToClaim.getPlayersSeeingChunk()) {
            if (onlinePlayer.getLocation().getChunk().equals(chunkToClaim)) {
                plugin.factionManager.factionHelperService.updatePlayerChunkState(onlinePlayer.getUniqueId(), chunkToClaim);

            }
        }

        player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "You have claimed this chunk for your faction!");
    }

    public void UnClaimLand(FactionObject faction, UUID playerUUID) {
        Player player = plugin.factionManager.factionHelperService.checkPlayer(playerUUID);
        if (player == null) {
            plugin.getLogger().warning("Something went wrong when trying to find the player with the UUID: " + playerUUID);
            return;
        }

        Chunk chunkToCheck = player.getLocation().getChunk();

        // Can only unclaim in the overworld
        if (!Objects.equals(Bukkit.getWorld("world"), player.getWorld())) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You can only unclaim land in the overworld!");
            return;
        }

        // Check if the player is unclaiming in a valid area (standing on a claimed chunk and claimed by his faction)
        if (!faction.getHardClaimedChunks().contains(chunkToCheck)) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The chunk you are trying to unclaim is not claimed by your faction!");
            return;
        }

        // Remove Chunk from the FactionManagers claimedChunkCache
        removeLinkedChunk(chunkToCheck);

        // Change player state
        addPlayerInProtectedChunks(playerUUID, PlayerChunkState.WILDERNESS);

        // Unlink Chunk to Faction
        faction.removeHardClaimedChunks(chunkToCheck);

        if (plugin.factionManager.factionMapRenderService.getUSE_BLUEMAP_ADDON()) {
            plugin.factionManager.factionMapRenderService.RedrawClaims(faction);
        }
        player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "You have unclaimed this chunk for your faction!");
    }
}
