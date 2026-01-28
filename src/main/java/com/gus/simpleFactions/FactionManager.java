package com.gus.simpleFactions;

import de.bluecolored.bluemap.api.math.Color;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;

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
    public Map<FactionObject, Color> factionClaimColorCache = new HashMap<>();

    public void CreateFaction(UUID player, String factionName){
        if (factionNameExists(factionName)){
            Objects.requireNonNull(Bukkit.getPlayer(player)).sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "This faction name already exists !");
            return;
        }

        var factionClaimColor = getRandomBlueMapColor();
        FactionObject newFaction = new FactionObject(plugin, player, factionName, factionClaimColor, plugin.getConfig().getInt("faction.object.base-faction-power"), plugin.getConfig().getInt("faction.object.weak-amount-coefficient"));
        existingFactions.add(newFaction);
        playerFactionLink.put(player, newFaction);
        newFaction.createTabTeam();
        factionClaimColorCache.put(newFaction, factionClaimColor);

        Objects.requireNonNull(Bukkit.getPlayer(player)).sendMessage("You have created a new faction: " + factionName + " !");
    }

    public void JoinFaction(UUID player, FactionObject faction){
        // Check if an invitation is pending
        if (!pendingFactionInvites.contains(new FactionInvite(player, faction))) {
            Objects.requireNonNull(Bukkit.getPlayer(player)).sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You are not invited to join this faction !");
            return;
        }

        // If the player already has a faction, kick them from it
        if (playerFactionLink.containsKey(player)){
            playerFactionLink.get(player).KickPlayer(player);
        }

        // Add player in the faction, Faction side
        faction.getFactionMembers().add(player);

        // Add player in the faction, Manager side
        playerFactionLink.put(player, faction);

        // Add power for the faction
        faction.setPower(faction.getPower() + plugin.getConfig().getInt("faction.object.base-faction-power-per-member"));
    }

    public void InvitePlayer(UUID player, FactionObject invitedFaction){
        if (pendingFactionInvites.contains(new FactionInvite(player, invitedFaction))) {
            Objects.requireNonNull(Bukkit.getPlayer(player)).sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You have already been invited by this faction !");
            return;
        }
        pendingFactionInvites.add(new FactionInvite(player, invitedFaction));
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

    public boolean CanInteractWithChunk(UUID player, Chunk chunk){

        // Chunk is in wilderness, everyone can interact with it
        if (!linkedChunks.containsKey(chunk)) return true;

        // Past this point, the chunk is claimed, need to determine by whom and if the player can interact with it
        // The player has no faction, he cannot interact with it
        if (!playerFactionLink.containsKey(player)) return false;

        // Past this point, The player has a faction, but maybe different from the chunk he's standing in
        // The player is in the same faction as the claimed chunk
        if (playerFactionLink.get(player).equals(linkedChunks.get(chunk))) return true;

        // Every outcome has been checked, but the faction is not equal as the claimed one, return false
        return false;
    }

    private Color getRandomBlueMapColor(){
        Random random = new Random();
        while (true){
            Color randomColor = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            if (!factionClaimColorCache.containsValue(randomColor)) {
                return randomColor;
            }
        }
    }

    private boolean factionNameExists(String factionName){
        for (FactionObject faction : existingFactions){
            if (faction.getFactionName().equals(factionName)){
                return true;
            }
        }
        return false;
    }
}
