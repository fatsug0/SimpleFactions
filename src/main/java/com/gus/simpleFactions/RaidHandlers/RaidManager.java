package com.gus.simpleFactions.RaidHandlers;

import com.gus.simpleFactions.Enums.RaidState;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class RaidManager {

    private final SimpleFactions plugin;
    private final RaidHelper helper;
    public RaidManager(SimpleFactions plugin) {
        this.plugin = plugin;
        this.helper = new RaidHelper(plugin);
        powerToChunkSelectionCoefficient = plugin.getConfig().getDouble("faction.powerToChunkSelectionCoefficient");
    }

    private final HashMap<FactionObject, ArrayList<RaidInfoObject>> waitingRaids = new HashMap<>();
    private final HashMap<FactionObject, ArrayList<RaidInfoObject>> currentRaids = new HashMap<>();
    public BukkitTask task;

    public double powerToChunkSelectionCoefficient;

    // Time constant
    private final int TIME_FOR_PREP_PHASE = 120 * 20;
    private final int TIME_FOR_HOLD_GROUNDS_PHASE = 120 * 20;
    private final int TIME_DURING_HOLD_GROUNDS_PHASE = 60 * 20;
    private final int TIME_FOR_CTF_PHASE = 120 * 20;
    private final int TIME_FOR_PAUSE = 60 * 20;

    private final HashMap<FactionObject, ArrayList<Chunk>> currentFactionSelection = new HashMap<>();
    public HashMap<FactionObject, ArrayList<RaidInfoObject>> getWaitingRaids() {
        return waitingRaids;
    }

    public HashMap<FactionObject, ArrayList<RaidInfoObject>> getCurrentRaids() {
        return currentRaids;
    }

    public HashMap<FactionObject, ArrayList<Chunk>> getCurrentFactionSelection() {
        return currentFactionSelection;
    }
    public void addChunkToSelection(FactionObject faction, Chunk chunk) {
        ArrayList<Chunk> chunks = currentFactionSelection.get(faction);
        if (chunks == null) {
            currentFactionSelection.put(faction, new ArrayList<>(Collections.singletonList(chunk)));
        } else {
            if (chunks.contains(chunk)) {
                return;
            }
            chunks.add(chunk);
        }
    }

    public void removeCurrentFactionSelection(FactionObject faction) {
        currentFactionSelection.remove(faction);
    }

    private void addRaidToMap(Map<FactionObject, ArrayList<RaidInfoObject>> map, FactionObject faction, RaidInfoObject raidInfo) {
        map.computeIfAbsent(faction, ignored -> new ArrayList<>()).add(raidInfo);
    }

    private final HashMap<FactionObject, Date> raidDeclarationCooldowns = new HashMap<>();
    private final HashMap<FactionObject, Integer> raidSanctions = new HashMap<>();

    public void SendRaidDeclaration(Player sender, FactionObject attackingFaction, FactionObject defendingFaction, String raidDate) {
        Calendar parsedRaidDate = helper.dateToCalendar(raidDate);

        // Wrong date format
        if (parsedRaidDate == null) {
            sender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Invalid raid date format. Use DD-MM-YYYY:HHMM");
            return;
        }

        // Can't raid in the past
        if (!Calendar.getInstance().getTime().before(parsedRaidDate.getTime())) {
            sender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Invalid raid time, you can't raid in the past !");
            return;
        }

        // Check raid declaration cooldown
        Date lastDeclaration = raidDeclarationCooldowns.get(attackingFaction);
        if (lastDeclaration != null && Calendar.getInstance().getTimeInMillis() - lastDeclaration.getTime() < 3600000) {
            sender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Raid declaration cooldown !");
            return;
        }

        // Faction cannot be attacked twice at the same time
        ArrayList<RaidInfoObject> existingDefendingRaids = new ArrayList<>();
        existingDefendingRaids.addAll(waitingRaids.getOrDefault(defendingFaction, new ArrayList<>()));
        existingDefendingRaids.addAll(currentRaids.getOrDefault(defendingFaction, new ArrayList<>()));
        for (RaidInfoObject raidInfo : existingDefendingRaids) {
            if (Math.abs(raidInfo.getRaidDate().getTimeInMillis() - parsedRaidDate.getTimeInMillis()) <= 1800000) { // 30 minutes grace time between raids
                sender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "This faction is already being attacked at this date !\n Change it !");
                return;
            }
        }

        // Can't raid your own faction
        if (attackingFaction.equals(defendingFaction)) {
            sender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You cannot raid your own faction.");
            return;
        }

        // No chunks selected for the raid
        if (!currentFactionSelection.containsKey(attackingFaction) ||
                currentFactionSelection.get(attackingFaction).stream().noneMatch(chunk -> defendingFaction.equals(plugin.factionManager.factionLandService.getLinkedChunks().get(chunk)))) {
            sender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Your selection for this faction is empty !");
            return;
        }

        // Check if the chunks selected are always in the same faction and weak
        ArrayList<Chunk> finalRaidSelection = new ArrayList<>();
        for (Chunk chunk : currentFactionSelection.get(attackingFaction)) {
            if (defendingFaction.equals(plugin.factionManager.factionLandService.getLinkedChunks().get(chunk)) &&
                    defendingFaction.getWeakClaimedChunks().contains(chunk) &&
                    !finalRaidSelection.contains(chunk)) {
                finalRaidSelection.add(chunk);
            } else {
                sender.sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString() + "Removed one chunk from your selection, it isn't weak or from the same faction anymore !");
            }
        }

        // Clear remaining raiding selection of faction
        currentFactionSelection.remove(attackingFaction);

        if (finalRaidSelection.isEmpty()) {
            sender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Your selection for this faction is empty !");
            return;
        }

        // Add the new raid to the system, waiting for start
        addRaidToMap(waitingRaids, defendingFaction, new RaidInfoObject(parsedRaidDate, attackingFaction, defendingFaction, finalRaidSelection));

        // Last raid declaration to check for cooldown
        raidDeclarationCooldowns.put(attackingFaction, Calendar.getInstance().getTime());
        sender.sendMessage(ChatColor.GREEN + "Raid declared.");
    }

    public void StartRaidPrepPhase(RaidInfoObject raidInfo) {
        if (raidInfo.getRaidState() != RaidState.START) return;
        helper.UiPhaseChange(raidInfo, "Preparation phase has started!", "Prepare to fight and win those chunks!", "Preparation phase has started!", "Prepare to fight to defend your land!");

        new BukkitRunnable() {
            int timer = TIME_FOR_PREP_PHASE;
            boolean corePlaced = false;
            boolean coreGiven = false;

            @Override
            public void run() {
                // The core carrier can place the core early (ClaimedChunksChecker#onBlockPlace moves
                // the raid straight to GROUNDS when that happens) - if so, skip the rest of the prep
                // timer immediately instead of waiting for it to expire and placing a second, random core.
                if (raidInfo.getRaidState() == RaidState.GROUNDS) {
                    corePlaced = true;
                    StartHoldGroundsPhase(raidInfo);
                    cancel();
                    return;
                }

                if (!coreGiven) {
                    ArrayList<UUID> eligibleDefenders = new ArrayList<>();
                    for (UUID uuid : raidInfo.getDefendingFaction().getFactionMembers()) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null && player.isOnline() && raidInfo.getAttackedChunks().contains(player.getLocation().getChunk())) {
                            eligibleDefenders.add(uuid);
                        }
                    }

                    if (eligibleDefenders.isEmpty()) {
                        int sanctions = raidSanctions.getOrDefault(raidInfo.getDefendingFaction(), 0) + 1;
                        raidSanctions.put(raidInfo.getDefendingFaction(), sanctions);
                        if (sanctions <= 3) {
                            helper.UiPhaseChange(raidInfo, "Raid can't start, no defending player available", "The raid is cancelled and a warning has been sent for the opposing faction...", null, null);
                            raidInfo.setRaidState(RaidState.END);
                            EndRaid(raidInfo, false);
                            cancel();
                            return;
                        }

                        raidSanctions.put(raidInfo.getDefendingFaction(), 0);
                        helper.UiPhaseChange(raidInfo, "Raid will start with no opposing team", "The opposing team has reached the allowed warnings before sanction, the raid will start with no opposing team...", null, null);
                        coreGiven = true;
                    }

                    if (!eligibleDefenders.isEmpty()) {
                        Random random = new Random();
                        UUID randomPlayer = eligibleDefenders.get(random.nextInt(eligibleDefenders.size()));
                        Player player = Bukkit.getPlayer(randomPlayer);
                        if (player != null) {
                            ItemStack core = new ItemStack(Material.NETHERITE_BLOCK);
                            ItemMeta coreMeta = core.getItemMeta();

                            if (coreMeta != null) {
                                NamespacedKey key = new NamespacedKey(plugin, "raid_flag");
                                coreMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, raidInfo.getDefendingFaction().getFactionName());
                                coreMeta.setDisplayName(ChatColor.BLACK + ChatColor.BOLD.toString() + "RAID CORE");
                                core.setItemMeta(coreMeta);
                            }

                            var fullInventory = player.getInventory().addItem(core);
                            if (!fullInventory.isEmpty()) {
                                player.getWorld().dropItem(player.getLocation(), core);
                            }

                            raidInfo.setRaidCore(core);
                            coreGiven = true;
                        }
                    }
                }

                if (timer <= 0) {
                    if (!corePlaced) {
                        Random random = new Random();
                        Chunk randomChunk = raidInfo.getAttackedChunks().get(random.nextInt(raidInfo.getAttackedChunks().size()));

                        int xCord = 1 + random.nextInt(14);
                        int zCord = 1 + random.nextInt(14);
                        int yCord = Math.max(1, randomChunk.getChunkSnapshot().getHighestBlockYAt(xCord, zCord) - 5);

                        for (int x = -1; x <= 1; x++) {
                            for (int y = -1; y <= 1; y++) {
                                for (int z = -1; z <= 1; z++) {
                                    randomChunk.getBlock(xCord + x, yCord + y, zCord + z).setType(Material.AIR);
                                }
                            }
                        }

                        Block coreBlock = randomChunk.getBlock(xCord, yCord, zCord);
                        coreBlock.setType(Material.NETHERITE_BLOCK);
                        coreBlock.setMetadata("CoreRaidBlock", new FixedMetadataValue(plugin, raidInfo.getDefendingFaction().getFactionName()));

                        raidInfo.setRaidCore(new ItemStack(Material.NETHERITE_BLOCK));
                    }

                    raidInfo.setRaidState(RaidState.GROUNDS);
                    StartHoldGroundsPhase(raidInfo);
                    cancel();
                    return;
                }

                timer -= 20;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    public void StartHoldGroundsPhase(RaidInfoObject raidInfo) {
        if (raidInfo.getRaidState() != RaidState.GROUNDS) return;
        helper.UiPhaseChange(raidInfo, "The hold grounds phase has start!", "hold the convoyed land for " + (raidInfo.getCurrentStateTimer() + TIME_FOR_HOLD_GROUNDS_PHASE) / 20 + " seconds !", "The hold grounds phase has start!", "defend the convoyed land " + TIME_DURING_HOLD_GROUNDS_PHASE / 20 + " seconds");

        new BukkitRunnable() {
            int timer = raidInfo.getCurrentStateTimer() + TIME_FOR_HOLD_GROUNDS_PHASE;
            int holdGroundsTimer = TIME_DURING_HOLD_GROUNDS_PHASE;

            @Override
            public void run() {
                if (!checkForEnoughPlayers(raidInfo)) {
                    raidInfo.setRaidState(RaidState.PAUSED);
                    PauseRaid(raidInfo, timer, RaidState.GROUNDS);
                    cancel();
                    return;
                }

                if (helper.countFactionPlayer(raidInfo.getAttackedChunks(), raidInfo.getAttackingFaction())
                        > helper.countFactionPlayer(raidInfo.getAttackedChunks(), raidInfo.getDefendingFaction())) {
                    holdGroundsTimer -= 20;

                    if (holdGroundsTimer <= 0) {
                        raidInfo.setRaidState(RaidState.CAPTURE_FLAG);
                        StartCaptureTheFlagPhase(raidInfo);
                        cancel();
                        return;
                    }

                } else {
                    holdGroundsTimer = TIME_DURING_HOLD_GROUNDS_PHASE;
                }

                if (timer <= 0) {
                    helper.UiPhaseChange(raidInfo, "Time has run out !", "You lost!", "Time has run out !", "You won!");
                    raidInfo.setRaidState(RaidState.END);
                    EndRaid(raidInfo, false);
                    cancel();
                    return;
                }

                timer -= 20;
                raidInfo.setCurrentStateTimer(timer);
                helper.UiHeadsUpDisplay(raidInfo);
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void StartCaptureTheFlagPhase(RaidInfoObject raidInfo) {
        if (raidInfo.getRaidState() != RaidState.CAPTURE_FLAG) return;
        helper.UiPhaseChange(raidInfo, "The capture the flag phase has started !", "Find and destroy the enemy flag, you have " + (raidInfo.getCurrentStateTimer() + TIME_FOR_CTF_PHASE) / 20 + " seconds !", "The capture the flag phase has started !", "Defend your core at all cost for " + (raidInfo.getCurrentStateTimer() + TIME_FOR_CTF_PHASE) / 20 + " seconds !");

        new BukkitRunnable() {
            int timer = raidInfo.getCurrentStateTimer() + TIME_FOR_CTF_PHASE;

            @Override
            public void run() {
                if (!checkForEnoughPlayers(raidInfo)) {
                    raidInfo.setRaidState(RaidState.PAUSED);
                    PauseRaid(raidInfo, timer, RaidState.CAPTURE_FLAG);
                    cancel();
                    return;
                }

                if (timer <= 0) {
                    helper.UiPhaseChange(raidInfo, "Time has run out !", "You lost!", "Time has run out !", "You won!");
                    raidInfo.setRaidState(RaidState.END);
                    EndRaid(raidInfo, false);
                    cancel();
                    return;
                }

                timer -= 20;
                raidInfo.setCurrentStateTimer(timer);
                helper.UiHeadsUpDisplay(raidInfo);
            }


        }.runTaskTimer(plugin, 0, 20);
    }

    private void PauseRaid(RaidInfoObject raidInfo, int savedTimer, RaidState resumeState) {
        if (raidInfo.getRaidState() != RaidState.PAUSED) return;

        new BukkitRunnable() {
            int timer = TIME_FOR_PAUSE;
            boolean pausedState = true;

            @Override
            public void run() {

                int attackerCount = helper.countFactionPlayer(raidInfo.getAttackedChunks(), raidInfo.getAttackingFaction());
                int defenderCount = helper.countFactionPlayer(raidInfo.getAttackedChunks(), raidInfo.getDefendingFaction());

                if (timer <= 0) {
                    raidInfo.setRaidState(RaidState.END);
                    EndRaid(raidInfo, false);
                    cancel();
                    return;
                }

                if (pausedState && attackerCount < 2) {
                    helper.UiPhaseChange(raidInfo,
                            "Raid is paused because of missing players",
                            "Raid is paused and waiting for more players to join the attackers side, after 1 minute the raid will be ended",
                            "Raid is paused because of missing players",
                            "Raid is paused and waiting for more players to join the attackers side, after 1 minute the raid will be ended");
                    pausedState = true;
                } else if (pausedState && defenderCount < 1) {
                    helper.UiPhaseChange(raidInfo,
                            "Raid is paused because of missing players",
                            "Raid is paused and waiting for more players to join the defender side, after 1 minute the raid will be ended",
                            "Raid is paused because of missing players",
                            "Raid is paused and waiting for more players to join the defender side, after 1 minute the raid will be ended");
                    pausedState = true;
                } else {
                    pausedState = false;
                    raidInfo.setRaidState(resumeState);
                    raidInfo.setCurrentStateTimer(savedTimer);
                    if (resumeState == RaidState.CAPTURE_FLAG) {
                        StartCaptureTheFlagPhase(raidInfo);
                    } else {
                        StartHoldGroundsPhase(raidInfo);
                    }
                    cancel();
                    return;
                }

                timer -= 5 * 20;
            }
        }.runTaskTimer(plugin, 0, 5 * 20);
    }

    public void EndRaid(RaidInfoObject raidInfo, boolean attackerWon) {
        helper.removeRaidFromMap(currentRaids, raidInfo.getDefendingFaction(), raidInfo);

        if (attackerWon) {
            for (Chunk chunk : raidInfo.getAttackedChunks()) {
                if (!raidInfo.getDefendingFaction().equals(plugin.factionManager.factionLandService.getLinkedChunks().get(chunk))) {
                    continue;
                }
                raidInfo.getDefendingFaction().removeWeakClaimedChunks(chunk);
                if (!raidInfo.getAttackingFaction().getWeakClaimedChunks().contains(chunk) &&
                        !raidInfo.getAttackingFaction().getHardClaimedChunks().contains(chunk)) {
                    raidInfo.getAttackingFaction().addWeakClaimedChunks(chunk);
                }
                plugin.factionManager.factionLandService.addLinkedChunk(chunk, raidInfo.getAttackingFaction());
            }

            helper.UiPhaseChange(raidInfo,
                    "Raid has ended",
                    "You have won the raid and captured the attacked chunks !",
                    "Raid has ended",
                    "You have lost the raid !");
        } else {
            helper.UiPhaseChange(raidInfo,
                    "Raid has ended",
                    "You have lost the raid !",
                    "Raid has ended",
                    "You have won the raid and kept your chunks !");
        }
    }

    public void StartCheckForWaitingRaids() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }

        task = new BukkitRunnable() {
            @Override
            public void run() {
                ArrayList<Map.Entry<FactionObject, RaidInfoObject>> raidsToStart = new ArrayList<>();

                for (Map.Entry<FactionObject, ArrayList<RaidInfoObject>> entry : waitingRaids.entrySet()) {
                    FactionObject defendingFaction = entry.getKey();
                    for (RaidInfoObject raidInfo : entry.getValue()) {
                        if (raidInfo.getRaidState() == RaidState.WAITING && helper.checkWaitingRaids(Calendar.getInstance(), raidInfo.getRaidDate())) {
                            raidsToStart.add(new AbstractMap.SimpleEntry<>(defendingFaction, raidInfo));
                        }
                    }
                }

                for (Map.Entry<FactionObject, RaidInfoObject> raidToStart : raidsToStart) {
                    FactionObject defendingFaction = raidToStart.getKey();
                    RaidInfoObject raidInfo = raidToStart.getValue();
                    raidInfo.setRaidState(RaidState.START);
                    helper.removeRaidFromMap(waitingRaids, defendingFaction, raidInfo);
                    addRaidToMap(currentRaids, defendingFaction, raidInfo);
                    StartRaidPrepPhase(raidInfo);
                }
            }
        }.runTaskTimer(plugin, 0, 1200);
    }

    private boolean checkForEnoughPlayers(RaidInfoObject raidInfo) {
        int attackerCount = helper.countFactionPlayer(raidInfo.getAttackedChunks(), raidInfo.getAttackingFaction());
        int defenderCount = helper.countFactionPlayer(raidInfo.getAttackedChunks(), raidInfo.getDefendingFaction());
        return attackerCount >= 2 && defenderCount >= 1;
    }
}
