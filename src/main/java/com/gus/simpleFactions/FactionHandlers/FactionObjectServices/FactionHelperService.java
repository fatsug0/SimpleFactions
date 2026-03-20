package com.gus.simpleFactions.FactionHandlers.FactionObjectServices;

import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class FactionHelperService {

    private final SimpleFactions plugin;
    public FactionHelperService(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    public boolean isChunkInLand(ArrayList<Chunk> getClaimedChunks, Chunk chunkToClaim) {
        World world = Bukkit.getWorld("world");
        if (world == null) return false;

        // Check on X axis
        if (getClaimedChunks.contains(world.getChunkAt(chunkToClaim.getX() - 1, chunkToClaim.getZ())))
            return true;
        if (getClaimedChunks.contains(world.getChunkAt(chunkToClaim.getX() + 1, chunkToClaim.getZ())))
            return true;

        // Check on Z axis
        if (getClaimedChunks.contains(world.getChunkAt(chunkToClaim.getX(), chunkToClaim.getZ() - 1)))
            return true;
        return getClaimedChunks.contains(world.getChunkAt(chunkToClaim.getX(), chunkToClaim.getZ() + 1));
    }

    public boolean isChunkHardClaimed(ArrayList<Chunk> getClaimedChunks, Chunk chunk) {
        return getClaimedChunks.contains(chunk);
    }

    public void createTabTeam(UUID ownerUUID, String teamName, String prefix, ArrayList<String> colors) {
        if (prefix == null) prefix = teamName;
        Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
        Player owner = Bukkit.getPlayer(ownerUUID);
        if (owner == null) return;

        // First create global team
        Team globalTeam = scoreboard.registerNewTeam("global_" + plugin.factionManager.factionFormatterService.toTeamName(teamName));
        String globalTeamPrefix = plugin.factionManager.factionFormatterService.useMiniMessage(" [" + prefix + "] ", colors);
        globalTeam.setPrefix(globalTeamPrefix);
        globalTeam.addEntry(owner.getName());

        // Then create faction team
        Team factionTeam = scoreboard.registerNewTeam("faction_" + plugin.factionManager.factionFormatterService.toTeamName(teamName));
        factionTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
        String factionTeamPrefix = plugin.factionManager.factionFormatterService.useMiniMessage(" [" + prefix + "] ", colors);
        factionTeam.setPrefix(factionTeamPrefix);
        factionTeam.addEntry(owner.getName());
    }

    public Player checkPlayer(UUID playerUUID) {
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

    public boolean CanInteractWithChunk(FactionObject faction, UUID playerUUID, Chunk chunk){

        // Chunk is in wilderness, everyone can interact with it
        if (!plugin.factionManager.factionLandService.getLinkedChunks().containsKey(chunk)) return true;

        // Past this point, the chunk is claimed, need to determine by whom and if the player can interact with it
        // The player has no faction, he cannot interact with it
        if (!plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(playerUUID)) return false;

        // Past this point, The player has a faction, but maybe different from the chunk he's standing in
        // The player is in the same faction as the claimed chunk
        if (plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(playerUUID).equals(plugin.factionManager.factionLandService.getLinkedChunks().get(chunk))) return true;

        // Every outcome has been checked, but the faction is not equal as the claimed one, return false
        return false;
    }

    public boolean factionNameExists(String factionName){
        for (FactionObject faction : plugin.factionManager.factionMembershipService.getExistingFactions()){
            if (faction.getFactionName().equals(factionName)){
                return true;
            }
        }
        return false;
    }
}
