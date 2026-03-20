package com.gus.simpleFactions.FactionHandlers;

import com.gus.simpleFactions.FactionHandlers.FactionObjectServices.*;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.*;

public class FactionManager {

    private final SimpleFactions plugin;
    public FactionManager(SimpleFactions plugin) {
        this.plugin = plugin;

        factionFormatterService = new FactionFormatterService(plugin);
        factionHelperService = new FactionHelperService(plugin);
        factionLandService = new FactionLandService(plugin);
        factionMapRenderService = new FactionMapRenderService(plugin);
        factionMembershipService = new FactionMembershipService(plugin);
        factionRankService = new FactionRankService(plugin);
    }

    public FactionFormatterService factionFormatterService;
    public FactionHelperService factionHelperService;
    public FactionLandService factionLandService;
    public FactionMapRenderService factionMapRenderService;
    public FactionMembershipService factionMembershipService;
    public FactionRankService factionRankService;

    public void CreateFaction(UUID playerUUID, String factionName){
        if (factionMembershipService.getPlayerFactionLink().containsKey(playerUUID)) {
            Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You already have a faction !");
            return;
        }

        if (factionHelperService.factionNameExists(factionName)){
            Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "This faction name already exists !");
            return;
        }

        FactionObject newFaction = new FactionObject(playerUUID, factionName, plugin.getConfig().getInt("faction.object.base-faction-power"));
        factionMembershipService.addExistingFaction(newFaction);
        factionMembershipService.addPlayerFactionLink(playerUUID, newFaction);
        factionHelperService.createTabTeam(playerUUID, factionName, null, new ArrayList<>(List.of("#77777")));

        Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).sendMessage("You have created a new faction: " + factionName + " !");
    }
}
