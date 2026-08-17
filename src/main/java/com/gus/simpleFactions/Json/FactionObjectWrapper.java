package com.gus.simpleFactions.Json;

import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

public class FactionObjectWrapper {

    public FactionObjectWrapper(FactionObject factionObject) {
        this.factionOwner = factionObject.getOwner().toString();
        this.factionName = factionObject.getFactionName();
        this.factionHome = factionObject.getFactionHome() == null ? null : new SavedLocation(
                factionObject.getFactionHome().getWorld() == null ? "world" : factionObject.getFactionHome().getWorld().getName(),
                factionObject.getFactionHome().getX(),
                factionObject.getFactionHome().getY(),
                factionObject.getFactionHome().getZ(),
                factionObject.getFactionHome().getYaw(),
                factionObject.getFactionHome().getPitch()
        );
        factionObject.getFactionMembers().forEach(playerUUID -> this.factionMembers.add(playerUUID.toString()));
        factionObject.getHardClaimedChunks().forEach(chunk -> this.hardClaimedChunks.add(new SavedChunk(chunk.getX(), chunk.getZ())));
        factionObject.getWeakClaimedChunks().forEach(chunk -> this.weakClaimedChunks.add(new SavedChunk(chunk.getX(), chunk.getZ())));
        factionObject.getFactionRanks().forEach(rank -> this.factionRanks.add(new FactionRankObjectWrapper(rank)));
        factionObject.getSavedFactionRanks().forEach((uuid, rank) -> this.savedFactionRanks.put(uuid.toString(), rank));
        this.power = factionObject.getPower();
        this.teamPrefix = factionObject.getTeamPrefix();
        this.factionColors = new ArrayList<>(factionObject.getFactionColors());
        for (int i = 0; i < factionObject.getFactionInv().getSize(); i++) {
            this.factionInv.put(i, itemToBase64(factionObject.getFactionInv().getItem(i)));
        }
    }

    private String factionOwner;
    public String getOwner() {
        return this.factionOwner;
    }

    private String factionName;
    public String getName() {
        return this.factionName;
    }

    private SavedLocation factionHome;
    public SavedLocation getHome() {
        return this.factionHome;
    }

    private ArrayList<String> factionMembers = new ArrayList<>();
    public ArrayList<String> getMembers() {
        return this.factionMembers == null ? new ArrayList<>() : this.factionMembers;
    }

    private ArrayList<SavedChunk> hardClaimedChunks = new ArrayList<>();
    public ArrayList<SavedChunk> getHardClaimedChunks() {
        return this.hardClaimedChunks == null ? new ArrayList<>() : this.hardClaimedChunks;
    }

    private ArrayList<SavedChunk> weakClaimedChunks = new ArrayList<>();
    public ArrayList<SavedChunk> getWeakClaimedChunks() {
        return this.weakClaimedChunks == null ? new ArrayList<>() : this.weakClaimedChunks;
    }

    private ArrayList<FactionRankObjectWrapper> factionRanks = new ArrayList<>();
    public ArrayList<FactionRankObjectWrapper> getRanks() {
        return this.factionRanks == null ? new ArrayList<>() : this.factionRanks;
    }

    private HashMap<String, String> savedFactionRanks = new HashMap<>();
    public HashMap<String, String> getSavedFactionRanks() {
        return this.savedFactionRanks == null ? new HashMap<>() : this.savedFactionRanks;
    }

    private Integer power;
    public Integer getPower() {
        return this.power == null ? 0 : this.power;
    }

    private String teamPrefix;
    public String getTeamPrefix() {
        return this.teamPrefix;
    }

    private ArrayList<String> factionColors = new ArrayList<>();
    public ArrayList<String> getColors() {
        return this.factionColors == null ? new ArrayList<>() : this.factionColors;
    }

    private HashMap<Integer, Object> factionInv = new HashMap<>();
    public HashMap<Integer, ItemStack> getInv() {
        HashMap<Integer, ItemStack> items = new HashMap<>();
        if (this.factionInv == null) return items;
        this.factionInv.forEach((slot, itemData) -> items.put(slot, itemData instanceof String encodedItem ? itemFromBase64(encodedItem) : null));
        return items;
    }

    private String itemToBase64(ItemStack item) {
        if (item == null) return null;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    private ItemStack itemFromBase64(String itemData) {
        if (itemData == null || itemData.isBlank()) return null;
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(itemData));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            Object item = dataInput.readObject();
            dataInput.close();
            return item instanceof ItemStack itemStack ? itemStack : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static class SavedLocation {
        public String world;
        public double x;
        public double y;
        public double z;
        public float yaw;
        public float pitch;

        public SavedLocation(String world, double x, double y, double z, float yaw, float pitch) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }

    public static class SavedChunk {
        public Double x;
        public Double y;
        public Double z;

        public SavedChunk(int x, int z) {
            this.x = (double) x;
            this.z = (double) z;
        }

        public int getX() {
            return x == null ? 0 : x.intValue();
        }

        public int getZ() {
            if (z != null) return z.intValue();
            return y == null ? 0 : y.intValue();
        }
    }
}
