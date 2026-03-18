package com.gus.simpleFactions.FactionHandlers.FactionObjectServices;

import com.gus.simpleFactions.FactionHandlers.FactionRankObject;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class FactionRankManager {

    private final SimpleFactions plugin;
    public FactionRankManager(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    public void CreateFactionRank(String rankName, Player player){
        for (FactionRankObject rank : factionRanks.values()){
            if (rank.getRankName().equals(rankName)){
                player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The rank you are trying to create already exists !");
                return;
            }
        }
        existingFactionRanks.add(new FactionRankObject(rankName));

        Team team = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard().getTeam(toTeamName(this.getFactionName()));
    }
    public void DeleteFactionRank(String rankName, Player player){
        if (existingFactionRanks.isEmpty()) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "There is no rank to delete !");
            return;
        }

        if (!existingFactionRanks.contains(new FactionRankObject(rankName))) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The rank you are trying to delete does not exist !");
            return;
        }

        for (UUID playerUUID : factionRanks.keySet()){
            if (factionRanks.get(playerUUID).getRankName().equals(rankName)){
                factionRanks.remove(playerUUID);

                // Remove his perms
                if (checkPlayer(playerUUID) == null) return;
                plugin.permissionManager.RemovePerm(checkPlayer(playerUUID), factionRanks.get(playerUUID).getPermissions());
            }
        }

        existingFactionRanks.removeIf(rank -> rank.getRankName().equals(rankName));
    }

    // Add & Remove Player to Rank
    public void RemovePlayerFromRank(Player player, String rankName){
        if (!factionRanks.containsKey(player.getUniqueId())){
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Player has no a rank ! Cant remove him from it !");
            return;
        }

        for (FactionRankObject rank : existingFactionRanks){
            if (rank.getRankName().equals(rankName)){
                factionRanks.remove(player.getUniqueId(), rank);
                plugin.permissionManager.RemovePerm(player, rank.getPermissions());
            }
        }
        System.out.println(ChatColor.GREEN + ChatColor.ITALIC.toString() + "Added player " + player.getName() + " to rank " + rankName);
        System.out.println(player.getName() + " now has the following permissions: " + player.getEffectivePermissions().toString());
    }
    public void AddPlayerToRank(Player player, String rankName){
        if (factionRanks.containsKey(player.getUniqueId())){
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Player has already a rank ! Remove it first !");
            return;
        }
        for (FactionRankObject rank : existingFactionRanks){
            if (rank.getRankName().equals(rankName)){
                factionRanks.put(player.getUniqueId(), rank);
                plugin.permissionManager.AddPerm(player, rank.getPermissions());
            }
        }
        System.out.println(ChatColor.GREEN + ChatColor.ITALIC.toString() + "Added player " + player.getName() + " to rank " + rankName);
        System.out.println(player.getName() + " now has the following permissions: " + player.getEffectivePermissions().toString());
    }

    // Add & Remove permission from rank
    public void AddPermissionRank(String rankName, ArrayList<String> permission) {
        for (FactionRankObject rank : factionRanks.values()) {
            if (rank.getRankName().equals(rankName)) {
                for (String perm : permission) {
                    rank.addPermission(perm);
                }
                System.out.println("Added the following permissions to the rank " + rankName + ": " + permission.toString());
                return;
            }
        }
    }
    public void RemovePermissionRank(String rankName, ArrayList<String> permission) {
        for (FactionRankObject rank : factionRanks.values()) {
            if (rank.getRankName().equals(rankName)) {
                for (String perm : permission) {
                    rank.removePermission(perm);
                }
                System.out.println("Removed the following permissions from the rank " + rankName + ": " + permission.toString());
                return;
            }
        }
    }
}
