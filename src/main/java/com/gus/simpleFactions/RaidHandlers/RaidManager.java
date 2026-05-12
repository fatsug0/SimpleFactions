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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class RaidManager {

    private final SimpleFactions plugin;

    public RaidManager(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    private final HashMap<FactionObject, ArrayList<RaidInfoObject>> waitingRaids = new HashMap<>();

    public HashMap<FactionObject, ArrayList<RaidInfoObject>> getWaitingRaids() {
        return waitingRaids;
    }

    private final HashMap<FactionObject, ArrayList<RaidInfoObject>> currentRaids = new HashMap<>();

    public HashMap<FactionObject, ArrayList<RaidInfoObject>> getCurrentRaids() {
        return currentRaids;
    }

    private final int TIME_FOR_PREP_PHASE = 120 * 20;
    private final int TIME_FOR_HOLD_GROUNDS_PHASE = 120 * 20;
    private final int TIME_DURING_HOLD_GROUNDS_PHASE = 60 * 20;
    private final int TIME_FOR_CTF_PHASE = 120 * 20;
    private static final DateTimeFormatter RAID_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-uuuu:HHmm");

    public BukkitTask task;

    private final HashMap<FactionObject, ArrayList<SelectedChunk>> currentFactionSelection = new HashMap<>();

    private record SelectedChunk(FactionObject defendingFaction, Chunk chunk) {
    }

    public void addCurrentFactionSelection(FactionObject attackingFaction, FactionObject defendingFaction, Chunk chunk, Player sender) {
        if (currentFactionSelection.containsKey(attackingFaction)) {
            ArrayList<SelectedChunk> selectedChunks = currentFactionSelection.get(attackingFaction);
            for (SelectedChunk selectedChunk : selectedChunks) {
                if (selectedChunk.defendingFaction.equals(defendingFaction) && selectedChunk.chunk.equals(chunk)) {
                    sender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You already selected this chunk !");
                    return;
                }
            }
            selectedChunks.add(new SelectedChunk(defendingFaction, chunk));
        } else {
            currentFactionSelection.put(attackingFaction, new ArrayList<>(List.of(new SelectedChunk(defendingFaction, chunk))));
        }
    }

    public void removeCurrentFactionSelection(FactionObject faction) {
        currentFactionSelection.remove(faction);
    }

    public void SendRaidDeclaration(Player sender, FactionObject attackingFaction, FactionObject defendingFaction, String raidDate) {
        Calendar parsedRaidDate = dateToCalendar(raidDate);
        if (parsedRaidDate == null) {
            sender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Invalid raid date format. Use DD-MM-YYYY:TTTT");
            return;
        }

        for (RaidInfoObject raidInfo : waitingRaids.getOrDefault(defendingFaction, new ArrayList<>())) {
            if (raidInfo.getRaidDate().getTimeInMillis() == parsedRaidDate.getTimeInMillis()) {
                sender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "This faction is already being attacked at this date !\n Change it !");
                return;
            }
        }
        for (RaidInfoObject raidInfo : currentRaids.getOrDefault(defendingFaction, new ArrayList<>())) {
            if (raidInfo.getRaidDate().getTimeInMillis() == parsedRaidDate.getTimeInMillis()) {
                sender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "This faction is already being attacked at this date !\n Change it !");
                return;
            }
        }

        if (!currentFactionSelection.containsKey(attackingFaction) || currentFactionSelection.get(attackingFaction).isEmpty()) {
            sender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You haven't selected any chunks");
            return;
        }

        ArrayList<Chunk> attackedChunks = new ArrayList<>();
        for (SelectedChunk selectedChunk : currentFactionSelection.get(attackingFaction)) {
            if (selectedChunk.defendingFaction.equals(defendingFaction)) {
                attackedChunks.add(selectedChunk.chunk);
            }
        }

        if (attackedChunks.isEmpty()) {
            sender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You haven't selected any chunks for this defending faction");
            return;
        }

        RaidInfoObject raidInfoObject = new RaidInfoObject(
                RaidState.WAITING,
                parsedRaidDate,
                plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(sender.getUniqueId()),
                attackedChunks
        );
        addRaidToMap(waitingRaids, defendingFaction, raidInfoObject);
    }

    public void StartRaidPrepPhase(RaidInfoObject raidInfo, FactionObject defendingFaction) {
        if (raidInfo.getRaidState() != RaidState.START) return;
        UiPhaseChange(raidInfo, defendingFaction, "Preparation phase has started!", "Prepare to fight!", "Preparation phase has started!", "Prepare to fight!");

        new BukkitRunnable() {
            int timer = TIME_FOR_PREP_PHASE;
            boolean corePlaced = false;
            boolean coreGiven = false;

            @Override
            public void run() {
                if (!coreGiven) {
                    ArrayList<UUID> eligibleDefenders = new ArrayList<>();
                    for (UUID uuid : defendingFaction.getFactionMembers()) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null && player.isOnline() && raidInfo.getAttackedChunks().contains(player.getLocation().getChunk())) {
                            eligibleDefenders.add(uuid);
                        }
                    }

                    if (!eligibleDefenders.isEmpty()) {
                        Random random = new Random();
                        UUID randomPlayer = eligibleDefenders.get(random.nextInt(eligibleDefenders.size()));
                        Player player = Bukkit.getPlayer(randomPlayer);
                        if (player != null) {
                            ItemStack core = new ItemStack(Material.NETHERITE_BLOCK);
                            ItemMeta coreMeta = core.getItemMeta();
                            if (coreMeta != null) {
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

                        int xCord = random.nextInt(16);
                        int zCord = random.nextInt(16);
                        int yCord = Math.max(1, randomChunk.getChunkSnapshot().getHighestBlockYAt(xCord, zCord) - 5);

                        Block coreBlock = randomChunk.getBlock(xCord, yCord, zCord);
                        coreBlock.setType(Material.NETHERITE_BLOCK);
                        coreBlock.setMetadata("CoreRaidBlock", new FixedMetadataValue(plugin, defendingFaction.getFactionName()));

                        for (int i = -1; i <= 1; i++) {
                            for (int j = -1; j <= 1; j++) {
                                randomChunk.getBlock(xCord + i, yCord + j, zCord).setType(Material.AIR);
                            }
                        }

                        raidInfo.setRaidCore(new ItemStack(coreBlock.getType(), 1, coreBlock.getData()));
                    }

                    raidInfo.setRaidState(RaidState.GROUNDS);
                    StartHoldGroundsPhase(raidInfo, defendingFaction);
                    cancel();
                    return;
                }

                if (raidInfo.getRaidState() == RaidState.GROUNDS) {
                    corePlaced = true;
                }

                timer -= 20;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    public void StartHoldGroundsPhase(RaidInfoObject raidInfo, FactionObject defendingFaction) {
        if (raidInfo.getRaidState() != RaidState.GROUNDS) return;
        UiPhaseChange(raidInfo, defendingFaction, "The hold grounds phase has start!", "hold the convoyed land for " + TIME_DURING_HOLD_GROUNDS_PHASE / 20 + " seconds !", "The hold grounds phase has start!", "defend the convoyed land " + TIME_DURING_HOLD_GROUNDS_PHASE / 20 + " seconds");

        new BukkitRunnable() {
            int timer = TIME_FOR_HOLD_GROUNDS_PHASE;
            int holdGroundsTimer = TIME_DURING_HOLD_GROUNDS_PHASE;

            @Override
            public void run() {
                if (countFactionPlayer(raidInfo.getAttackedChunks(), raidInfo.getAttackingFaction()) > countFactionPlayer(raidInfo.getAttackedChunks(), defendingFaction)) {
                    holdGroundsTimer -= 20;
                    if (holdGroundsTimer <= 0) {
                        raidInfo.setRaidState(RaidState.CAPTURE_FLAG);
                        StartCaptureTheFlagPhase(raidInfo, defendingFaction);
                        cancel();
                        return;
                    }
                } else {
                    holdGroundsTimer = TIME_DURING_HOLD_GROUNDS_PHASE;
                }

                if (timer <= 0) {
                    UiPhaseChange(raidInfo, defendingFaction, "Time has run out!", "You lost!", "Time has run out!", "You won!");
                    raidInfo.setRaidState(RaidState.END);
                    removeRaidFromMap(currentRaids, defendingFaction, raidInfo);
                    cancel();
                    return;
                }

                timer -= 20;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    public void StartCaptureTheFlagPhase(RaidInfoObject raidInfo, FactionObject defendingFaction) {
        if (raidInfo.getRaidState() != RaidState.CAPTURE_FLAG) return;
        UiPhaseChange(raidInfo, defendingFaction, "The capture the flag phase has started !", "Find and destroy the core !", "The capture the flag phase has started !", "Defend the core !");

        new BukkitRunnable() {
            int timer = TIME_FOR_CTF_PHASE;

            @Override
            public void run() {
                if (timer <= 0) {
                    UiPhaseChange(raidInfo, defendingFaction, "Time has run out!", "You lost!", "Time has run out!", "You won!");
                    raidInfo.setRaidState(RaidState.END);
                    removeRaidFromMap(currentRaids, defendingFaction, raidInfo);
                    cancel();
                    return;
                }

                timer -= 20;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    public void EndRaid(RaidInfoObject raidInfo, FactionObject defendingFaction) {
        if (raidInfo.getRaidState() != RaidState.END) return;
        UiPhaseChange(raidInfo, defendingFaction, "Raid ended!", "You won!", "Raid ended!", "You lost!");
        removeRaidFromMap(currentRaids, defendingFaction, raidInfo);
    }

    public void StartCheckForWaitingRaids() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                ArrayList<Map.Entry<FactionObject, RaidInfoObject>> raidsToStart = new ArrayList<>();

                for (Map.Entry<FactionObject, ArrayList<RaidInfoObject>> entry : waitingRaids.entrySet()) {
                    FactionObject faction = entry.getKey();
                    for (RaidInfoObject raidInfo : entry.getValue()) {
                        if (raidInfo.getRaidState() == RaidState.WAITING && checkWaitingRaids(Calendar.getInstance(), raidInfo.getRaidDate())) {
                            raidsToStart.add(new AbstractMap.SimpleEntry<>(faction, raidInfo));
                        }
                    }
                }

                for (Map.Entry<FactionObject, RaidInfoObject> raidToStart : raidsToStart) {
                    FactionObject faction = raidToStart.getKey();
                    RaidInfoObject raidInfo = raidToStart.getValue();
                    raidInfo.setRaidState(RaidState.START);
                    removeRaidFromMap(waitingRaids, faction, raidInfo);
                    addRaidToMap(currentRaids, faction, raidInfo);
                    StartRaidPrepPhase(raidInfo, faction);
                }
            }
        }.runTaskTimer(plugin, 0, 1200);
    }

    private void addRaidToMap(Map<FactionObject, ArrayList<RaidInfoObject>> map, FactionObject faction, RaidInfoObject raidInfo) {
        ArrayList<RaidInfoObject> raids = map.computeIfAbsent(faction, ignored -> new ArrayList<>());
        raids.add(raidInfo);
    }

    private void removeRaidFromMap(Map<FactionObject, ArrayList<RaidInfoObject>> map, FactionObject faction, RaidInfoObject raidInfo) {
        ArrayList<RaidInfoObject> raids = map.get(faction);
        if (raids == null) return;
        raids.remove(raidInfo);
        if (raids.isEmpty()) {
            map.remove(faction);
        }
    }

    private Calendar dateToCalendar(String date) {
        if (date == null || date.length() != 15) return null;

        try {
            LocalDateTime parsed = LocalDateTime.parse(date, RAID_DATE_FORMAT);
            GregorianCalendar calendar = GregorianCalendar.from(parsed.atZone(ZoneOffset.UTC));
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar;
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private boolean checkWaitingRaids(Calendar nowDate, Calendar raidDate) {
        if (nowDate == null || raidDate == null) return false;
        return !nowDate.before(raidDate);
    }

    private Player checkPlayer(UUID playerUUID) {
        if (playerUUID == null) {
            plugin.getLogger().warning("Null UUID passed to checkPlayer");
            return null;
        }
        Player onlinePlayer = Bukkit.getPlayer(playerUUID);
        if (onlinePlayer != null) {
            return onlinePlayer;
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
        if (offlinePlayer.hasPlayedBefore()) {
            return offlinePlayer.getPlayer();
        } else {
            plugin.getLogger().warning("Player with UUID " + playerUUID + " has never played on this server.");
            return null;
        }
    }

    private int countFactionPlayer(ArrayList<Chunk> chunks, FactionObject faction) {
        int count = 0;
        for (UUID playerUUID : faction.getFactionMembers()) {
            Player player = checkPlayer(playerUUID);
            if (player != null && player.isOnline() && chunks.contains(player.getLocation().getChunk())) {
                count++;
            }
        }
        return count;
    }

    private void UiPhaseChange(RaidInfoObject raidInfoObject, FactionObject defendingFaction, String attackingTitle, String attackingSubTitle, String defendingTitle, String defendingSubTitle) {
        for (UUID uuid : raidInfoObject.getAttackingFaction().getFactionMembers()) {
            Player player = checkPlayer(uuid);
            if (player != null && player.isOnline() && raidInfoObject.getAttackedChunks().contains(player.getLocation().getChunk())) {
                player.sendTitle(ChatColor.RED + attackingTitle, ChatColor.RED + attackingSubTitle);
            }
        }

        for (UUID uuid : defendingFaction.getFactionMembers()) {
            Player player = checkPlayer(uuid);
            if (player != null && player.isOnline() && raidInfoObject.getAttackedChunks().contains(player.getLocation().getChunk())) {
                player.sendTitle(ChatColor.RED + defendingTitle, ChatColor.RED + defendingSubTitle);
            }
        }
    }
}
