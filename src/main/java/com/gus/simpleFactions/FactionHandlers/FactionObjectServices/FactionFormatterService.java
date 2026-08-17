package com.gus.simpleFactions.FactionHandlers.FactionObjectServices;

import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionRankObject;
import com.gus.simpleFactions.SimpleFactions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class FactionFormatterService {

    private final SimpleFactions plugin;
    public FactionFormatterService(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    public String SendFactionInfo(FactionObject faction, UUID playerUUID) {
        Player player = plugin.factionManager.factionHelperService.checkPlayer(playerUUID);
        if (player == null) {
            return ChatColor.RED + "Something went wrong when trying to find the player with the UUID: " + playerUUID;
        }

        // Enhanced with colors, styles, and more details

        // The owner may well be offline when /faction info is run - Bukkit.getPlayer() only
        // resolves online players, so look the name up through the offline-player store instead.
        String ownerName = Bukkit.getOfflinePlayer(faction.getOwner()).getName();
        if (ownerName == null) ownerName = faction.getOwner().toString();

        return ChatColor.YELLOW.toString() + ChatColor.BOLD + "=== Faction Info: " + ChatColor.GREEN + ChatColor.BOLD + faction.getFactionName() + " " + ChatColor.YELLOW + ChatColor.BOLD + "===\n" +
                ChatColor.GRAY + "Owner: " + ChatColor.AQUA + ChatColor.BOLD + ownerName + "\n" +
                ChatColor.GRAY + "Members: " + ChatColor.GREEN + faction.getFactionMembers().size() + " " + ChatColor.GRAY + "(total)\n" +
                ChatColor.GRAY + "Power: " + ChatColor.GOLD + faction.getPower() + " " + ChatColor.GRAY + "/ Max Weak Chunks: " + ChatColor.RED + plugin.factionManager.factionLandService.getMAX_WEAK_CHUNKS(faction) + "\n" +
                ChatColor.GRAY + "Claimed Chunks: " + ChatColor.DARK_GREEN + faction.getHardClaimedChunks().size() + " " + ChatColor.GRAY + "(Strong) + " + ChatColor.DARK_RED + faction.getWeakClaimedChunks().size() + " " + ChatColor.GRAY + "(Weak)\n" +
                ChatColor.GRAY + "Home: " + ChatColor.LIGHT_PURPLE + (faction.getFactionHome() != null ? "Set at " + faction.getFactionHome().getBlockX() + ", " + faction.getFactionHome().getBlockY() + ", " + faction.getFactionHome().getBlockZ() : "Not set") + "\n" +
                ChatColor.YELLOW + ChatColor.BOLD + "=======================";
    }

    public String getAllRankInfo(FactionObject faction){
        if (faction.getFactionRanks().isEmpty()) {
            return ChatColor.RED + "No ranks exist in this faction.";
        }

        StringBuilder info = new StringBuilder();
        info.append(ChatColor.YELLOW).append(ChatColor.BOLD).append("=== All Ranks in ").append(ChatColor.GREEN).append(ChatColor.BOLD).append(faction.getFactionName()).append(ChatColor.YELLOW).append(ChatColor.BOLD).append(" ===\n");
        for (FactionRankObject rank : faction.getFactionRanks()) {
            int memberCount = rank.getRankMembers().size();
            info.append(ChatColor.AQUA).append(ChatColor.BOLD).append(rank.getRankName()).append(ChatColor.GRAY).append(": ").append(ChatColor.GREEN).append(memberCount).append(" members, ").append(ChatColor.GOLD).append(rank.getPermissions().size()).append(" permissions\n");
        }
        info.append(ChatColor.YELLOW).append(ChatColor.BOLD).append("=======================");

        return info.toString();
    }

    public String getRankInfo(FactionObject faction, String rankName){
        FactionRankObject targetRank = null;
        for (FactionRankObject rank : faction.getFactionRanks()) {
            if (rank.getRankName().equalsIgnoreCase(rankName)) {
                targetRank = rank;
                break;
            }
        }
        if (targetRank == null) {
            return ChatColor.RED + "Rank '" + rankName + "' does not exist.";
        }

        int memberCount = targetRank.getRankMembers().size();

        return ChatColor.YELLOW.toString() + ChatColor.BOLD + "=== Rank Info: " + ChatColor.AQUA + ChatColor.BOLD + rankName + " " + ChatColor.YELLOW + ChatColor.BOLD + "===\n" +
                ChatColor.GRAY + "Members: " + ChatColor.GREEN + memberCount + "\n" +
                ChatColor.GRAY + "Permissions: " + ChatColor.GOLD + targetRank.getPermissions().size() + "\n" +
                ChatColor.YELLOW + ChatColor.BOLD + "=======================";
    }

    public String SendRankPlayerInfo(FactionObject faction, String rankName){
        FactionRankObject targetRank = null;
        for (FactionRankObject rank : faction.getFactionRanks()) {
            if (rank.getRankName().equalsIgnoreCase(rankName)) {
                targetRank = rank;
                break;
            }
        }
        if (targetRank == null) {
            return ChatColor.RED + "Rank '" + rankName + "' does not exist.";
        }

        StringBuilder info = new StringBuilder();
        info.append(ChatColor.YELLOW).append(ChatColor.BOLD).append("=== Players in Rank: ").append(ChatColor.AQUA).append(ChatColor.BOLD).append(rankName).append(ChatColor.YELLOW).append(ChatColor.BOLD).append(" ===\n");
        boolean hasMembers = false;
        for (FactionRankObject rank : faction.getFactionRanks()) {
            if (rank.getRankName().equals(rankName)) {
                for (UUID playerUUID : rank.getRankMembers()) {
                    Player player = Bukkit.getPlayer(playerUUID);
                    if (player != null) {
                        info.append(ChatColor.GREEN).append("- ").append(player.getName()).append("\n");
                        hasMembers = true;
                    }
                }
            }
        }
        if (!hasMembers) {
            info.append(ChatColor.GRAY).append("No players in this rank.\n");
        }
        info.append(ChatColor.YELLOW).append(ChatColor.BOLD).append("=======================");

        return info.toString();
    }

    public String SendRankPermissionsInfo(FactionObject faction, String rankName){
        FactionRankObject targetRank = null;
        for (FactionRankObject rank : faction.getFactionRanks()) {
            if (rank.getRankName().equalsIgnoreCase(rankName)) {
                targetRank = rank;
                break;
            }
        }
        if (targetRank == null) {
            return ChatColor.RED + "Rank '" + rankName + "' does not exist.";
        }

        StringBuilder info = new StringBuilder();
        info.append(ChatColor.YELLOW).append(ChatColor.BOLD).append("=== Permissions for Rank: ").append(ChatColor.AQUA).append(ChatColor.BOLD).append(rankName).append(ChatColor.YELLOW).append(ChatColor.BOLD).append(" ===\n");
        if (targetRank.getPermissions().isEmpty()) {
            info.append(ChatColor.GRAY).append("No permissions assigned.\n");
        } else {
            for (String perm : targetRank.getPermissions()) {
                info.append(ChatColor.GOLD).append("- ").append(perm).append("\n");
            }
        }
        info.append(ChatColor.YELLOW).append(ChatColor.BOLD).append("=======================");

        return info.toString();
    }

    public void SendHelp(UUID player){
        // Here we explain the use of the faction command
        Objects.requireNonNull(Bukkit.getPlayer(player)).sendMessage(
                "The Faction command use (faction / fac / f):\n" +
                        "/faction create <factionName> : Create a new faction\n>" +
                        "/faction join <factionName> : Join an existing faction\n>" +
                        "/faction invite <playerName> : Invite a player to join your faction\n>" +
                        "/faction kick <playerName> : Kick a player from your faction\n>" +
                        "/faction home [set] : Teleport to your faction home, use the set option to set it\n>" +
                        "/faction claim : Claim the chunk you currently are\n" +
                        "/faction unclaim : Unclaim the chunk you currently are\n" +
                        "/faction info : Get all the important info on your faction\n" +
                        "/faction leave <confirm> : Leave your faction, use the confirm option to confirm the action\n" +
                        "/faction disband <confirm> : Disband your faction, use the confirm option to confirm the action\n" +
                        "/faction disband : Disband your faction\n"
        );
    }

    public String toTeamName(String factionName) {
        // This is used to ensure the right format in the team naming (basic characters and 16 characters limit)
        String base = "f_" + factionName.toLowerCase().replaceAll("[^a-z0-9_]", "");
        return base.substring(0, Math.min(16, base.length()));
    }

    /**
     * The scoreboard team is always registered under "faction_" + toTeamName(...) (see
     * FactionHelperService#createTabTeam). Every lookup must use this same full name, or
     * scoreboard.getTeam(...) silently returns null.
     */
    public String toFullTeamName(String factionName) {
        return "faction_" + toTeamName(factionName);
    }

    public String useLegacyText(String text){
        final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
                .character('§')
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .build();

        Component parsed = Component.text(text)
                .color(TextColor.color(100, 100, 100));
        return LEGACY.serialize(parsed);
    }

    public String useMiniMessage(String text, ArrayList<String> colors){
        final MiniMessage MM = MiniMessage.miniMessage();
        final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
                .character('§')
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .build();

        Component parsed;
        if (colors.size() == 1) {
            parsed = MM.deserialize("<shadow:#000000FF><b><color:" + colors.getFirst() + ">" + text + "</color>");
        } else {
            StringBuilder colorCode = new StringBuilder();
            for (String color : colors) colorCode.append(":#").append(color);
            parsed = MM.deserialize("<shadow:#000000FF><b><gradient" + colorCode + ">" + text + "</gradient>");
        }
        return LEGACY.serialize(parsed);
    }

    public void setTeamPrefix(FactionObject faction, String prefixName, ArrayList<String> colors){
        Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
        Team team = scoreboard.getTeam(toFullTeamName(faction.getFactionName()));
        if (team == null) return;
        team.setPrefix(useMiniMessage(" [" + prefixName + "] ", colors));

        // Keep the faction's stored color in sync with what's actually shown in the tab list -
        // this is also what colors the faction's BlueMap claim markers (FactionMapRenderService).
        faction.setFactionColors(colors);
    }
}
