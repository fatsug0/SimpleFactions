package com.gus.simpleFactions.FactionHandlers.FactionObjectServices;

import com.gus.simpleFactions.Enums.PlayerChunkState;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.SimpleFactions;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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

    public boolean PlayerIsInHisFaction(UUID uuid, Chunk chunkToCheck) {
        return (plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(uuid) &&
                plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(uuid).equals(plugin.factionManager.factionLandService.getLinkedChunks().get(chunkToCheck)));
    }

    public void createTabTeam(UUID ownerUUID, String teamName, String prefix, ArrayList<String> colors) {
        if (prefix == null) prefix = teamName;
        Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
        Player owner = Bukkit.getPlayer(ownerUUID);
        if (owner == null) return;

        // Create faction team
        Team factionTeam = scoreboard.registerNewTeam("faction_" + plugin.factionManager.factionFormatterService.toTeamName(teamName));
        String factionTeamPrefix = plugin.factionManager.factionFormatterService.useMiniMessage(" [" + prefix + "] ", colors);
        factionTeam.setPrefix(factionTeamPrefix);
        factionTeam.addEntry(owner.getName());
    }

    public void addPlayerToTabTeam(UUID playerUUID, String teamName) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) return;
        Team team = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard().getTeam("faction_" + plugin.factionManager.factionFormatterService.toTeamName(teamName));
        if (team == null) return;
        team.addEntry(player.getName());
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

    public boolean factionNameExists(String factionName){
        for (FactionObject faction : plugin.factionManager.factionMembershipService.getExistingFactions()){
            if (faction.getFactionName().equals(factionName)){
                return true;
            }
        }
        return false;
    }

    public void updatePlayerChunkState(UUID playerUUID, Chunk chunkToCheck){
        // Check if chunk is claimed
        if (plugin.factionManager.factionLandService.getLinkedChunks().containsKey(chunkToCheck)) {
            // Your faction claim
            if (plugin.factionManager.factionHelperService.PlayerIsInHisFaction(playerUUID, chunkToCheck)) {
                // Hard claimed
                if (plugin.factionManager.factionHelperService.isChunkHardClaimed(plugin.factionManager.factionLandService.getLinkedChunks().get(chunkToCheck).getHardClaimedChunks(), chunkToCheck)) {
                    if (plugin.factionManager.factionLandService.getPlayerChunkState().get(playerUUID) != PlayerChunkState.HARD) {
                        Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§6§lWelcome home (hard claim) !"));
                        plugin.factionManager.factionLandService.getPlayerChunkState().put(playerUUID, PlayerChunkState.HARD);
                    }
                } else { // Weak claimed
                    if (plugin.factionManager.factionLandService.getPlayerChunkState().get(playerUUID) != PlayerChunkState.WEAK) {
                        Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§6§lWelcome home (weak claim) !"));
                        plugin.factionManager.factionLandService.getPlayerChunkState().put(playerUUID, PlayerChunkState.WEAK);
                    }
                }

            } else { // Enemy faction claim
                if (plugin.factionManager.factionLandService.getPlayerChunkState().get(playerUUID) != PlayerChunkState.ENEMY) {
                    Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§4§lYou have entered the " + plugin.factionManager.factionLandService.getLinkedChunks().get(chunkToCheck).getFactionName() + " faction !"));
                    plugin.factionManager.factionLandService.getPlayerChunkState().put(playerUUID, PlayerChunkState.ENEMY);
                }
            }
        } else { // Not claimed, wilderness
            if (plugin.factionManager.factionLandService.getPlayerChunkState().get(playerUUID) != PlayerChunkState.WILDERNESS) {
                Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§2§lYou have entered wilderness !"));
                plugin.factionManager.factionLandService.getPlayerChunkState().put(playerUUID, PlayerChunkState.WILDERNESS);
            }
        }

    }
}
