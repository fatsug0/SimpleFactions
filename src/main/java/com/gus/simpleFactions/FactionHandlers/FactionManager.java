package com.gus.simpleFactions.FactionHandlers;

import com.gus.simpleFactions.FactionHandlers.FactionObjectServices.*;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.lang.reflect.Array;
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
        factionHelperService.createTabTeam(playerUUID, factionName, null, new ArrayList<>(List.of(String.format("#%06X", (int) (Math.random() * 0xFFFFFF)))));

        Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).sendMessage("You have created a new faction: " + factionName + " !");

        plugin.factionManager.factionRankService.CreateFactionRank(newFaction, "OWNER", Objects.requireNonNull(Bukkit.getPlayer(playerUUID)));
        for (String perm : new ArrayList<>(List.of(
                "simplefactions.invite",
                "simplefactions.kick",
                "simplefactions.disband",
                "simplefactions.claim",
                "simplefactions.unclaim",
                "simplefactions.toggle",
                "simplefactions.raid",
                "simplefactions.prefix",
                "simplefactions.home",
                "simplefactions.home.set",
                "simplefactions.rank.create",
                "simplefactions.rank.delete",
                "simplefactions.rank.manage.player.add",
                "simplefactions.rank.manage.player.remove",
                "simplefactions.rank.manage.player.list",
                "simplefactions.rank.manage.permissions.add",
                "simplefactions.rank.manage.permissions.remove",
                "simplefactions.rank.manage.permissions.list"
        ))) {
            plugin.factionManager.factionRankService.AddPermissionRank(newFaction, "OWNER", perm);
        }

        plugin.factionManager.factionRankService.CreateFactionRank(newFaction, "MEMBER", null);
        plugin.factionManager.factionRankService.AddPermissionRank(newFaction, "MEMBER", "simplefaction.invite");
        plugin.factionManager.factionRankService.AddPermissionRank(newFaction, "MEMBER", "simplefactions.home");
    }
}
