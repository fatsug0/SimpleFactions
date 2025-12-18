package com.gus.simpleFactions;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FactionManager {

    private SimpleFactions plugin;
    public FactionManager(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    public record FactionInvite(UUID invitedPlayer,  FactionObject invitingFaction){}

    public ArrayList<FactionObject> existingFactions = new ArrayList<>();
    public Map<UUID, FactionObject> playerFactionLink = new HashMap<>();
    public ArrayList<FactionInvite> pendingFactionInvites = new ArrayList<>();

    public Map<Chunk, FactionObject> claimedChunks = new HashMap<>();
    public ArrayList<Chunk> protectedChunks = new ArrayList<>();

    public void CreateFaction(UUID player, String factionName){
        FactionObject newFaction = new FactionObject(plugin, player, factionName);
        existingFactions.add(newFaction);
        playerFactionLink.put(player, newFaction);

        Bukkit.getPlayer(player).sendMessage("You have created a new faction: " + factionName + " !");
    }

    public void JoinFaction(UUID player, String factionName){
        // If the player already has a faction, kick them from it
        if (playerFactionLink.containsKey(player)){
            playerFactionLink.get(player).KickPlayer(player);
        }
        for (FactionObject faction : existingFactions){
            if (faction.getFactionName().equals(factionName)){
                // Add player in the faction, Faction side
                faction.getFactionMembers().add(player);

                // Add player in the faction, Manager side
                playerFactionLink.put(player, faction);
            }
        }
    }

    public ArrayList<String> PlayerInvitations(UUID player){
        ArrayList<String> returnArray = new ArrayList<>();
        for(FactionInvite invite : this.pendingFactionInvites){
            if (invite.invitedPlayer().equals(player)){
                returnArray.add(invite.invitingFaction().getFactionName());
            }
        }
        if (returnArray.isEmpty()){
            return null;
        }
        return returnArray;
    }

    public String SendHelp(UUID player){
        // Here we explain the use of the faction command

        return "";
    }
}
