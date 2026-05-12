package com.gus.simpleFactions.FactionHandlers.FactionObjectServices;

import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionRankObject;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class FactionRankService {

    private final SimpleFactions plugin;
    public FactionRankService(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    private HashMap<UUID, PermissionAttachment> perms = new HashMap<>();
    public HashMap<UUID, PermissionAttachment> getPerms() {
        return perms;
    }
    public Map<String, ArrayList<Map<String, Boolean>>> permWrapper() {
        Map<String, ArrayList<Map<String, Boolean>>> returnMap = new HashMap<>();
        for (Map.Entry<UUID, PermissionAttachment> entry : perms.entrySet()) {
            if (!returnMap.containsKey(entry.getKey().toString())) {
                returnMap.put(entry.getKey().toString(), new ArrayList<>(){{add(entry.getValue().getPermissions());}});
            } else {
                returnMap.get(entry.getKey().toString()).add(entry.getValue().getPermissions());
            }
        }
        return returnMap;
    }
    public void unWrapPerms(Map<String, ArrayList<Map<String, Boolean>>> wrappedPerms) {
        perms.clear();
        if (wrappedPerms == null) return;
        for (Map.Entry<String, ArrayList<Map<String, Boolean>>> entry : wrappedPerms.entrySet()) {
            for (Map<String, Boolean> perm : entry.getValue()) {
                try {
                    for (Map.Entry<String, Boolean> permission : perm.entrySet()) {
                        if (Boolean.TRUE.equals(permission.getValue())) {
                            AddPermToPlayer(UUID.fromString(entry.getKey()), permission.getKey());
                        }
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Skipped invalid saved permission UUID: " + entry.getKey());
                }
            }
        }
    }

    public void CreateFactionRank(FactionObject faction, String rankName, Player player){
        for (FactionRankObject rank : faction.getFactionRanks()){
            if (rank.getRankName().equals(rankName)){
                if (player == null) {
                    return;
                }
                player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The rank you are trying to create already exists !");
                return;
            }
        }
        faction.addFactionRank(new FactionRankObject(rankName, null));
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
        if (player == null) return;
        FactionRankObject rank = getRank(faction, rankName);
        if (rank == null) {
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
        if (player == null) return;
        for (FactionRankObject rank : faction.getFactionRanks()){
            if (rank.getRankMembers().contains(player.getUniqueId())){
                return;
            }
        }

        FactionRankObject rank = getRank(faction, rankName);
        if (rank == null) {
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
        if (player == null || perm == null) return;
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
        perms.put(playerUUID, attachment);
    }

    public void RemovePermToPlayer(UUID playerUUID, String perm){
        Player player = plugin.factionManager.factionHelperService.checkPlayer(playerUUID);
        if (player == null || perm == null) return;
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
        perms.put(playerUUID, attachment);
    }

    public FactionRankObject getRank(FactionObject faction, String rankName){
        for (FactionRankObject rank : faction.getFactionRanks()){
            if (rank.getRankName().equals(rankName)){
                return rank;
            }
        }
        return null;
    }

    public void removeAttachment(UUID playerUUID){
        if (perms.containsKey(playerUUID)) {
            perms.remove(playerUUID);
        }
    }
}
