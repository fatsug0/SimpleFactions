package com.gus.simpleFactions.FactionHandlers.Objects;

import de.bluecolored.bluemap.api.markers.MarkerSet;
import org.bukkit.*;


import java.util.*;

public class FactionObject {

    public FactionObject(UUID player, String factionName, Integer factionStartPower) {
        this.factionName = factionName;
        this.factionOwner = player;
        this.factionMembers.add(player);
        this.power = factionStartPower;

        this.factionMarkerSet = new MarkerSet("factionMarkerSet" + factionName);
    }


    private final UUID factionOwner;
    public UUID getOwner() {
        return this.factionOwner;
    }


    private final String factionName;
    public String getFactionName() {
        return this.factionName;
    }


    private Location factionHome;
    public Location getFactionHome() {
        return this.factionHome;
    }
    public void setFactionHome(Location factionHome) {
        this.factionHome = factionHome;
    }


    private ArrayList<UUID> factionMembers = new ArrayList<>();
    public ArrayList<UUID> getFactionMembers() {
        return this.factionMembers;
    }
    public void addFactionMember(UUID player) {
        this.factionMembers.add(player);
    }
    public void removeFactionMember(UUID player) {
        this.factionMembers.remove(player);
    }


    private ArrayList<Chunk> hardClaimedChunks = new ArrayList<>();
    public ArrayList<Chunk> getHardClaimedChunks() {
        return this.hardClaimedChunks;
    }
    public void addHardClaimedChunks(Chunk chunk) {
        this.hardClaimedChunks.add(chunk);
    }
    public void removeHardClaimedChunks(Chunk chunk) {
        this.hardClaimedChunks.remove(chunk);
    }


    private ArrayList<Chunk> weakClaimedChunks = new ArrayList<>();
    public ArrayList<Chunk> getWeakClaimedChunks() {
        return this.weakClaimedChunks;
    }
    public void addWeakClaimedChunks(Chunk chunk) {
        this.weakClaimedChunks.add(chunk);
    }
    public void removeWeakClaimedChunks(Chunk chunk) {
        this.weakClaimedChunks.remove(chunk);
    }


    private ArrayList<FactionRankObject> factionRanks = new ArrayList<>();
    public ArrayList<FactionRankObject> getFactionRanks() {
        return this.factionRanks;
    }
    public void addFactionRank(FactionRankObject rank) {
        this.factionRanks.add(rank);
    }
    public void removeFactionRank(FactionRankObject rank) {
        this.factionRanks.remove(rank);
    }


    private int power;
    public int getPower() {
        return this.power;
    }
    public void setPower(int power) {
        this.power = power;
    }


    private String teamPrefix;
    public String getTeamPrefix() {
        return this.teamPrefix;
    }
    public void setTeamPrefix(String teamPrefix) {
        this.teamPrefix = teamPrefix;
    }


    private final MarkerSet factionMarkerSet;
    public MarkerSet getFactionMarkerSet() {
        return this.factionMarkerSet;
    }


    private final FactionInvetoryObject factionInv = new FactionInvetoryObject("Faction Storage", 9*6);
    public FactionInvetoryObject getFactionInv() {
        return this.factionInv;
    }
}