package com.gus.simpleFactions.FactionHandlers;

import com.gus.simpleFactions.FactionHandlers.FactionObjectServices.*;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

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
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) {
            // Log but don't crash - player might have logged off
            return;
        }
        
        if (factionMembershipService.getPlayerFactionLink().containsKey(playerUUID)) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You already have a faction!");
            return;
        }

        if (factionName == null || factionName.isBlank() || factionName.length() > 32) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Faction names must be between 1 and 32 characters!");
            return;
        }

        if (factionHelperService.factionNameExists(factionName)){
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "This faction name already exists!");
            return;
        }

        if (factionHelperService.teamNameTaken(factionName)){
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "This faction name is too similar to an existing faction, please pick another!");
            return;
        }

        FactionObject newFaction = new FactionObject(playerUUID, factionName, plugin.getConfig().getInt("faction.object.base-faction-power"), null);
        factionMembershipService.addExistingFaction(newFaction);
        factionMembershipService.addPlayerFactionLink(playerUUID, newFaction);

        // Give the faction a random starting color, and remember it - this is also what
        // the faction's BlueMap claim markers are colored with (see FactionMapRenderService).
        ArrayList<String> factionColors = new ArrayList<>(List.of(String.format("#%06X", (int) (Math.random() * 0xFFFFFF))));
        newFaction.setFactionColors(factionColors);
        factionHelperService.createTabTeam(playerUUID, factionName, null, factionColors);

        player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "You have created a new faction: " + factionName + "!");

        plugin.factionManager.factionRankService.CreateFactionRank(newFaction, "OWNER", player);
        plugin.factionManager.factionRankService.AddPlayerToRank(newFaction, player, "OWNER");
        for (String perm : new ArrayList<>(List.of(
                "simplefactions.invite",
                "simplefactions.kick",
                "simplefactions.disband",
                "simplefactions.claim",
                "simplefactions.unclaim",
                "simplefactions.raid",
                "simplefactions.raid.select",
                "simplefactions.prefix",
                "simplefactions.home.set",
                "simplefactions.rank.create",
                "simplefactions.rank.delete",
                "simplefactions.rank.manage.info",
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
        plugin.factionManager.factionRankService.AddPermissionRank(newFaction, "MEMBER", "simplefactions.invite");
        plugin.factionManager.factionRankService.AddPermissionRank(newFaction, "MEMBER", "simplefactions.home");

    }

    /**
     * DEBUG ONLY (see config.yml debug.wipe-factions-on-start). Wipes every faction, claim,
     * invite, permission attachment, and raid from memory - meant to be called right after
     * JsonHandler#LoadSequence during onEnable, so the server starts from a clean slate for
     * testing without needing to delete the data files by hand.
     */
    public void WipeAllFactionData() {
        // Unregister scoreboard teams and BlueMap markers before we forget which factions owned them
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        for (FactionObject faction : factionMembershipService.getExistingFactions()) {
            if (scoreboardManager != null) {
                Team team = scoreboardManager.getMainScoreboard().getTeam(factionFormatterService.toFullTeamName(faction.getFactionName()));
                if (team != null) team.unregister();
            }
            if (factionMapRenderService.getUSE_BLUEMAP_ADDON()) {
                factionMapRenderService.RemoveFactionFromMap(faction);
            }
        }

        // Release every permission attachment handed out so far
        for (UUID playerUUID : new ArrayList<>(factionRankService.getPerms().keySet())) {
            factionRankService.removeAttachment(playerUUID);
        }

        factionMembershipService.getExistingFactions().clear();
        factionMembershipService.getPlayerFactionLink().clear();
        factionMembershipService.getPendingFactionInvites().clear();

        factionLandService.getLinkedChunks().clear();
        factionLandService.getPlayerChunkState().clear();

        plugin.raidManager.getWaitingRaids().clear();
        plugin.raidManager.getCurrentRaids().clear();
        plugin.raidManager.getCurrentFactionSelection().clear();

        plugin.getLogger().warning("[DEBUG] Wiped all faction data on startup (debug.wipe-factions-on-start is enabled in config.yml)");
    }
}
