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

import java.util.*;

public class RaidManager {

    private final SimpleFactions plugin;
    public RaidManager(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    public HashMap<FactionObject, ArrayList<RaidInfoObject>> waitingRaids = new HashMap<>();
    public HashMap<FactionObject, ArrayList<RaidInfoObject>> currentRaids = new HashMap<>();

    private final int TIME_FOR_PREP_PHASE = 120 * 20;
    private final int TIME_FOR_HOLD_GROUNDS_PHASE = 120 * 20;
    private final int TIME_DURING_HOLD_GROUNDS_PHASE = 60 * 20;
    private final int TIME_FOR_CTF_PHASE = 120 * 20;

    public BukkitTask task;

    public boolean SendRaidDeclaration(Player sender, ArrayList<Chunk> attackedChunks, FactionObject defendingFaction, String raidDate) {
        for (RaidInfoObject raidInfo : currentRaids.get(defendingFaction)) {

            // Check if the defending faction doesn't already have a raid going on this date
            if (dateToCalendar(raidDate) != null && raidInfo.getRaidDate().get(Calendar.DAY_OF_YEAR) == (dateToCalendar(raidDate).get(Calendar.DAY_OF_YEAR))) {
                // RAID CANT HAPPEN
                sender.sendMessage(ChatColor.RED + "This faction is already being attacked at this date !\n Change it !");
                return false;
            }
        }

        // Check if the attacked chunks are in a WEAK state
        for (Chunk chunk : attackedChunks) {
            if (!plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(sender.getUniqueId()).getHardClaimedChunks().contains(chunk)) {
                // RAID CANT HAPPEN
                sender.sendMessage(ChatColor.RED + "The selected chunks are not in a weak state !");
                return false;
            }
        }

        addRaidToFaction(defendingFaction, new RaidInfoObject(RaidState.WAITING, dateToCalendar(raidDate), plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(sender.getUniqueId()), attackedChunks));
        return true;
    }

    public void StartRaidPrepPhase(RaidInfoObject raidInfo, FactionObject defendingFaction){
        if (raidInfo.getRaidState() != RaidState.START) return;
        UiPhaseChange(raidInfo, defendingFaction, "Preparation phase has started!", "Prepare to fight!", "Preparation phase has started!", "Prepare to fight!");

        new BukkitRunnable() {

            // Run Once
            // Time for each faction to prepare before the raid
            int timer = TIME_FOR_PREP_PHASE; // 20 ticks per seconds
            boolean corePlaced = false;
            boolean coreGiven = false;

            @Override
            public void run() {

                //region create & give core
                while (!coreGiven) {

                    // Get an array of all the player defending the faction
                    ArrayList<UUID> defendingFactionMembers = new ArrayList<>(defendingFaction.getFactionMembers());
                    for (UUID uuid : defendingFactionMembers) {
                        if (checkPlayer(uuid) != null && Bukkit.getPlayer(uuid).isOnline() && raidInfo.getAttackedChunks().contains(checkPlayer(uuid).getLocation().getChunk())){
                            defendingFactionMembers.add(uuid);
                        }
                    }

                    // Get the random player and giving him the core
                    if (!defendingFactionMembers.isEmpty()) {

                        // Get a random player
                        Random random = new Random();
                        UUID randomPlayer = defendingFactionMembers.get(random.nextInt(defendingFactionMembers.size()));

                        // Create the ItemStack
                        ItemStack core = new ItemStack(Material.NETHERITE_BLOCK);
                        ItemMeta coreMeta = core.getItemMeta();
                        coreMeta.setDisplayName(ChatColor.BLACK + ChatColor.BOLD.toString() + "RAID CORE");
                        core.setItemMeta(coreMeta);

                        // Add to player inventory or drop it on the ground (inventory full)
                        if (checkPlayer(randomPlayer) == null) break;
                        var fullInventory = Bukkit.getPlayer(randomPlayer).getInventory().addItem(core);
                        if (!fullInventory.isEmpty()) Bukkit.getPlayer(randomPlayer).getWorld().dropItem(Bukkit.getPlayer(randomPlayer).getLocation(), core);

                        raidInfo.setRaidCore(core);
                        coreGiven = true;
                    }
                }
                //endregion

                //region place core randomly or and start the "hold grounds" phase
                if (timer <= 0) {

                    // Check first if the core has been placed, if not, place it randomly
                    if (!corePlaced) {
                        Random random = new Random();
                        Chunk randomChunk = raidInfo.getAttackedChunks().get(random.nextInt(raidInfo.getAttackedChunks().size()));

                        int xCord = random.nextInt(16, 16);
                        int zCord = random.nextInt(16, 16);
                        int yCord = randomChunk.getChunkSnapshot().getHighestBlockYAt(xCord, zCord) - 5;

                        // Place the core
                        Block coreBlock = randomChunk.getBlock(xCord, yCord, zCord);
                        coreBlock.setType(Material.NETHERITE_BLOCK);
                        coreBlock.setMetadata("CoreRaidBlock", new FixedMetadataValue(plugin, defendingFaction.getFactionName()));

                        // Clear the surrounding blocks
                        for (int i = -1; i <= 1; i++) {
                            for (int j = -1; j <= 1; j++) {
                                randomChunk.getBlock(xCord + i, yCord + j, zCord).setType(Material.AIR);
                            }
                        }

                        raidInfo.setRaidCore(new ItemStack(coreBlock.getType(), 1, coreBlock.getData()));
                        corePlaced = true;
                    }

                    // Time has run out, start the raid
                    StartHoldGroundsPhase(raidInfo, defendingFaction);
                    cancel();
                    return;
                }
                //endregion

                // The core has been placed by a player, the raid will start when the time has run out
                if (raidInfo.getRaidState() == RaidState.GROUNDS) corePlaced = true;

                // The time has run out, and the core has been placed, start the raid
                if (timer <= 0 && corePlaced) {
                    StartHoldGroundsPhase(raidInfo, defendingFaction);
                    cancel();
                    return;
                }

                timer -= 20;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    public void StartHoldGroundsPhase(RaidInfoObject raidInfo, FactionObject defendingFaction){
        if (raidInfo.getRaidState() != RaidState.GROUNDS) return;
        UiPhaseChange(raidInfo, defendingFaction, "The hold grounds phase has start!", "hold the convoyed land for " + TIME_DURING_HOLD_GROUNDS_PHASE / 20 + " seconds !", "The hold grounds phase has start!", "defend the convoyed land " + TIME_DURING_HOLD_GROUNDS_PHASE / 20 + " seconds");

        new BukkitRunnable() {

            int timer = TIME_FOR_HOLD_GROUNDS_PHASE;
            int holdGroundsTimer = TIME_DURING_HOLD_GROUNDS_PHASE;

            @Override
            public void run() {
                // The condition to pass to the next phase is that the number of attacking players has to be greater than
                // the number of defending players for TIME_DURING_HOLD_GROUNDS_PHASE seconds

                if (countFactionPlayer(raidInfo.getAttackedChunks(), raidInfo.getAttackingFaction()) > countFactionPlayer(raidInfo.getAttackedChunks(), defendingFaction)){
                    holdGroundsTimer -= 20;
                    if (holdGroundsTimer <= 0) {
                        StartCaptureTheFlagPhase(raidInfo, defendingFaction);
                        cancel();
                    }
                } else{
                    holdGroundsTimer = TIME_DURING_HOLD_GROUNDS_PHASE;
                }

                if (timer <= 0) {
                    // Cancel raid, time has run out DEFEND WIN
                    UiPhaseChange(raidInfo, defendingFaction, "Time has run out!", "You lost!", "Time has run out!", "You won!");
                }

                timer -= 20;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    public void StartCaptureTheFlagPhase(RaidInfoObject raidInfo, FactionObject defendingFaction){
        if (raidInfo.getRaidState() != RaidState.CAPTURE_FLAG) return;
        UiPhaseChange(raidInfo, defendingFaction, "The capture the flag phase has started !", "Find and destroy the core !", "The capture the flag phase has started !", "Defend the core !");

        new BukkitRunnable() {

            int timer = TIME_FOR_CTF_PHASE;

            @Override
            public void run() {
                // The condition to pass to the next phase and win the raid is that the attacking faction finds and destroys
                // the defending raid core within the time of the raid (TIME_FOR_CTF_PHASE)

                // Here the win condition is checked by an event listener (onBlockBreak)
                // So this BukkitRunnable is only used to cancel the raid if the win condition is not met (time has run out)

                if (timer <= 0) {
                    // Cancel raid, time has run out DEFEND WIN
                    UiPhaseChange(raidInfo, defendingFaction, "Time has run out!", "You lost!", "Time has run out!", "You won!");
                }

                timer -= 20;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    public void EndRaid(RaidInfoObject raidInfo, FactionObject defendingFaction){
        if (raidInfo.getRaidState() != RaidState.END) return;
        UiPhaseChange(raidInfo, defendingFaction, "Raid ended!", "You won!", "Raid ended!", "You lost!");

        // If the raid gets to this phase, the ATTACK faction has won
    }

    public void StartCheckForWaitingRaids(){
        task = new BukkitRunnable() {

            @Override
            public void run() {
                for (FactionObject faction : waitingRaids.keySet()) {
                    for (RaidInfoObject raidInfo : waitingRaids.get(faction)) {
                        if (raidInfo.getRaidState() == RaidState.WAITING) {
                            if (checkWaitingRaids(Calendar.getInstance(), raidInfo.getRaidDate())) {
                                raidInfo.setRaidState(RaidState.START);
                                removeRaidToFaction(faction, raidInfo);
                                addRaidToFaction(faction, raidInfo);
                                StartRaidPrepPhase(raidInfo, faction);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1200); // Check every minute (20 ticks * 60 seconds)

    }

    // Helper method
    private void addRaidToFaction(FactionObject faction, RaidInfoObject raidInfo){
        var raids = currentRaids.get(faction);
        raids.add(raidInfo);
        waitingRaids.put(faction, raids);
    }

    private void removeRaidToFaction(FactionObject faction, RaidInfoObject raidInfo){
        var raids = currentRaids.get(faction);
        if (!currentRaids.containsKey(faction) || raids.contains(raidInfo)) return;
        raids.remove(raidInfo);
        waitingRaids.put(faction, raids);
    }

    private Calendar dateToCalendar(String date) {
        // Date is formated: DD-MM-YYYY:TTTT (UTC + 0)
        if (date.length() != 15) return null;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Integer.parseInt(date.substring(6, 9)));
        cal.set(Calendar.MONTH, Integer.parseInt(date.substring(3, 5)) - 1);
        cal.set(Calendar.DAY_OF_YEAR, Integer.parseInt(date.substring(0, 2)));
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(date.substring(10, 12)));
        cal.set(Calendar.MINUTE, Integer.parseInt(date.substring(13, 15)));

        return cal;
    }

    private boolean checkWaitingRaids(Calendar nowDate, Calendar raidDate){
        return (
                (raidDate.get(Calendar.YEAR) - nowDate.get(Calendar.YEAR)) <= 1 &&
                (raidDate.get(Calendar.DAY_OF_YEAR) - raidDate.get(Calendar.DAY_OF_YEAR)) <= 1 &&
                (raidDate.get(Calendar.HOUR_OF_DAY) - nowDate.get(Calendar.HOUR_OF_DAY)) <= 1 &&
                (raidDate.get(Calendar.MINUTE) - nowDate.get(Calendar.MINUTE)) <= 0
        );
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
            return offlinePlayer.getPlayer();  // This might still be null if not loaded
        } else {
            plugin.getLogger().warning("Player with UUID " + playerUUID + " has never played on this server.");
            return null;
        }
    }

    private int countFactionPlayer(ArrayList<Chunk> chunks, FactionObject faction){
        int count = 0;
        for (UUID playerUUID : faction.getFactionMembers()){
            if (checkPlayer(playerUUID) != null && Bukkit.getPlayer(playerUUID).isOnline() && chunks.contains(Bukkit.getPlayer(playerUUID).getLocation().getChunk())) {
                count++;
            }
        }
        return count;
    }

    private void UiPhaseChange(RaidInfoObject raidInfoObject, FactionObject defendingFaction, String attackingTitle, String attackingSubTitle, String defendingTitle, String defendingSubTitle){
        // Attacking faction
        for (UUID uuid : raidInfoObject.getAttackingFaction().getFactionMembers()){
            if (checkPlayer(uuid) != null && Bukkit.getPlayer(uuid).isOnline() && raidInfoObject.getAttackedChunks().contains(checkPlayer(uuid).getLocation().getChunk())){
                Bukkit.getPlayer(uuid).sendTitle(ChatColor.RED + attackingTitle, ChatColor.RED + attackingSubTitle);
            }
        }

        // Defending faction
        for (UUID uuid : defendingFaction.getFactionMembers()){
            if (checkPlayer(uuid) != null && Bukkit.getPlayer(uuid).isOnline() && raidInfoObject.getAttackedChunks().contains(checkPlayer(uuid).getLocation().getChunk())){
                Bukkit.getPlayer(uuid).sendTitle(ChatColor.RED + defendingTitle, ChatColor.RED + defendingSubTitle);
            }
        }
    }
}
