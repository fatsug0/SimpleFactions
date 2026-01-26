package com.gus.simpleFactions;

import net.md_5.bungee.api.chat.objects.PlayerObject;

import java.util.ArrayList;

public class FactionRank {

    public FactionRank(String rankName) {
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
