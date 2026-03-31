package com.gus.simpleFactions.FactionHandlers.FactionObjectServices;

import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionRankObject;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class FactionRankService {

    private final SimpleFactions plugin;
    public FactionRankService(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    private HashMap<UUID, PermissionAttachment> perms = new HashMap<>();

    public void CreateFactionRank(FactionObject faction, String rankName, Player player){
        for (FactionRankObject rank : faction.getFactionRanks()){
            if (rank.getRankName().equals(rankName)){
                if (player == null) {
                    System.out.println("The rank you are trying to create already exists !");
                    return;
                }
                player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The rank you are trying to create already exists !");
                return;
            }
        }
        faction.addFactionRank(new FactionRankObject(rankName));
    }

    public void DeleteFactionRank(FactionObject faction, String rankName, Player player){
        if (faction.getFactionRanks().isEmpty()) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "There is no rank to delete !");
            return;
        }

        FactionRankObject rank = getRank(faction, rankName);
        if (rank == null) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The rank you are trying to delete does not exist !");
            return;
        }

        for (UUID playerUUID : rank.getRankMembers()) {
            RemovePlayerFromRank(faction, plugin.factionManager.factionHelperService.checkPlayer(playerUUID), rankName);
        }

        faction.removeFactionRank(rank);
    }

    // Add & Remove Player to Rank
    public void RemovePlayerFromRank(FactionObject faction, Player player, String rankName){
        FactionRankObject rank = getRank(faction, rankName);
        if (rank == null) {
            System.out.println(ChatColor.RED + ChatColor.BOLD.toString() + "The rank you are trying to add the player to does not exist !");
            return;
        }

        if (!rank.getRankMembers().contains(player.getUniqueId())){
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Player is not in this rank !");
            return;
        }

        for (String perm : rank.getPermissions()){
            RemovePermToPlayer(player.getUniqueId(), perm);
        }

        rank.removeRankMember(player.getUniqueId());
    }
    public void AddPlayerToRank(FactionObject faction, Player player, String rankName){
        for (FactionRankObject rank : faction.getFactionRanks()){
            if (rank.getRankMembers().contains(player.getUniqueId())){
                System.out.println(ChatColor.RED + ChatColor.BOLD.toString() + "Player is already in a rank, " + rank.getRankName());
                return;
            }
        }

        FactionRankObject rank = getRank(faction, rankName);
        if (rank == null) {
            System.out.println(ChatColor.RED + ChatColor.BOLD.toString() + "The rank you are trying to add the player to does not exist !");
            return;
        }

        rank.addRankMember(player.getUniqueId());

        for (String perm : rank.getPermissions()){
            AddPermToPlayer(player.getUniqueId(), perm);
        }
    }

    // Add & Remove permission from rank
    public void AddPermissionRank(FactionObject faction, String rankName, String permission) {
        FactionRankObject rank = getRank(faction, rankName);
        if (rank == null) {
            System.out.println(ChatColor.RED + ChatColor.BOLD.toString() + "The rank you are trying to add the player to does not exist !");
            return;
        }

        rank.addPermission(permission);

        // Add perm for every player in the rank
        for (UUID playerUUID : rank.getRankMembers()) {
            AddPermToPlayer(playerUUID, permission);
        }
    }
    public void RemovePermissionRank(FactionObject faction, String rankName, String permission) {
        FactionRankObject rank = getRank(faction, rankName);
        if (rank == null) {
            System.out.println(ChatColor.RED + ChatColor.BOLD.toString() + "The rank you are trying to add the player to does not exist !");
            return;
        }

        rank.removePermission(permission);

        // Remove perm for every player in the rank
        for (UUID playerUUID : rank.getRankMembers()) {
            RemovePermToPlayer(playerUUID, permission);
        }
    }

    public void AddPermToPlayer(UUID playerUUID, String perm){
        Player player = plugin.factionManager.factionHelperService.checkPlayer(playerUUID);
        PermissionAttachment attachment;

        if (!perms.containsKey(playerUUID)) {
            attachment = player.addAttachment(plugin);
            perms.put(playerUUID, attachment);
        } else {
            attachment = perms.get(playerUUID);
        }

        if (!player.hasPermission(perm)) {
            attachment.setPermission(perm, true);
        }
        System.out.println("Perm added: " + perm);
    }

    public void RemovePermToPlayer(UUID playerUUID, String perm){
        Player player = plugin.factionManager.factionHelperService.checkPlayer(playerUUID);
        PermissionAttachment attachment;

        if (!perms.containsKey(playerUUID)) {
            attachment = player.addAttachment(plugin);
            perms.put(playerUUID, attachment);
        } else {
            attachment = perms.get(playerUUID);
        }

        if (player.hasPermission(perm)) {
            attachment.unsetPermission(perm);
        }
        System.out.println("Perm removed: " + perm);
    }

    public FactionRankObject getRank(FactionObject faction, String rankName){
        for (FactionRankObject rank : faction.getFactionRanks()){
            if (rank.getRankName().equals(rankName)){
                return rank;
            }
        }
        return null;
    }
}
