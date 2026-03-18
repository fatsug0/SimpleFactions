package com.gus.simpleFactions.FactionHandlers.FactionObjectServices;

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
        return this.getClaimedChunks().contains(world.getChunkAt(chunkToClaim.getX(), chunkToClaim.getZ() + 1));
    }

    public boolean isChunkHardClaimed(Chunk chunk) {
        return this.getClaimedChunks().contains(chunk);
    }

    public void createTabTeam(boolean discrete, String teamName, String prefix, ArrayList<String> colors) {
        Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
        Player owner = Bukkit.getPlayer(this.getOwner());
        if (owner == null) return;

        // First create global team
        Team globalTeam = scoreboard.registerNewTeam("global_" + toTeamName(teamName));
        String globalTeamPrefix = useMiniMessage(" [" + prefix + "] ", colors);
        globalTeam.setPrefix(globalTeamPrefix);

        Bukkit.getPlayer(this.getOwner());

        globalTeam.addEntry(owner.getName());

        // Then create faction team
        Team factionTeam = scoreboard.registerNewTeam("faction_" + toTeamName(teamName));
        factionTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
        String factionTeamPrefix = useMiniMessage(" [" + prefix + "] ", colors);
        factionTeam.setPrefix(factionTeamPrefix);

        Bukkit.getPlayer(this.getOwner());

        factionTeam.addEntry(owner.getName());
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
