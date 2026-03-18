package com.gus.simpleFactions.FactionHandlers.FactionObjectServices;

import com.gus.simpleFactions.FactionHandlers.FactionRankObject;
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

    public String SendFactionInfo(UUID playerUUID) {
        Player player = checkPlayer(playerUUID);
        if (player == null) {
            System.out.println("Something went wrong when trying to find the player with the UUID: " + playerUUID);
            return ChatColor.RED + "Something went wrong when trying to find the player with the UUID: " + playerUUID;
        }

        // Enhanced with colors, styles, and more details

        return "§e§l=== Faction Info: §a§l" + this.getFactionName() + " §e§l===\n" +
                "§7Owner: §b§l" + Objects.requireNonNull(Bukkit.getPlayer(this.getOwner())).getName() + "\n" +
                "§7Members: §a" + this.getFactionMembers().size() + " §7(total)\n" +
                "§7Power: §6" + this.getPower() + " §7/ Max Weak Chunks: §c" + (int) this.getMaxWeakChunks() + "\n" +
                "§7Claimed Chunks: §2" + this.getClaimedChunks().size() + " §7(Strong) + §4" + this.getWeakChunks().size() + " §7(Weak)\n" +
                "§7Home: §d" + (this.getFactionHome() != null ? "Set at " + this.getFactionHome().getBlockX() + ", " + this.getFactionHome().getBlockY() + ", " + this.getFactionHome().getBlockZ() : "Not set") + "\n" +
                "§e§l=======================";
    }

    public String getAllRankInfo(){
        if (existingFactionRanks.isEmpty()) {
            return "§cNo ranks exist in this faction.";
        }

        StringBuilder info = new StringBuilder();
        info.append("§e§l=== All Ranks in §a§l").append(this.getFactionName()).append(" §e§l===\n");
        for (FactionRankObject rank : existingFactionRanks) {
            int memberCount = 0;
            for (FactionRankObject playerRank : factionRanks.values()) {
                if (playerRank.getRankName().equals(rank.getRankName())) {
                    memberCount++;
                }
            }
            info.append("§b§l").append(rank.getRankName()).append("§7: §a").append(memberCount).append(" members, §6").append(rank.getPermissions().size()).append(" permissions\n");
        }
        info.append("§e§l=======================");

        return info.toString();
    }

    public String getRankInfo(String rankName){
        FactionRankObject targetRank = null;
        for (FactionRankObject rank : existingFactionRanks) {
            if (rank.getRankName().equalsIgnoreCase(rankName)) {
                targetRank = rank;
                break;
            }
        }
        if (targetRank == null) {
            return "§cRank '" + rankName + "' does not exist.";
        }

        int memberCount = 0;
        for (FactionRankObject playerRank : factionRanks.values()) {
            if (playerRank.getRankName().equals(rankName)) {
                memberCount++;
            }
        }

        return "§e§l=== Rank Info: §b§l" + rankName + " §e§l===\n" +
                "§7Members: §a" + memberCount + "\n" +
                "§7Permissions: §6" + targetRank.getPermissions().size() + "\n" +
                "§e§l=======================";
    }

    public String SendRankPlayerInfo(String rankName){
        FactionRankObject targetRank = null;
        for (FactionRankObject rank : existingFactionRanks) {
            if (rank.getRankName().equalsIgnoreCase(rankName)) {
                targetRank = rank;
                break;
            }
        }
        if (targetRank == null) {
            return "§cRank '" + rankName + "' does not exist.";
        }

        StringBuilder info = new StringBuilder();
        info.append("§e§l=== Players in Rank: §b§l").append(rankName).append(" §e§l===\n");
        boolean hasMembers = false;
        for (Map.Entry<UUID, FactionRankObject> entry : factionRanks.entrySet()) {
            if (entry.getValue().getRankName().equals(rankName)) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null) {
                    info.append("§a- ").append(player.getName()).append("\n");
                    hasMembers = true;
                }
            }
        }
        if (!hasMembers) {
            info.append("§7No players in this rank.\n");
        }
        info.append("§e§l=======================");

        return info.toString();
    }

    public String SendRankPermissionsInfo(String rankName){
        FactionRankObject targetRank = null;
        for (FactionRankObject rank : existingFactionRanks) {
            if (rank.getRankName().equalsIgnoreCase(rankName)) {
                targetRank = rank;
                break;
            }
        }
        if (targetRank == null) {
            return "§cRank '" + rankName + "' does not exist.";
        }

        StringBuilder info = new StringBuilder();
        info.append("§e§l=== Permissions for Rank: §b§l").append(rankName).append(" §e§l===\n");
        if (targetRank.getPermissions().isEmpty()) {
            info.append("§7No permissions assigned.\n");
        } else {
            for (String perm : targetRank.getPermissions()) {
                info.append("§6- ").append(perm).append("\n");
            }
        }
        info.append("§e§l=======================");

        return info.toString();
    }

    private String toTeamName(String factionName) {
        // This is used to ensure the right format in the team naming (basic characters and 16 characters limit)
        String base = "f_" + factionName.toLowerCase().replaceAll("[^a-z0-9_]", "");
        return base.substring(0, Math.min(16, base.length()));
    }

    private String useLegacyText(String text){
        final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
                .character('§')
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .build();

        Component parsed = Component.text(text)
                .color(TextColor.color(100, 100, 100));
        return LEGACY.serialize(parsed);
    }

    private String useMiniMessage(String text, ArrayList<String> colors){
        final MiniMessage MM = MiniMessage.miniMessage();
        final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
                .character('§')
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .build();

        Component parsed;
        if (colors.size() == 1) {
            parsed = MM.deserialize("<shadow:#000000FF><b><color:#" + colors.getFirst() + ">" + text + "</color>");
        } else {
            StringBuilder colorCode = new StringBuilder();
            for (String color : colors) colorCode.append(":#").append(color);
            parsed = MM.deserialize("<shadow:#000000FF><b><gradient" + colorCode + ">" + text + "</gradient>");
        }
        return LEGACY.serialize(parsed);
    }

    public void setTeamPrefix(String prefixName, ArrayList<String> colors){
        Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
        Team team = scoreboard.getTeam(toTeamName(this.getFactionName()));
        if (team == null) return;
        team.setPrefix(useMiniMessage(" [" + prefixName + "] ", colors));
    }
}
