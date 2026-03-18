package com.gus.simpleFactions.FactionHandlers.FactionObjectServices;

import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public class FactionMembershipService {

    private final SimpleFactions plugin;
    public FactionMembershipService(SimpleFactions plugin) {
        this.plugin = plugin;
    }

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

        // Remove player from the team
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
}
