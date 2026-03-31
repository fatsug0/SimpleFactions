package com.gus.simpleFactions.FactionHandlers.Objects;

import java.util.ArrayList;
import java.util.UUID;

public class FactionRankObject {

    public FactionRankObject(String rankName) {
        this.rankName = rankName;
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
