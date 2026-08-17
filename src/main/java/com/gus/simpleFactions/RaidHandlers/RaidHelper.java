package com.gus.simpleFactions.RaidHandlers;

import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.SimpleFactions;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RaidHelper {
    private final SimpleFactions plugin;
    private static final DateTimeFormatter RAID_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-uuuu:HHmm");

    public RaidHelper(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    public void removeRaidFromMap(Map<FactionObject, ArrayList<RaidInfoObject>> map, FactionObject faction, RaidInfoObject raidInfo) {
        ArrayList<RaidInfoObject> raids = map.get(faction);
        if (raids == null) return;
        raids.remove(raidInfo);
        if (raids.isEmpty()) {
            map.remove(faction);
        }
    }

    public Calendar dateToCalendar(String date) {
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

    public boolean checkWaitingRaids(Calendar nowDate, Calendar raidDate) {
        if (nowDate == null || raidDate == null) return false;
        return !nowDate.before(raidDate);
    }

    public Player checkPlayer(UUID playerUUID) {
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

    public int countFactionPlayer(ArrayList<Chunk> chunks, FactionObject faction) {
        int count = 0;
        for (UUID playerUUID : faction.getFactionMembers()) {
            Player player = checkPlayer(playerUUID);
            if (player != null && player.isOnline() && chunks.contains(player.getLocation().getChunk()) && player.getWorld().equals(Bukkit.getWorld("world")) && player.getGameMode().equals(GameMode.SURVIVAL)) {
                count++;
            }
        }
        return count;
    }

    public void UiPhaseChange(RaidInfoObject raidInfoObject, String attackingTitle, String attackingSubTitle, String defendingTitle, String defendingSubTitle) {
        for (UUID uuid : raidInfoObject.getAttackingFaction().getFactionMembers()) {
            Player player = checkPlayer(uuid);
            if (player != null && player.isOnline() && raidInfoObject.getAttackedChunks().contains(player.getLocation().getChunk())) {
                player.sendTitle(ChatColor.RED + attackingTitle, ChatColor.RED + attackingSubTitle);
            }
        }

        for (UUID uuid : raidInfoObject.getDefendingFaction().getFactionMembers()) {
            Player player = checkPlayer(uuid);
            if (player != null && player.isOnline() && raidInfoObject.getAttackedChunks().contains(player.getLocation().getChunk()) && defendingTitle != null && defendingSubTitle != null) {
                player.sendTitle(ChatColor.RED + defendingTitle, ChatColor.RED + defendingSubTitle);
            }
        }
    }

    public void UiHeadsUpDisplay(RaidInfoObject raidInfoObject) {
        ArrayList<Player> attackers = new ArrayList<>();
        // Get the affected players in the attackers team
        for (UUID playerUUID :  raidInfoObject.getAttackingFaction().getFactionMembers()) {
            Player player = checkPlayer(playerUUID);
            if (player != null) {
                if (raidInfoObject.getAttackedChunks().contains(player.getLocation().getChunk())) {
                    attackers.add(player);
                }
            }
        }

        ArrayList<Player> defenders = new ArrayList<>();
        // Get the affected players int the defenders team
        for (UUID playerUUID :  raidInfoObject.getDefendingFaction().getFactionMembers()) {
            Player player = checkPlayer(playerUUID);
            if (player != null) {
                if (raidInfoObject.getAttackedChunks().contains(player.getLocation().getChunk())) {
                    defenders.add(player);
                }
            }
        }

        // Show them the HUD
        for (Player player : attackers) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(
                    "Raid State: " + raidInfoObject.getRaidState() +
                    " -- Time Left: " + (raidInfoObject.getCurrentStateTimer() / 20) + "s" +
                    " -- " + attackers.size() + " vs " + defenders.size())
            );
        }

        for (Player player : defenders) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(
                    "Raid State: " + raidInfoObject.getRaidState() +
                    " -- Time Left: " + (raidInfoObject.getCurrentStateTimer() / 20) + "s" +
                    " -- " + defenders.size() + " vs " + attackers.size())
            );
        }
    }
}
