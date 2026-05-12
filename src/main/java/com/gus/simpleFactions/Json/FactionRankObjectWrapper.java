package com.gus.simpleFactions.Json;

import com.gus.simpleFactions.FactionHandlers.Objects.FactionRankObject;

import java.util.ArrayList;

public class FactionRankObjectWrapper {

    public FactionRankObjectWrapper(FactionRankObject factionRankObject){
        this.rankName = factionRankObject.getRankName();
        factionRankObject.getRankMembers().forEach(playerUUID -> this.rankMembers.add(playerUUID.toString()));
        this.permissions.addAll(factionRankObject.getPermissions());
    }

    private String rankName;
    public String getRankName() {
        return rankName;
    }

    private ArrayList<String> rankMembers = new ArrayList<>();
    public ArrayList<String> getRankMembers() {
        return rankMembers == null ? new ArrayList<>() : rankMembers;
    }

    private ArrayList<String> permissions = new ArrayList<>();
    public ArrayList<String> getPermissions() {
        return permissions == null ? new ArrayList<>() : permissions;
    }
}
