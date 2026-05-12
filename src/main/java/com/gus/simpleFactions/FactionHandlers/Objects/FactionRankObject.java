package com.gus.simpleFactions.FactionHandlers.Objects;

import com.gus.simpleFactions.Json.FactionRankObjectWrapper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.UUID;

public class FactionRankObject {

    public FactionRankObject(@Nullable String rankName, @Nullable FactionRankObjectWrapper wrappedFactionRankObject) {
        if (wrappedFactionRankObject == null) {
            this.rankName = rankName;
        } else {
            this.rankName = wrappedFactionRankObject.getRankName();
            wrappedFactionRankObject.getRankMembers().forEach(playerUUID -> {
                try {
                    this.rankMembers.add(UUID.fromString(playerUUID));
                } catch (IllegalArgumentException ignored) {
                }
            });
            this.permissions.addAll(wrappedFactionRankObject.getPermissions());
        }
    }

    private final String rankName;
    public String getRankName() {
        return rankName;
    }


    private ArrayList<UUID> rankMembers = new ArrayList<>();
    public ArrayList<UUID> getRankMembers() {
        return rankMembers;
    }
    public void addRankMember(UUID playerUUID){
        rankMembers.add(playerUUID);
    }
    public void removeRankMember(UUID playerUUID){
        rankMembers.remove(playerUUID);
    }
    public boolean hasRankMember(UUID playerUUID){
        return rankMembers.contains(playerUUID);
    }


    private ArrayList<String> permissions = new ArrayList<>();
    public ArrayList<String> getPermissions() {
        return permissions;
    }
    public void addPermission(String permission){
        permissions.add(permission);
    }
    public void removePermission(String permission){
        permissions.remove(permission);
    }
    public boolean hasPermission(String permission){
        return permissions.contains(permission);
    }
}
