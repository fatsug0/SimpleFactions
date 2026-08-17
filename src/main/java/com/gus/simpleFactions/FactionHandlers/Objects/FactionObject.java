package com.gus.simpleFactions.FactionHandlers.Objects;

import com.gus.simpleFactions.Json.FactionObjectWrapper;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import org.bukkit.*;
import org.bukkit.inventory.Inventory;


import javax.annotation.Nullable;
import java.util.*;

public class FactionObject {

    public FactionObject(@Nullable UUID player, @Nullable String factionName, @Nullable Integer factionStartPower, @Nullable FactionObjectWrapper wrappedFactionObject) {
        if (wrappedFactionObject == null) {
            this.factionName = factionName;
            this.factionOwner = player;
            this.factionMembers.add(player);
            this.power = factionStartPower;

        } else {
            this.factionOwner = UUID.fromString(wrappedFactionObject.getOwner());
            this.factionName = wrappedFactionObject.getName();
            if (wrappedFactionObject.getHome() != null) {
                FactionObjectWrapper.SavedLocation home = wrappedFactionObject.getHome();
                World world = Bukkit.getWorld(home.world == null ? "world" : home.world);
                if (world != null) {
                    this.factionHome = new Location(world, home.x, home.y, home.z, home.yaw, home.pitch);
                }
            }
            wrappedFactionObject.getMembers().forEach(member -> {
                try {
                    this.factionMembers.add(UUID.fromString(member));
                } catch (IllegalArgumentException ignored) {
                }
            });
            World world = Bukkit.getWorld("world");
            if (world != null) {
                wrappedFactionObject.getHardClaimedChunks().forEach(chunk -> this.hardClaimedChunks.add(world.getChunkAt(chunk.getX(), chunk.getZ())));
                wrappedFactionObject.getWeakClaimedChunks().forEach(chunk -> this.weakClaimedChunks.add(world.getChunkAt(chunk.getX(), chunk.getZ())));
            }
            wrappedFactionObject.getSavedFactionRanks().forEach((uuid, rank) -> {
                try {
                    this.savedFactionRanks.put(UUID.fromString(uuid), rank);
                } catch (IllegalArgumentException ignored) {
                }
            });
            wrappedFactionObject.getRanks().forEach(factionRankObjectWrapper -> this.factionRanks.add(new FactionRankObject(factionRankObjectWrapper.getRankName(), factionRankObjectWrapper)));
            this.power = wrappedFactionObject.getPower();
            this.teamPrefix = wrappedFactionObject.getTeamPrefix();
            this.factionColors = new ArrayList<>(wrappedFactionObject.getColors());
            wrappedFactionObject.getInv().forEach((slot, item) -> this.factionInv.setItem(slot, item));
        }

        this.factionMarkerSet = new MarkerSet("factionMarkerSet" + this.factionName);
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


    private HashMap<UUID, String> savedFactionRanks = new HashMap<>();
    public HashMap<UUID, String> getSavedFactionRanks() {
        return this.savedFactionRanks;
    }
    public void addSavedFactionRank(UUID player, String rank) {
        this.savedFactionRanks.put(player, rank);
    }
    public void removeSavedFactionRank(UUID player) {
        this.savedFactionRanks.remove(player);
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


    // The color(s) currently used for this faction's tab-list prefix (hex strings, e.g.
    // "#RRGGBB"; more than one means a gradient). Kept in sync by FactionFormatterService
    // whenever the prefix is (re)colored, and used to color the faction's BlueMap claim markers.
    private ArrayList<String> factionColors = new ArrayList<>();
    public ArrayList<String> getFactionColors() {
        return this.factionColors;
    }
    public void setFactionColors(ArrayList<String> factionColors) {
        this.factionColors = factionColors == null ? new ArrayList<>() : factionColors;
    }


    private final MarkerSet factionMarkerSet;
    public MarkerSet getFactionMarkerSet() {
        return this.factionMarkerSet;
    }


    private final Inventory factionInv = Bukkit.createInventory(null, 9*6, "Faction Storage");
    public Inventory getFactionInv() {
        return this.factionInv;
    }
}
