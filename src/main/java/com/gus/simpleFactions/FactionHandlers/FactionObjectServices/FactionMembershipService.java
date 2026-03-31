package com.gus.simpleFactions.FactionHandlers.FactionObjectServices;

import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class FactionMembershipService {

    private final SimpleFactions plugin;
    public FactionMembershipService(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    private record FactionInvite(UUID invitedPlayer,  FactionObject invitingFaction){}


    private ArrayList<FactionObject> existingFactions = new ArrayList<>();
    public ArrayList<FactionObject> getExistingFactions() {
        return this.existingFactions;
    }
    public void addExistingFaction(FactionObject faction) {
        this.existingFactions.add(faction);
    }
    public void removeExistingFaction(FactionObject faction) {
        this.existingFactions.remove(faction);
    }


    private Map<UUID, FactionObject> playerFactionLink = new HashMap<>();
    public Map<UUID, FactionObject> getPlayerFactionLink() {
        return this.playerFactionLink;
    }
    public void addPlayerFactionLink(UUID playerUUID, FactionObject faction) {
        this.playerFactionLink.put(playerUUID, faction);
    }
    public void removePlayerFactionLink(UUID playerUUID) {
        this.playerFactionLink.remove(playerUUID);
    }


    private ArrayList<FactionInvite> pendingFactionInvites = new ArrayList<>();
    public ArrayList<FactionInvite> getPendingFactionInvites() {
        return this.pendingFactionInvites;
    }
    public void addPendingFactionInvite(FactionInvite invite) {
        this.pendingFactionInvites.add(invite);
    }
    public void removePendingFactionInvite(FactionInvite invite) {
        this.pendingFactionInvites.remove(invite);
    }

    private HashMap<UUID, Inventory> currentFactionInv = new HashMap<>();
    public HashMap<UUID, Inventory> getCurrentFactionInv() {
        return this.currentFactionInv;
    }
    public void addCurrentFactionInv(UUID playerUUID, Inventory inv) {
        this.currentFactionInv.put(playerUUID, inv);
    }
    public void removeCurrentFactionInv(UUID playerUUID) {
        this.currentFactionInv.remove(playerUUID);
    }


    public void KickPlayer(FactionObject faction, UUID playerUUID, boolean sendMsg) {
        Player player = plugin.factionManager.factionHelperService.checkPlayer(playerUUID);
        if (player == null) {
            System.out.println("Something went wrong when trying to find the player with the UUID: " + playerUUID);
            return;
        }

        // Check if the player is in the faction
        if (!faction.getFactionMembers().contains(playerUUID) || !getPlayerFactionLink().containsKey(playerUUID)) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The player you are trying to kick is not in your faction !");
            return;
        }
        // Remove him from the faction, Faction side
        faction.removeFactionMember(playerUUID);

        // Remove him from the faction, Manager side
        removePlayerFactionLink(playerUUID);

        if (sendMsg) player.sendMessage("§2You have been kicked of the faction: " + faction.getFactionName() + " !");
    }

    public void LeaveFaction(FactionObject faction, UUID playerUUID) {
        Player player = plugin.factionManager.factionHelperService.checkPlayer(playerUUID);
        if (player == null) {
            System.out.println("Something went wrong when trying to find the player with the UUID: " + playerUUID);
            return;
        }

        // Check if the player is not the Owner of the Faction
        if (playerUUID.equals(faction.getOwner())) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You can't leave your own faction !");
            return;
        }

        // Remove player from FactionManagers FactionPlayerLink ArrayList
        removePlayerFactionLink(playerUUID);

        // Remove player from the team
        Objects.requireNonNull(Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard().getTeam(plugin.factionManager.factionFormatterService.toTeamName(faction.getFactionName()))).removeEntry(player.getName());

        // Remove player from the Factions member list
        faction.removeFactionMember(playerUUID);

        player.sendMessage(ChatColor.RED + ChatColor.ITALIC.toString() + "You have left faction the faction " + faction.getFactionName());
    }

    public void DisbandFaction(FactionObject faction, UUID playerUUID) {
        Player player = plugin.factionManager.factionHelperService.checkPlayer(playerUUID);
        if (player == null) {
            System.out.println("Something went wrong when trying to find the player with the UUID: " + playerUUID);
            return;
        }

        // Check if the player is the Owner of the Faction
        if (!playerUUID.equals(faction.getOwner())) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You can only disband your faction if you are its owner !");
            return;
        }

        // Remove EVERY player of the Faction from FactionManagers FactionPlayerLink ArrayList
        for (UUID uuid : getPlayerFactionLink().keySet()){
            if (faction.getFactionMembers().contains(uuid)){
                removePlayerFactionLink(uuid);
            }
        }

        // Remove EVERY player from the Factions member list
        for (UUID uuid : faction.getFactionMembers()){
            faction.removeFactionMember(uuid);
        }

        // Unclaim every claimed land (and weak chunks)
        for (Chunk chunk : plugin.factionManager.factionLandService.getLinkedChunks().keySet()){
            if (plugin.factionManager.factionLandService.getLinkedChunks().get(chunk).equals(faction)){
                plugin.factionManager.factionLandService.removeLinkedChunk(chunk);
            }
        }

        Objects.requireNonNull(Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard().getTeam(plugin.factionManager.factionFormatterService.toTeamName(faction.getFactionName()))).unregister();

        // Remove this faction from the factions lists in the FactionManager
        removeExistingFaction(faction);

        player.sendMessage(ChatColor.RED + ChatColor.ITALIC.toString() + "You have, as faction owner, disbanded your faction " + faction.getFactionName());
    }

    public void TeleportHome(FactionObject faction, UUID playerUUID) {
        Player player = plugin.factionManager.factionHelperService.checkPlayer(playerUUID);
        if (player == null) {
            System.out.println("Something went wrong when trying to find the player with the UUID: " + playerUUID);
            return;
        }

        // There is no home setup yet
        if (faction.getFactionHome() == null) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You don't have a home set for your faction !\n Set one with /f home set");
            return;
        }

        // Use a Bukkit Runnable
        plugin.teleportManager.StartTeleport(playerUUID, 5, faction.getFactionHome());
    }

    public void SetHome(FactionObject faction, UUID playerUUID) {
        Player player = plugin.factionManager.factionHelperService.checkPlayer(playerUUID);
        if (player == null) {
            System.out.println("Something went wrong when trying to find the player with the UUID: " + playerUUID);
            return;
        }

        // Same home set, cancel set home
        if (player.getLocation().equals(faction.getFactionHome())) {
            Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The new home you are trying to set is the same as your current one !");
            return;
        }

        // Heck if the wanted home is in the claimed chunks of the faction
        if (faction.getHardClaimedChunks().isEmpty() || !faction.getHardClaimedChunks().contains(player.getLocation().getChunk())) {
            Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The home you are trying to set is not in the claimed chunks of your faction !");
            return;
        }

        // Remove and set the new Faction home
        faction.setFactionHome(player.getLocation());

        player.sendMessage("§2You have, set the home of your faction !");
    }

    public void JoinFaction(UUID playerUUID, String factionName){
        // Check if an invitation is pending
        for (FactionInvite invite : pendingFactionInvites) {
            if (invite.invitingFaction.getFactionName().equals(factionName) && invite.invitedPlayer.equals(playerUUID)) {
                // If the player already has a faction, kick them from it
                if (playerFactionLink.containsKey(playerUUID)){
                    KickPlayer(playerFactionLink.get(playerUUID), playerUUID, false);
                }

                // Add player in the faction, Faction side
                invite.invitingFaction.getFactionMembers().add(playerUUID);

                // Add player in the faction, Manager side
                playerFactionLink.put(playerUUID, invite.invitingFaction);

                // Add power for the faction
                invite.invitingFaction.setPower(invite.invitingFaction.getPower() + plugin.getConfig().getInt("faction.object.base-faction-power-per-member"));

                // Add player to the team
                plugin.factionManager.factionHelperService.addPlayerToTabTeam(playerUUID, invite.invitingFaction.getFactionName());

                plugin.factionManager.factionRankService.AddPlayerToRank(invite.invitingFaction, plugin.factionManager.factionHelperService.checkPlayer(playerUUID), "MEMBER");

                plugin.factionManager.factionHelperService.checkPlayer(playerUUID).sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "You have joined the faction " + invite.invitingFaction.getFactionName() + " !");
                return;
            }
        }
        plugin.factionManager.factionHelperService.checkPlayer(playerUUID).sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You have not been invited to join this faction");
    }

    public void InvitePlayer(UUID senderUUID, UUID playerUUID, FactionObject invitingFaction){
        for (FactionInvite invite : pendingFactionInvites){
            if (invite.invitingFaction.equals(invitingFaction) && invite.invitedPlayer.equals(playerUUID)) {
                if (plugin.factionManager.factionHelperService.checkPlayer(senderUUID) == null) continue;
                plugin.factionManager.factionHelperService.checkPlayer(senderUUID).sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "This player already has been invited to join your faction");
                return;
            }
        }
        pendingFactionInvites.add(new FactionInvite(playerUUID, invitingFaction));

        // Send confirmations
        if (plugin.factionManager.factionHelperService.checkPlayer(playerUUID) != null)
            plugin.factionManager.factionHelperService.checkPlayer(playerUUID).sendMessage(
                    ChatColor.GREEN + ChatColor.BOLD.toString() + "You have been invited to join " + invitingFaction.getFactionName() + " !");

        if (plugin.factionManager.factionHelperService.checkPlayer(senderUUID) != null &&
                plugin.factionManager.factionHelperService.checkPlayer(playerUUID) != null)
            plugin.factionManager.factionHelperService.checkPlayer(senderUUID).sendMessage(
                    ChatColor.GRAY + "You have invited " + ChatColor.ITALIC + Bukkit.getPlayer(playerUUID).getName() + ChatColor.RESET + ChatColor.GRAY + " to join " + invitingFaction.getFactionName() + " !");
    }

    public ArrayList<FactionObject> getPlayerInvites(UUID playerUUID){
        ArrayList<FactionObject> factions = new ArrayList<>();
        for (FactionInvite invite : pendingFactionInvites){
            if (invite.invitedPlayer.equals(playerUUID)) factions.add(invite.invitingFaction);
        }
        return factions;
    }
}
