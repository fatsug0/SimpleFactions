package com.gus.simpleFactions.FactionHandlers;

import com.gus.simpleFactions.Enums.PlayerChunkState;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class FactionManager {

    private final SimpleFactions plugin;
    public FactionManager(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    public record FactionInvite(UUID invitedPlayer,  FactionObject invitingFaction){}

    public ArrayList<FactionObject> existingFactions = new ArrayList<>();
    public Map<UUID, FactionObject> playerFactionLink = new HashMap<>();
    public ArrayList<FactionInvite> pendingFactionInvites = new ArrayList<>();

    public Map<Chunk, FactionObject> linkedChunks = new HashMap<>();
    public Map<UUID, PlayerChunkState> playerInProtectedChunks = new HashMap<>();

    // BlueMap related
    public Map<Chunk, Integer> bluemapClaimedChunk = new HashMap<>();

    public void CreateFaction(UUID playerUUID, String factionName){
        if (playerFactionLink.containsKey(playerUUID)) {
            Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You already have a faction !");
            return;
        }

        if (factionNameExists(factionName)){
            Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "This faction name already exists !");
            return;
        }

        FactionObject newFaction = new FactionObject(plugin, playerUUID, factionName, plugin.getConfig().getInt("faction.object.base-faction-power"), plugin.getConfig().getInt("faction.object.weak-amount-coefficient"));
        existingFactions.add(newFaction);
        playerFactionLink.put(playerUUID, newFaction);
        newFaction.createTabTeam(factionName, factionName, new ArrayList<>(List.of("#77777")));

        Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).sendMessage("You have created a new faction: " + factionName + " !");
    }

    public void JoinFaction(UUID playerUUID, FactionObject faction){
        // Check if an invitation is pending
        for (FactionInvite invite : pendingFactionInvites) {
            if (invite.invitingFaction.equals(faction) && invite.invitedPlayer.equals(playerUUID)) {
                // If the player already has a faction, kick them from it
                if (playerFactionLink.containsKey(playerUUID)){
                    playerFactionLink.get(playerUUID).KickPlayer(playerUUID);
                }

                // Add player in the faction, Faction side
                faction.getFactionMembers().add(playerUUID);

                // Add player in the faction, Manager side
                playerFactionLink.put(playerUUID, faction);

                // Add power for the faction
                faction.setPower(faction.getPower() + plugin.getConfig().getInt("faction.object.base-faction-power-per-member"));
                return;
            }
        }
        checkPlayer(playerUUID).sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You have not been invited to join this faction");
    }

    public void InvitePlayer(UUID senderUUID, UUID playerUUID, FactionObject invitedFaction){
        for (FactionInvite invite : pendingFactionInvites){
            if (invite.invitingFaction.equals(invitedFaction) && invite.invitedPlayer.equals(playerUUID)) {
                if (checkPlayer(senderUUID) == null) continue;
                checkPlayer(senderUUID).sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "This player already has been invited to join your faction");
                return;
            }
        }
        pendingFactionInvites.add(new FactionInvite(playerUUID, invitedFaction));

        // Send confirmations
        if (checkPlayer(playerUUID) != null) checkPlayer(playerUUID).sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "You have been invited to join " + invitedFaction.getFactionName() + " !");
        if (checkPlayer(senderUUID) != null && checkPlayer(playerUUID) != null) checkPlayer(senderUUID).sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "You have invited " + Bukkit.getPlayer(playerUUID).getName() + " to join " + invitedFaction.getFactionName() + " !");
    }

    public void SendHelp(UUID player){
        // Here we explain the use of the faction command
        Objects.requireNonNull(Bukkit.getPlayer(player)).sendMessage(
                "The Faction command use (faction / fac / f):\n" +
                        "/faction create <factionName> : Create a new faction\n>" +
                        "/faction join <factionName> : Join an existing faction\n>" +
                        "/faction invite <playerName> : Invite a player to join your faction\n>" +
                        "/faction kick <playerName> : Kick a player from your faction\n>" +
                        "/faction home [set] : Teleport to your faction home, use the set option to set it\n>" +
                        "/faction claim : Claim the chunk you currently are\n" +
                        "/faction unclaim : Unclaim the chunk you currently are\n" +
                        "/faction info : Get all the important info on your faction\n" +
                        "/faction leave <confirm> : Leave your faction, use the confirm option to confirm the action\n" +
                        "/faction disband <confirm> : Disband your faction, use the confirm option to confirm the action\n" +
                        "/faction disband : Disband your faction\n"
        );
    }

    public boolean CanInteractWithChunk(UUID playerUUID, Chunk chunk){

        // Chunk is in wilderness, everyone can interact with it
        if (!linkedChunks.containsKey(chunk)) return true;

        // Past this point, the chunk is claimed, need to determine by whom and if the player can interact with it
        // The player has no faction, he cannot interact with it
        if (!playerFactionLink.containsKey(playerUUID)) return false;

        // Past this point, The player has a faction, but maybe different from the chunk he's standing in
        // The player is in the same faction as the claimed chunk
        if (playerFactionLink.get(playerUUID).equals(linkedChunks.get(chunk))) return true;

        // Every outcome has been checked, but the faction is not equal as the claimed one, return false
        return false;
    }

    private boolean factionNameExists(String factionName){
        for (FactionObject faction : existingFactions){
            if (faction.getFactionName().equals(factionName)){
                return true;
            }
        }
        return false;
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
