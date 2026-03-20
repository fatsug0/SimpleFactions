package com.gus.simpleFactions.FactionHandlers.Objects;

import java.util.ArrayList;

public class FactionRankObject {

    public FactionRankObject(String rankName) {
        this.rankName = rankName;
    }

    private final String rankName;
    private ArrayList<String> permissions = new ArrayList<>();

    public String getRankName() {
        return rankName;
    }

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
