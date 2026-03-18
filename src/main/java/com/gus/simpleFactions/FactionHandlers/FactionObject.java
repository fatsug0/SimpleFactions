package com.gus.simpleFactions.FactionHandlers;

import com.gus.simpleFactions.SimpleFactions;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;


import java.util.*;

public class FactionObject {

    public FactionObject(SimpleFactions plugin, UUID player, String factionName, Integer factionStartPower, double maxRaidableChunksCoefficient) {
        this.factionName = factionName;
        this.factionOwner = player;
        this.factionMembers.add(player);
        this.power = factionStartPower;
        this.maxWeakChunks = Math.round(factionStartPower * maxRaidableChunksCoefficient);

        // Classes instances
        this.plugin = plugin;
        this.factionManager = plugin.factionManager;

        // BlueMap stuff
        this.markerSet = MarkerSet.builder().label("FactionMarkerSet " + factionName).defaultHidden(false).build();
        this.factionClaimColor = "7f7f7f";
    }

    private final SimpleFactions plugin;
    private final FactionManager factionManager;

    private final UUID factionOwner;
    public UUID getOwner() {
        return this.factionOwner;
    }

    private Location factionHome;
    public Location getFactionHome() {
        return this.factionHome;
    }

    private final String factionName;
    public String getFactionName() {
        return this.factionName;
    }

    private ArrayList<UUID> factionMembers = new ArrayList<>();
    public ArrayList<UUID> getFactionMembers() {
        return this.factionMembers;
    }

    private ArrayList<Chunk> claimedChunks = new ArrayList<>();
    public ArrayList<Chunk> getClaimedChunks() {
        return this.claimedChunks;
    }

    private ArrayList<Chunk> weakChunks = new ArrayList<>();
    public ArrayList<Chunk> getWeakChunks() {
        return this.weakChunks;
    }

    private ArrayList<FactionRankObject> existingFactionRankObjects = new ArrayList<>();
    public Map<UUID, FactionRankObject> factionRanks = new HashMap<>();

    private int power;
    public int getPower() {
        return this.power;
    }
    public void setPower(int power) {
        this.power = power;
    }

    private final double maxWeakChunks;
    public double getMaxWeakChunks() {
        return this.maxWeakChunks;
    }

    // For now, using a boolean, but while use a config file for this (when setup)
    // Is used to display on bluemap the claimed chunks of the faction
    private final boolean USE_BLUEMAP_ADDON = true;
    public final MarkerSet markerSet;
    private final String factionClaimColor;

    public String teamPrefix;

}