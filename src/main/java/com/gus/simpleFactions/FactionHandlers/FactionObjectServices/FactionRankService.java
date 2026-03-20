package com.gus.simpleFactions.FactionHandlers.FactionObjectServices;

import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionRankObject;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class FactionRankService {

    private final SimpleFactions plugin;
    public FactionRankService(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    public void CreateFactionRank(FactionObject faction, String rankName, Player player){
        for (FactionRankObject rank : faction.getFactionRanks().values()){
            if (rank.getRankName().equals(rankName)){
                player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The rank you are trying to create already exists !");
                return;
            }
        }
        faction.getExistingFactionRankObjects().add(new FactionRankObject(rankName));

        Team team = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard().getTeam(plugin.factionManager.factionFormatterService.toTeamName(faction.getFactionName()));
    }
    public void DeleteFactionRank(FactionObject faction, String rankName, Player player){
        if (faction.getExistingFactionRankObjects().isEmpty()) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "There is no rank to delete !");
            return;
        }

        if (!faction.getExistingFactionRankObjects().contains(new FactionRankObject(rankName))) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The rank you are trying to delete does not exist !");
            return;
        }

        for (UUID playerUUID : faction.getFactionRanks().keySet()){
            if (faction.getFactionRanks().get(playerUUID).getRankName().equals(rankName)){
                faction.removeFactionRank(playerUUID);

                // Remove his perms
                if (plugin.factionManager.factionHelperService.checkPlayer(playerUUID) == null) return;
                plugin.permissionManager.RemovePerm(plugin.factionManager.factionHelperService.checkPlayer(playerUUID), faction.getFactionRanks().get(playerUUID).getPermissions());
            }
        }

        faction.getExistingFactionRankObjects().removeIf(rank -> rank.getRankName().equals(rankName));
    }

    // Add & Remove Player to Rank
    public void RemovePlayerFromRank(FactionObject faction, Player player, String rankName){
        if (!faction.getFactionRanks().containsKey(player.getUniqueId())){
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Player has no a rank ! Cant remove him from it !");
            return;
        }

        for (FactionRankObject rank : faction.getExistingFactionRankObjects()){
            if (rank.getRankName().equals(rankName)){
                faction.getFactionRanks().remove(player.getUniqueId(), rank);
                plugin.permissionManager.RemovePerm(player, rank.getPermissions());
            }
        }
        System.out.println(ChatColor.GREEN + ChatColor.ITALIC.toString() + "Added player " + player.getName() + " to rank " + rankName);
        System.out.println(player.getName() + " now has the following permissions: " + player.getEffectivePermissions().toString());
    }
    public void AddPlayerToRank(FactionObject faction, Player player, String rankName){
        if (faction.getFactionRanks().containsKey(player.getUniqueId())){
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Player has already a rank ! Remove it first !");
            return;
        }
        for (FactionRankObject rank : faction.getExistingFactionRankObjects()){
            if (rank.getRankName().equals(rankName)){
                faction.getFactionRanks().put(player.getUniqueId(), rank);
                plugin.permissionManager.AddPerm(player, rank.getPermissions());
            }
        }
        System.out.println(ChatColor.GREEN + ChatColor.ITALIC.toString() + "Added player " + player.getName() + " to rank " + rankName);
        System.out.println(player.getName() + " now has the following permissions: " + player.getEffectivePermissions().toString());
    }

    // Add & Remove permission from rank
    public void AddPermissionRank(FactionObject faction, String rankName, ArrayList<String> permission) {
        for (FactionRankObject rank : faction.getFactionRanks().values()) {
            if (rank.getRankName().equals(rankName)) {
                for (String perm : permission) {
                    rank.addPermission(perm);
                }
                System.out.println("Added the following permissions to the rank " + rankName + ": " + permission.toString());
                return;
            }
        }
    }
    public void RemovePermissionRank(FactionObject faction, String rankName, ArrayList<String> permission) {
        for (FactionRankObject rank : faction.getFactionRanks().values()) {
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
