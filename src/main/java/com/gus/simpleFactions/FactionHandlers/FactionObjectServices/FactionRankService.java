package com.gus.simpleFactions.FactionHandlers.FactionObjectServices;

import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionRankObject;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.*;

public class FactionRankService {

    private static final Set<String> PROTECTED_RANKS = Set.of("OWNER", "MEMBER");

    private final SimpleFactions plugin;
    public FactionRankService(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    private final HashMap<UUID, PermissionAttachment> perms = new HashMap<>();
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
        String normalizedRankName = normalizeRankName(rankName);

        if (faction == null || normalizedRankName == null) {
            if (player != null) {
                player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Invalid rank name!");
            }
            return;
        }

        if (getRank(faction, normalizedRankName) != null) {
            if (player != null) {
                player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The rank you are trying to create already exists!");
            }
            return;
        }

        faction.addFactionRank(new FactionRankObject(normalizedRankName, null));

        if (player != null) {
            player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "Rank created: " + normalizedRankName);
        }
    }

    public void DeleteFactionRank(FactionObject faction, String rankName, Player player){
        String normalizedRankName = normalizeRankName(rankName);

        if (faction == null || normalizedRankName == null) {
            if (player != null) {
                player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Invalid rank name!");
            }
            return;
        }

        if (PROTECTED_RANKS.contains(normalizedRankName)) {
            if (player != null) {
                player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Default ranks cannot be deleted!");
            }
            return;
        }

        if (faction.getFactionRanks().isEmpty()) {
            if (player != null) {
                player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "There is no rank to delete!");
            }
            return;
        }

        FactionRankObject rank = getRank(faction, normalizedRankName);
        if (rank == null) {
            if (player != null) {
                player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The rank you are trying to delete does not exist!");
            }
            return;
        }

        for (UUID playerUUID : new ArrayList<>(rank.getRankMembers())) {
            removePlayerFromRank(faction, playerUUID, normalizedRankName, false);
        }

        faction.removeFactionRank(rank);

        if (player != null) {
            player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "Rank deleted: " + normalizedRankName);
        }
    }

    // Add & Remove Player to Rank
    public void RemovePlayerFromRank(FactionObject faction, Player player, String rankName){
        if (player == null) return;

        removePlayerFromRank(faction, player.getUniqueId(), rankName, true);
    }

    private void removePlayerFromRank(FactionObject faction, UUID playerUUID, String rankName, boolean notifyPlayer) {
        if (faction == null || playerUUID == null) return;

        String normalizedRankName = normalizeRankName(rankName);
        FactionRankObject rank = getRank(faction, normalizedRankName);
        Player player = plugin.factionManager.factionHelperService.checkPlayer(playerUUID);

        if (rank == null) {
            return;
        }

        if (!rank.getRankMembers().contains(playerUUID)){
            if (notifyPlayer && player != null) {
                player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Player is not in this rank!");
            }
            return;
        }

        for (String perm : rank.getPermissions()){
            RemovePermToPlayer(playerUUID, perm);
        }

        rank.removeRankMember(playerUUID);
        faction.removeSavedFactionRank(playerUUID);
    }

    public void AddPlayerToRank(FactionObject faction, Player player, String rankName){
        if (player == null) return;

        AddPlayerToRank(faction, player.getUniqueId(), rankName);
    }

    public void AddPlayerToRank(FactionObject faction, UUID playerUUID, String rankName){
        if (faction == null || playerUUID == null) return;

        String normalizedRankName = normalizeRankName(rankName);
        FactionRankObject rank = getRank(faction, normalizedRankName);

        if (rank == null) {
            Player player = plugin.factionManager.factionHelperService.checkPlayer(playerUUID);
            if (player != null) {
                player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "That rank does not exist!");
            }
            return;
        }

        if (!faction.getFactionMembers().contains(playerUUID)) {
            Player player = plugin.factionManager.factionHelperService.checkPlayer(playerUUID);
            if (player != null) {
                player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "That player is not in this faction!");
            }
            return;
        }

        for (FactionRankObject existingRank : faction.getFactionRanks()){
            if (existingRank.getRankMembers().contains(playerUUID)){
                if (existingRank.getRankName().equals(normalizedRankName)) {
                    return;
                }

                for (String perm : existingRank.getPermissions()) {
                    RemovePermToPlayer(playerUUID, perm);
                }

                existingRank.removeRankMember(playerUUID);
            }
        }

        rank.addRankMember(playerUUID);
        faction.addSavedFactionRank(playerUUID, normalizedRankName);

        for (String perm : rank.getPermissions()){
            AddPermToPlayer(playerUUID, perm);
        }
    }

    public void ensureDefaultRanks(FactionObject faction) {
        if (faction == null) {
            return;
        }

        if (getRank(faction, "OWNER") == null) {
            faction.addFactionRank(new FactionRankObject("OWNER", null));
        }

        if (getRank(faction, "MEMBER") == null) {
            faction.addFactionRank(new FactionRankObject("MEMBER", null));
        }
    }

    public void ensureOwnerHasOwnerRank(FactionObject faction) {
        if (faction == null || faction.getOwner() == null) {
            return;
        }

        ensureDefaultRanks(faction);

        UUID ownerUUID = faction.getOwner();
        FactionRankObject ownerRank = getRank(faction, "OWNER");

        if (!faction.getFactionMembers().contains(ownerUUID)) {
            faction.addFactionMember(ownerUUID);
        }

        for (FactionRankObject rank : faction.getFactionRanks()) {
            if (!rank.getRankName().equalsIgnoreCase("OWNER")) {
                rank.removeRankMember(ownerUUID);
            }
        }

        ownerRank.addRankMember(ownerUUID);
        faction.addSavedFactionRank(ownerUUID, "OWNER");
    }

    public void ensureMembersHaveValidRanks(FactionObject faction) {
        if (faction == null) {
            return;
        }

        ensureDefaultRanks(faction);
        ensureOwnerHasOwnerRank(faction);

        FactionRankObject memberRank = getRank(faction, "MEMBER");

        for (UUID memberUUID : faction.getFactionMembers()) {
            if (memberUUID == null || memberUUID.equals(faction.getOwner())) {
                continue;
            }

            FactionRankObject currentRank = getPlayerRank(faction, memberUUID);

            if (currentRank == null) {
                memberRank.addRankMember(memberUUID);
                faction.addSavedFactionRank(memberUUID, "MEMBER");
            } else {
                faction.addSavedFactionRank(memberUUID, currentRank.getRankName());
            }
        }
    }

    public void reapplyRankPermissions(FactionObject faction, UUID playerUUID) {
        if (faction == null || playerUUID == null) {
            return;
        }

        ensureMembersHaveValidRanks(faction);

        FactionRankObject rank = getPlayerRank(faction, playerUUID);

        if (rank == null) {
            String savedRank = faction.getSavedFactionRanks().get(playerUUID);
            rank = savedRank == null ? null : getRank(faction, savedRank);
        }

        if (rank == null) {
            return;
        }

        faction.addSavedFactionRank(playerUUID, rank.getRankName());

        for (String permission : rank.getPermissions()) {
            AddPermToPlayer(playerUUID, permission);
        }
    }

    public FactionRankObject getPlayerRank(FactionObject faction, UUID playerUUID) {
        if (faction == null || playerUUID == null) {
            return null;
        }

        for (FactionRankObject rank : faction.getFactionRanks()) {
            if (rank.hasRankMember(playerUUID)) {
                return rank;
            }
        }

        return null;
    }

    // Add & Remove permission from rank
    public void AddPermissionRank(FactionObject faction, String rankName, String permission) {
        String normalizedRankName = normalizeRankName(rankName);
        FactionRankObject rank = getRank(faction, normalizedRankName);
        if (rank == null || permission == null || permission.isBlank()) {
            return;
        }

        if (rank.hasPermission(permission)) {
            return;
        }

        rank.addPermission(permission);

        // Add perm for every player in the rank
        for (UUID playerUUID : rank.getRankMembers()) {
            AddPermToPlayer(playerUUID, permission);
        }
    }
    public void RemovePermissionRank(FactionObject faction, String rankName, String permission) {
        String normalizedRankName = normalizeRankName(rankName);
        FactionRankObject rank = getRank(faction, normalizedRankName);
        if (rank == null || permission == null || permission.isBlank()) {
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
        if (faction == null || rankName == null) {
            return null;
        }

        String normalizedRankName = normalizeRankName(rankName);

        for (FactionRankObject rank : faction.getFactionRanks()){
            if (rank.getRankName().equalsIgnoreCase(normalizedRankName)){
                return rank;
            }
        }
        return null;
    }

    public void removeAttachment(UUID playerUUID){
        PermissionAttachment attachment = perms.remove(playerUUID);
        Player player = plugin.factionManager.factionHelperService.checkPlayer(playerUUID);

        if (player != null && attachment != null) {
            player.removeAttachment(attachment);
        }
    }

    private String normalizeRankName(String rankName) {
        if (rankName == null) {
            return null;
        }

        String normalizedRankName = rankName.trim().toUpperCase();

        if (normalizedRankName.isBlank()) {
            return null;
        }

        return normalizedRankName;
    }
}
