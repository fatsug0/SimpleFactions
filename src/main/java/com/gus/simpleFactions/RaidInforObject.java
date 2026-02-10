package com.gus.simpleFactions;

import com.gus.simpleFactions.Enums.RaidState;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Calendar;

public class RaidInforObject {

    private RaidState raidState;
    private final Calendar raidDate;
    private final FactionObject attackingFaction;
    private final ArrayList<Chunk> attackedChunks;
    private ItemStack raidCore;

    public RaidInforObject(RaidState raidState, Calendar raidDate, FactionObject attackingFaction, ArrayList<Chunk> attackedChunks) {
        this.raidState = raidState;
        this.raidDate = raidDate;
        this.attackingFaction = attackingFaction;
        this.attackedChunks = attackedChunks;
        raidCore = null;
    }

    public RaidState getRaidState() { return this.raidState; }
    public Calendar getRaidDate() { return this.raidDate; }
    public FactionObject getAttackingFaction() { return this.attackingFaction; }
    public ArrayList<Chunk> getAttackedChunks() { return this.attackedChunks; }
    public ItemStack getRaidCore() { return this.raidCore; }

    public void setRaidCore(ItemStack raidCore) { this.raidCore = raidCore; }
    public void setRaidState(RaidState raidState) { this.raidState = raidState; }
}
