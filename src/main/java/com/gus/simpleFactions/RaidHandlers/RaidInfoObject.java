package com.gus.simpleFactions.RaidHandlers;

import com.gus.simpleFactions.Enums.RaidState;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import org.bukkit.Chunk;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Calendar;

public class RaidInfoObject {

    private RaidState raidState;
    private final Calendar raidDate;
    private final FactionObject attackingFaction;
    private final FactionObject defendingFaction;
    private final ArrayList<Chunk> attackedChunks;
    private Integer currentStateTimer;
    private ItemStack raidCore;

    public RaidInfoObject(Calendar raidDate, FactionObject attackingFaction, FactionObject defendingFaction, ArrayList<Chunk> attackedChunks) {
        this.raidState = RaidState.WAITING;
        this.raidDate = raidDate;
        this.attackingFaction = attackingFaction;
        this.defendingFaction = defendingFaction;
        this.attackedChunks = attackedChunks;
        currentStateTimer = 0;
        raidCore = null;
    }

    public RaidState getRaidState() { return this.raidState; }
    public Calendar getRaidDate() { return this.raidDate; }
    public FactionObject getAttackingFaction() { return this.attackingFaction; }
    public FactionObject getDefendingFaction() { return this.defendingFaction; }
    public ArrayList<Chunk> getAttackedChunks() { return this.attackedChunks; }
    public ItemStack getRaidCore() { return this.raidCore; }

    public void setRaidCore(ItemStack raidCore) { this.raidCore = raidCore; }
    public void setRaidState(RaidState raidState) { this.raidState = raidState; }

    public Integer getCurrentStateTimer() { return this.currentStateTimer; }
    public void setCurrentStateTimer(int currentStateTimer) { this.currentStateTimer = currentStateTimer; }
}
