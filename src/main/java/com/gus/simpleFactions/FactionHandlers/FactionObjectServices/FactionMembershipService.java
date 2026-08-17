package com.gus.simpleFactions.FactionHandlers.FactionObjectServices;

import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.Json.FactionObjectWrapper;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.util.*;

public class FactionMembershipService {

    private final SimpleFactions plugin;
    public FactionMembershipService(SimpleFactions plugin) {
        this.plugin = plugin;
    }


    private record FactionInvite(UUID invitedPlayer,  FactionObject invitingFaction){}
    public record FactionInviteWrapper(UUID invitedPlayer,  String invitingFaction){}


    private ArrayList<FactionObject> existingFactions = new ArrayList<>();
    public ArrayList<FactionObject> getExistingFactions() {
        return existingFactions;
    }
    public void addExistingFaction(FactionObject faction) {
        existingFactions.add(faction);
    }
    public void removeExistingFaction(FactionObject faction) {
        existingFactions.remove(faction);
    }
    public ArrayList<FactionObjectWrapper> getWrappedExistingFaction() {
        ArrayList<FactionObjectWrapper> returnArray = new ArrayList<>();
        for (FactionObject faction : existingFactions) {
            returnArray.add(new FactionObjectWrapper(faction));
        }
        return returnArray;
    }
    public void unWrapExistingFaction(ArrayList<FactionObjectWrapper> factionList) {
        existingFactions.clear();
        if (factionList == null) return;
        for (FactionObjectWrapper factionObjectWrapper : factionList) {
            if (factionObjectWrapper == null || factionObjectWrapper.getOwner() == null || factionObjectWrapper.getName() == null) continue;
            try {
                existingFactions.add(new FactionObject(null, null, null, factionObjectWrapper));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Skipped invalid saved faction: " + factionObjectWrapper.getName());
            }
        }
    }


    private Map<UUID, FactionObject> playerFactionLink = new HashMap<>();
    public Map<UUID, FactionObject> getPlayerFactionLink() {
        return playerFactionLink;
    }
    public void addPlayerFactionLink(UUID playerUUID, FactionObject faction) {
        this.playerFactionLink.put(playerUUID, faction);
    }
    public void removePlayerFactionLink(UUID playerUUID) {
        playerFactionLink.remove(playerUUID);
    }
    public Map<String, String> getWrappedPlayerFactionLink() {
        Map<String, String> returnMap = new HashMap<>();
        for (Map.Entry<UUID, FactionObject> entry : playerFactionLink.entrySet()) {
            returnMap.put(entry.getKey().toString(), entry.getValue().getFactionName());
        }
        return returnMap;
    }
    public void unWrapPlayerFactionLink(Map<String, String> playerFactionLink) {
        this.playerFactionLink.clear();
        if (playerFactionLink == null) return;
        for (Map.Entry<String, String> entry : playerFactionLink.entrySet()) {
            try {
                Optional<FactionObject> faction = existingFactions.stream().filter(existingFaction -> existingFaction.getFactionName().equals(entry.getValue())).findFirst();
                faction.ifPresent(factionObject -> addPlayerFactionLink(UUID.fromString(entry.getKey()), factionObject));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Skipped invalid saved player faction link for UUID: " + entry.getKey());
            }
        }
    }


    private ArrayList<FactionInvite> pendingFactionInvites = new ArrayList<>();
    public ArrayList<FactionInvite> getPendingFactionInvites() {
        return pendingFactionInvites;
    }
    public ArrayList<FactionInviteWrapper> getWrappedPendingFactionInvites() {
        ArrayList<FactionInviteWrapper> returnArray = new ArrayList<>();
        for (FactionInvite invite : pendingFactionInvites) {
            returnArray.add(new FactionInviteWrapper(invite.invitedPlayer, invite.invitingFaction.getFactionName()));
        }
        return returnArray;
    }
    public void unWrapPendingFactionInvites(ArrayList<FactionInviteWrapper> pendingFactionInvites) {
        this.pendingFactionInvites.clear();
        if (pendingFactionInvites == null) return;
        for (FactionInviteWrapper invite : pendingFactionInvites) {
            Optional<FactionObject> faction = existingFactions.stream().filter(existingFaction -> existingFaction.getFactionName().equals(invite.invitingFaction)).findFirst();
            faction.ifPresent(factionObject -> this.pendingFactionInvites.add(new FactionInvite(invite.invitedPlayer, factionObject)));
        }
    }

    public void KickPlayer(FactionObject faction, UUID playerUUID, boolean sendMsg) {
        Player player = plugin.factionManager.factionHelperService.checkPlayer(playerUUID);
        if (player == null) {
            plugin.getLogger().warning("Something went wrong when trying to find the player with the UUID: " + playerUUID);
            return;
        }

        // Check if the player is in the faction
        if (!faction.getFactionMembers().contains(playerUUID) || !getPlayerFactionLink().containsKey(playerUUID)) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The player you are trying to kick is not in your faction!");
            return;
        }
        // Remove him from the faction, Faction side
        faction.removeFactionMember(playerUUID);

        // Remove him from the faction, Manager side
        removePlayerFactionLink(playerUUID);

        if (sendMsg) player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "You have been kicked from the faction: " + faction.getFactionName() + "!");
    }

    public void LeaveFaction(FactionObject faction, UUID playerUUID) {
        Player player = plugin.factionManager.factionHelperService.checkPlayer(playerUUID);
        if (player == null) {
            plugin.getLogger().warning("Something went wrong when trying to find the player with the UUID: " + playerUUID);
            return;
        }

        // Check if the player is not the Owner of the Faction
        if (playerUUID.equals(faction.getOwner())) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You can't leave your own faction!");
            return;
        }

        // Remove player from FactionManagers FactionPlayerLink ArrayList
        removePlayerFactionLink(playerUUID);

        // Remove player from the team
        removePlayerFromScoreboardTeam(faction, player);

        // Remove player from the Factions member list
        faction.removeFactionMember(playerUUID);

        player.sendMessage(ChatColor.YELLOW + "You have left the faction " + faction.getFactionName() + "!");
    }

    public void DisbandFaction(FactionObject faction, UUID playerUUID) {
        Player player = plugin.factionManager.factionHelperService.checkPlayer(playerUUID);
        if (player == null) {
            plugin.getLogger().warning("Something went wrong when trying to find the player with the UUID: " + playerUUID);
            return;
        }

        // Check if the player is the Owner of the Faction
        if (!playerUUID.equals(faction.getOwner())) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You can only disband your faction if you are its owner!");
            return;
        }

        // Remove EVERY player of the Faction from FactionManagers FactionPlayerLink ArrayList
        // (iterate over a copy of the key set, since removePlayerFactionLink mutates the live map)
        for (UUID uuid : new ArrayList<>(getPlayerFactionLink().keySet())){
            if (faction.getFactionMembers().contains(uuid)){
                removePlayerFactionLink(uuid);
            }
        }

        // Remove EVERY player from the Factions member list
        for (UUID uuid : faction.getFactionMembers()){
            faction.removeFactionMember(uuid);
        }

        // Unclaim every claimed land (and weak chunks)
        // (iterate over a copy of the key set, since removeLinkedChunk mutates the live map)
        for (Chunk chunk : new ArrayList<>(plugin.factionManager.factionLandService.getLinkedChunks().keySet())){
            if (faction.equals(plugin.factionManager.factionLandService.getLinkedChunks().get(chunk))){
                plugin.factionManager.factionLandService.removeLinkedChunk(chunk);
            }
        }
        faction.getHardClaimedChunks().clear();
        faction.getWeakClaimedChunks().clear();

        // Remove any BlueMap markers, otherwise the disbanded faction's territory keeps showing
        // on the map forever ("ghost" claims that nothing owns anymore).
        if (plugin.factionManager.factionMapRenderService.getUSE_BLUEMAP_ADDON()) {
            plugin.factionManager.factionMapRenderService.RemoveFactionFromMap(faction);
        }

        unregisterScoreboardTeam(faction);

        // Remove this faction from the factions lists in the FactionManager
        removeExistingFaction(faction);

        player.sendMessage(ChatColor.YELLOW + "You have disbanded your faction " + faction.getFactionName() + "!");
    }

    // Null-safe scoreboard team helpers (the team may legitimately be missing if creation failed earlier)
    private void removePlayerFromScoreboardTeam(FactionObject faction, Player player) {
        var scoreboardManager = Bukkit.getScoreboardManager();
        if (scoreboardManager == null) return;
        var team = scoreboardManager.getMainScoreboard().getTeam(plugin.factionManager.factionFormatterService.toFullTeamName(faction.getFactionName()));
        if (team == null) {
            plugin.getLogger().warning("No scoreboard team found for faction: " + faction.getFactionName());
            return;
        }
        team.removeEntry(player.getName());
    }

    private void unregisterScoreboardTeam(FactionObject faction) {
        var scoreboardManager = Bukkit.getScoreboardManager();
        if (scoreboardManager == null) return;
        var team = scoreboardManager.getMainScoreboard().getTeam(plugin.factionManager.factionFormatterService.toFullTeamName(faction.getFactionName()));
        if (team == null) {
            plugin.getLogger().warning("No scoreboard team found for faction: " + faction.getFactionName());
            return;
        }
        team.unregister();
    }

    public void TeleportHome(FactionObject faction, UUID playerUUID) {
        Player player = plugin.factionManager.factionHelperService.checkPlayer(playerUUID);
        if (player == null) {
            plugin.getLogger().warning("Something went wrong when trying to find the player with the UUID: " + playerUUID);
            return;
        }

        // There is no home setup yet
        if (faction.getFactionHome() == null) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You don't have a home set for your faction! Set one with /f home set");
            return;
        }
        // Use a Bukkit Runnable
        plugin.teleportManager.StartTeleport(playerUUID, 5, faction.getFactionHome());
    }

    public void SetHome(FactionObject faction, UUID playerUUID) {
        Player player = plugin.factionManager.factionHelperService.checkPlayer(playerUUID);
        if (player == null) {
            plugin.getLogger().warning("Something went wrong when trying to find the player with the UUID: " + playerUUID);
            return;
        }

        // Same home set, cancel set home
        if (player.getLocation().equals(faction.getFactionHome())) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The new home you are trying to set is the same as your current one!");
            return;
        }

        // Check if the wanted home is in the claimed chunks of the faction
        if (faction.getHardClaimedChunks().isEmpty() || !faction.getHardClaimedChunks().contains(player.getLocation().getChunk())) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The home you are trying to set is not in the claimed chunks of your faction!");
            return;
        }

        // Remove and set the new Faction home
        faction.setFactionHome(player.getLocation());

        player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "You have set the home of your faction!");
    }

    public void JoinFaction(UUID playerUUID, String factionName){
        // Check if an invitation is pending
        Iterator<FactionInvite> iterator = pendingFactionInvites.iterator();
        while (iterator.hasNext()) {
            FactionInvite invite = iterator.next();
            if (invite.invitingFaction.getFactionName().equals(factionName) && invite.invitedPlayer.equals(playerUUID)) {
                // Consume the invite now that it's being accepted, otherwise it lingers forever
                // and can be "reused" to rejoin later without a fresh invite.
                iterator.remove();

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

                // Add player to the faction rank
                plugin.factionManager.factionRankService.AddPlayerToRank(invite.invitingFaction, plugin.factionManager.factionHelperService.checkPlayer(playerUUID), "MEMBER");

                // Confirmation message
                plugin.factionManager.factionHelperService.checkPlayer(playerUUID).sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "You have joined the faction " + invite.invitingFaction.getFactionName() + "!");
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
                    ChatColor.GREEN + ChatColor.BOLD.toString() + "You have been invited to join " + invitingFaction.getFactionName() + "!");

        if (plugin.factionManager.factionHelperService.checkPlayer(senderUUID) != null &&
                plugin.factionManager.factionHelperService.checkPlayer(playerUUID) != null)
            plugin.factionManager.factionHelperService.checkPlayer(senderUUID).sendMessage(
                    ChatColor.GRAY + "You have invited " + ChatColor.ITALIC + plugin.factionManager.factionHelperService.checkPlayer(playerUUID).getName() + ChatColor.RESET + ChatColor.GRAY + " to join " + invitingFaction.getFactionName() + " !");
    }

    public ArrayList<FactionObject> getPlayerInvites(UUID playerUUID){
        ArrayList<FactionObject> factions = new ArrayList<>();
        for (FactionInvite invite : pendingFactionInvites){
            if (invite.invitedPlayer.equals(playerUUID)) factions.add(invite.invitingFaction);
        }
        return factions;
    }
}
