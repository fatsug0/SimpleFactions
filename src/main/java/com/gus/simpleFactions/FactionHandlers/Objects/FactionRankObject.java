package com.gus.simpleFactions.FactionHandlers.Objects;

import com.gus.simpleFactions.Json.FactionRankObjectWrapper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.UUID;

public class FactionRankObject {

    public FactionRankObject(@Nullable String rankName, @Nullable FactionRankObjectWrapper wrappedFactionRankObject) {
        if (wrappedFactionRankObject == null) {
            this.rankName = normalizeRankName(rankName);
        } else {
            this.rankName = normalizeRankName(wrappedFactionRankObject.getRankName());
            wrappedFactionRankObject.getRankMembers().forEach(playerUUID -> {
                try {
                    addRankMember(UUID.fromString(playerUUID));
                } catch (IllegalArgumentException ignored) {
                }
            });
            wrappedFactionRankObject.getPermissions().forEach(this::addPermission);
        }
    }

    private final String rankName;
    public String getRankName() {
        return rankName;
    }

    private String normalizeRankName(String rankName) {
        if (rankName == null) {
            return null;
        }

        return rankName.trim().toUpperCase();
    }


    private final ArrayList<UUID> rankMembers = new ArrayList<>();
    public ArrayList<UUID> getRankMembers() {
        return rankMembers;
    }
    public void addRankMember(UUID playerUUID){
        if (playerUUID != null && !rankMembers.contains(playerUUID)) {
            rankMembers.add(playerUUID);
        }
    }
    public void removeRankMember(UUID playerUUID){
        rankMembers.remove(playerUUID);
    }
    public boolean hasRankMember(UUID playerUUID){
        return rankMembers.contains(playerUUID);
    }


    private final ArrayList<String> permissions = new ArrayList<>();
    public ArrayList<String> getPermissions() {
        return permissions;
    }
    public void addPermission(String permission){
        if (permission != null && !permission.isBlank() && !permissions.contains(permission)) {
            permissions.add(permission);
        }
    }
    public void removePermission(String permission){
        permissions.remove(permission);
    }
    public boolean hasPermission(String permission){
        return permissions.contains(permission);
    }
}
