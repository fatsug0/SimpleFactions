package com.gus.simpleFactions.Commands.Faction.Sub;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.RaidHandlers.RaidInfoObject;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

public class FactionSubAdmin implements CommandInterface {

    private final SimpleFactions plugin;

    public FactionSubAdmin(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "admin";
    }

    @Override
    public String getDescription() {
        return """
                Staff command group for managing and auditing SimpleFactions.
                Use it to inspect factions, players, claims, and raids, or to force maintenance actions.
                Designed for developers and moderators who need server-wide faction control.""";
    }

    @Override
    public String getPermission() {
        return "simplefactions.admin";
    }

    @Override
    public String getUsage() {
        return "/faction admin <summary|factions|info|members|claims|inspect|claim|unclaim|power|disband|raids|save>";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>();
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Material.COMMAND_BLOCK);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Objects.requireNonNull(getPermission()))) {
            sender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You do not have permission to use this command!");
            return;
        }

        if (args.length < 2) {
            sendAdminHelp(sender);
            return;
        }

        switch (args[1].toLowerCase()) {
            case "summary" -> sendSummary(sender);
            case "factions" -> sendFactionList(sender);
            case "info" -> sendFactionInfo(sender, args);
            case "members" -> sendFactionMembers(sender, args);
            case "claims" -> sendFactionClaims(sender, args);
            case "inspect" -> inspect(sender, args);
            case "claim" -> forceClaim(sender, args);
            case "unclaim" -> forceUnclaim(sender, args);
            case "power" -> setPower(sender, args);
            case "disband" -> forceDisband(sender, args);
            case "raids" -> sendRaidOverview(sender);
            case "save" -> forceSave(sender);
            case "help" -> sendAdminHelp(sender);
            default -> sendAdminHelp(sender);
        }
    }

    private void sendAdminHelp(CommandSender sender) {
        sender.sendMessage(header("Admin Tools"));
        sender.sendMessage(line("/f admin summary", "Show faction, claim, raid, and online-player totals."));
        sender.sendMessage(line("/f admin factions", "List all factions with power, members, and claim counts."));
        sender.sendMessage(line("/f admin info <faction>", "Inspect one faction in detail."));
        sender.sendMessage(line("/f admin members <faction>", "List all members of one faction."));
        sender.sendMessage(line("/f admin claims <faction>", "List hard and weak chunks owned by a faction."));
        sender.sendMessage(line("/f admin inspect [player]", "Inspect a player or your current chunk."));
        sender.sendMessage(line("/f admin claim <faction> <x> <z> [hard|weak]", "Force-claim a chunk for a faction."));
        sender.sendMessage(line("/f admin unclaim <x> <z>", "Force-unclaim a chunk, no matter who owns it."));
        sender.sendMessage(line("/f admin power <faction> <amount>", "Set a faction's power."));
        sender.sendMessage(line("/f admin disband <faction> confirm", "Force-delete a faction."));
        sender.sendMessage(line("/f admin raids", "List waiting and active raids."));
        sender.sendMessage(line("/f admin save", "Force-save faction data."));
    }

    private void sendSummary(CommandSender sender) {
        int factions = plugin.factionManager.factionMembershipService.getExistingFactions().size();
        int linkedPlayers = plugin.factionManager.factionMembershipService.getPlayerFactionLink().size();
        int claims = plugin.factionManager.factionLandService.getLinkedChunks().size();
        int waitingRaids = countRaids(plugin.raidManager.getWaitingRaids());
        int activeRaids = countRaids(plugin.raidManager.getCurrentRaids());

        sender.sendMessage(header("Faction Summary"));
        sender.sendMessage(line("Factions", String.valueOf(factions)));
        sender.sendMessage(line("Linked players", String.valueOf(linkedPlayers)));
        sender.sendMessage(line("Claims", String.valueOf(claims)));
        sender.sendMessage(line("Waiting raids", String.valueOf(waitingRaids)));
        sender.sendMessage(line("Active raids", String.valueOf(activeRaids)));
        sender.sendMessage(line("Online players", String.valueOf(Bukkit.getOnlinePlayers().size())));
    }

    private void sendFactionList(CommandSender sender) {
        sender.sendMessage(header("Factions"));
        if (plugin.factionManager.factionMembershipService.getExistingFactions().isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "No factions exist.");
            return;
        }

        for (FactionObject faction : plugin.factionManager.factionMembershipService.getExistingFactions()) {
            sender.sendMessage(ChatColor.YELLOW + faction.getFactionName()
                    + ChatColor.GRAY + " | power " + ChatColor.WHITE + faction.getPower()
                    + ChatColor.GRAY + " | members " + ChatColor.WHITE + faction.getFactionMembers().size()
                    + ChatColor.GRAY + " | hard " + ChatColor.WHITE + faction.getHardClaimedChunks().size()
                    + ChatColor.GRAY + " | weak " + ChatColor.WHITE + faction.getWeakClaimedChunks().size());
        }
    }

    private void sendFactionInfo(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /f admin info <faction>");
            return;
        }

        FactionObject faction = findFaction(args[2]);
        if (faction == null) {
            sender.sendMessage(ChatColor.RED + "Faction not found: " + args[2]);
            return;
        }

        sender.sendMessage(header("Faction: " + faction.getFactionName()));
        sender.sendMessage(line("Owner", playerName(faction.getOwner()) + " (" + faction.getOwner() + ")"));
        sender.sendMessage(line("Power", String.valueOf(faction.getPower())));
        sender.sendMessage(line("Members", String.valueOf(faction.getFactionMembers().size())));
        sender.sendMessage(line("Hard claims", String.valueOf(faction.getHardClaimedChunks().size())));
        sender.sendMessage(line("Weak claims", String.valueOf(faction.getWeakClaimedChunks().size())));
        sender.sendMessage(line("Ranks", String.valueOf(faction.getFactionRanks().size())));
        sender.sendMessage(line("Home", faction.getFactionHome() == null ? "Not set" : formatLocation(faction.getFactionHome().getChunk())));
        sender.sendMessage(line("Prefix", faction.getTeamPrefix() == null ? "Not set" : faction.getTeamPrefix()));
    }

    private void sendFactionMembers(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /f admin members <faction>");
            return;
        }

        FactionObject faction = findFaction(args[2]);
        if (faction == null) {
            sender.sendMessage(ChatColor.RED + "Faction not found: " + args[2]);
            return;
        }

        sender.sendMessage(header("Members: " + faction.getFactionName()));
        for (UUID member : faction.getFactionMembers()) {
            sender.sendMessage(ChatColor.YELLOW + "- " + ChatColor.WHITE + playerName(member) + ChatColor.GRAY + " (" + member + ")");
        }
    }

    private void sendFactionClaims(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /f admin claims <faction>");
            return;
        }

        FactionObject faction = findFaction(args[2]);
        if (faction == null) {
            sender.sendMessage(ChatColor.RED + "Faction not found: " + args[2]);
            return;
        }

        sender.sendMessage(header("Claims: " + faction.getFactionName()));
        sender.sendMessage(line("Hard", formatChunks(faction.getHardClaimedChunks())));
        sender.sendMessage(line("Weak", formatChunks(faction.getWeakClaimedChunks())));
    }

    private void inspect(CommandSender sender, String[] args) {
        if (args.length == 2) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Console usage: /f admin inspect <player>");
                return;
            }
            inspectPlayer(sender, player);
            inspectChunk(sender, player.getLocation().getChunk());
            return;
        }

        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /f admin inspect [player]");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[2]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player must be online to inspect location: " + args[2]);
            return;
        }

        inspectPlayer(sender, target);
        inspectChunk(sender, target.getLocation().getChunk());
    }

    private void forceClaim(CommandSender sender, String[] args) {
        if (args.length < 5 || args.length > 6) {
            sender.sendMessage(ChatColor.RED + "Usage: /f admin claim <faction> <x> <z> [hard|weak]");
            return;
        }

        FactionObject faction = findFaction(args[2]);
        if (faction == null) {
            sender.sendMessage(ChatColor.RED + "Faction not found: " + args[2]);
            return;
        }

        Chunk chunk = parseChunk(sender, args[3], args[4]);
        if (chunk == null) return;

        boolean weak = args.length == 6 && args[5].equalsIgnoreCase("weak");
        if (args.length == 6 && !args[5].equalsIgnoreCase("weak") && !args[5].equalsIgnoreCase("hard")) {
            sender.sendMessage(ChatColor.RED + "Claim type must be hard or weak.");
            return;
        }

        FactionObject previousOwner = plugin.factionManager.factionLandService.getLinkedChunks().get(chunk);
        if (previousOwner != null) {
            removeClaim(previousOwner, chunk);
        }

        if (weak) {
            if (!faction.getWeakClaimedChunks().contains(chunk)) faction.getWeakClaimedChunks().add(chunk);
        } else {
            if (!faction.getHardClaimedChunks().contains(chunk)) faction.getHardClaimedChunks().add(chunk);
        }

        plugin.factionManager.factionLandService.addLinkedChunk(chunk, faction);
        redrawClaim(faction, chunk, weak);
        updatePlayersInChunk(chunk);
        sender.sendMessage(ChatColor.GREEN + "Forced " + (weak ? "weak" : "hard") + " claim " + formatLocation(chunk) + " for " + faction.getFactionName() + ".");
    }

    private void forceUnclaim(CommandSender sender, String[] args) {
        if (args.length != 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /f admin unclaim <x> <z>");
            return;
        }

        Chunk chunk = parseChunk(sender, args[2], args[3]);
        if (chunk == null) return;

        FactionObject owner = plugin.factionManager.factionLandService.getLinkedChunks().get(chunk);
        if (owner == null) {
            sender.sendMessage(ChatColor.RED + "That chunk is not claimed.");
            return;
        }

        removeClaim(owner, chunk);
        updatePlayersInChunk(chunk);
        sender.sendMessage(ChatColor.GREEN + "Unclaimed " + formatLocation(chunk) + " from " + owner.getFactionName() + ".");
    }

    private void setPower(CommandSender sender, String[] args) {
        if (args.length != 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /f admin power <faction> <amount>");
            return;
        }

        FactionObject faction = findFaction(args[2]);
        if (faction == null) {
            sender.sendMessage(ChatColor.RED + "Faction not found: " + args[2]);
            return;
        }

        Integer power = parseInt(args[3]);
        if (power == null || power < 0) {
            sender.sendMessage(ChatColor.RED + "Power must be a positive whole number.");
            return;
        }

        faction.setPower(power);
        sender.sendMessage(ChatColor.GREEN + "Set " + faction.getFactionName() + " power to " + power + ".");
    }

    private void forceDisband(CommandSender sender, String[] args) {
        if (args.length != 4 || !args[3].equalsIgnoreCase("confirm")) {
            sender.sendMessage(ChatColor.RED + "Usage: /f admin disband <faction> confirm");
            return;
        }

        FactionObject faction = findFaction(args[2]);
        if (faction == null) {
            sender.sendMessage(ChatColor.RED + "Faction not found: " + args[2]);
            return;
        }

        String factionName = faction.getFactionName();
        for (Chunk chunk : new ArrayList<>(faction.getHardClaimedChunks())) {
            removeClaim(faction, chunk);
        }
        for (Chunk chunk : new ArrayList<>(faction.getWeakClaimedChunks())) {
            removeClaim(faction, chunk);
        }

        Iterator<Map.Entry<UUID, FactionObject>> linkIterator = plugin.factionManager.factionMembershipService.getPlayerFactionLink().entrySet().iterator();
        while (linkIterator.hasNext()) {
            if (linkIterator.next().getValue().equals(faction)) {
                linkIterator.remove();
            }
        }

        Team team = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard()
                .getTeam("faction_" + plugin.factionManager.factionFormatterService.toTeamName(factionName));
        if (team != null) {
            team.unregister();
        }

        plugin.factionManager.factionMembershipService.removeExistingFaction(faction);
        sender.sendMessage(ChatColor.GREEN + "Force-disbanded faction " + factionName + ".");
    }

    private void sendRaidOverview(CommandSender sender) {
        sender.sendMessage(header("Raids"));
        sendRaidMap(sender, "Waiting", plugin.raidManager.getWaitingRaids());
        sendRaidMap(sender, "Active", plugin.raidManager.getCurrentRaids());
    }

    private void forceSave(CommandSender sender) {
        plugin.jsonHandler.SaveSequence();
        sender.sendMessage(ChatColor.GREEN + "Faction data saved.");
    }

    private void inspectPlayer(CommandSender sender, Player player) {
        FactionObject faction = plugin.factionManager.factionMembershipService.getPlayerFactionLink().get(player.getUniqueId());
        sender.sendMessage(header("Player: " + player.getName()));
        sender.sendMessage(line("UUID", player.getUniqueId().toString()));
        sender.sendMessage(line("Faction", faction == null ? "None" : faction.getFactionName()));
        sender.sendMessage(line("World", player.getWorld().getName()));
        sender.sendMessage(line("Chunk", formatLocation(player.getLocation().getChunk())));
    }

    private void inspectChunk(CommandSender sender, Chunk chunk) {
        FactionObject owner = plugin.factionManager.factionLandService.getLinkedChunks().get(chunk);
        String claimType = "Wilderness";
        if (owner != null) {
            claimType = owner.getHardClaimedChunks().contains(chunk) ? "Hard claim" : "Weak claim";
        }

        sender.sendMessage(header("Chunk: " + formatLocation(chunk)));
        sender.sendMessage(line("Owner", owner == null ? "None" : owner.getFactionName()));
        sender.sendMessage(line("State", claimType));
        sender.sendMessage(line("Players inside", String.valueOf(chunk.getPlayersSeeingChunk().size())));
    }

    private void sendRaidMap(CommandSender sender, String label, HashMap<FactionObject, ArrayList<RaidInfoObject>> raids) {
        int raidCount = countRaids(raids);
        sender.sendMessage(ChatColor.YELLOW + label + ChatColor.GRAY + " raids: " + ChatColor.WHITE + raidCount);
        if (raidCount == 0) return;

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy:HHmm");
        for (Map.Entry<FactionObject, ArrayList<RaidInfoObject>> entry : raids.entrySet()) {
            FactionObject defendingFaction = entry.getKey();
            for (RaidInfoObject raid : entry.getValue()) {
                sender.sendMessage(ChatColor.GRAY + "- defending " + ChatColor.WHITE + defendingFaction.getFactionName()
                        + ChatColor.GRAY + " vs " + ChatColor.WHITE + raid.getAttackingFaction().getFactionName()
                        + ChatColor.GRAY + " | state " + ChatColor.WHITE + raid.getRaidState()
                        + ChatColor.GRAY + " | date " + ChatColor.WHITE + dateFormat.format(raid.getRaidDate().getTime())
                        + ChatColor.GRAY + " | chunks " + ChatColor.WHITE + raid.getAttackedChunks().size());
            }
        }
    }

    private FactionObject findFaction(String factionName) {
        Optional<FactionObject> faction = plugin.factionManager.factionMembershipService.getExistingFactions().stream()
                .filter(existingFaction -> existingFaction.getFactionName().equalsIgnoreCase(factionName))
                .findFirst();
        return faction.orElse(null);
    }

    private Chunk parseChunk(CommandSender sender, String xArg, String zArg) {
        Integer x = parseInt(xArg);
        Integer z = parseInt(zArg);
        if (x == null || z == null) {
            sender.sendMessage(ChatColor.RED + "Chunk coordinates must be whole numbers.");
            return null;
        }

        World world = Bukkit.getWorld("world");
        if (world == null) {
            sender.sendMessage(ChatColor.RED + "World 'world' is not loaded.");
            return null;
        }

        return world.getChunkAt(x, z);
    }

    private Integer parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void removeClaim(FactionObject faction, Chunk chunk) {
        faction.removeHardClaimedChunks(chunk);
        faction.removeWeakClaimedChunks(chunk);
        plugin.factionManager.factionLandService.removeLinkedChunk(chunk);

        if (plugin.factionManager.factionMapRenderService.getUSE_BLUEMAP_ADDON()
                && plugin.factionManager.factionMapRenderService.getBluemapClaimedChunk().containsKey(chunk)) {
            plugin.factionManager.factionMapRenderService.RemoveChunks(faction, chunk);
        }
    }

    private void redrawClaim(FactionObject faction, Chunk chunk, boolean weak) {
        if (!plugin.factionManager.factionMapRenderService.getUSE_BLUEMAP_ADDON()) return;
        plugin.factionManager.factionMapRenderService.DrawChunks(faction, chunk, weak);
    }

    private void updatePlayersInChunk(Chunk chunk) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getLocation().getChunk().equals(chunk)) {
                plugin.factionManager.factionHelperService.updatePlayerChunkState(onlinePlayer.getUniqueId(), chunk);
            }
        }
    }

    private int countRaids(HashMap<FactionObject, ArrayList<RaidInfoObject>> raids) {
        int count = 0;
        for (ArrayList<RaidInfoObject> factionRaids : raids.values()) {
            count += factionRaids.size();
        }
        return count;
    }

    private String formatChunks(ArrayList<Chunk> chunks) {
        if (chunks.isEmpty()) return "None";
        StringJoiner joiner = new StringJoiner(ChatColor.GRAY + ", " + ChatColor.WHITE);
        for (Chunk chunk : chunks) {
            joiner.add(chunk.getX() + "," + chunk.getZ());
        }
        return joiner.toString();
    }

    private String formatLocation(Chunk chunk) {
        return chunk.getWorld().getName() + " " + chunk.getX() + "," + chunk.getZ();
    }

    private String playerName(UUID playerUUID) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
        return player.getName() == null ? "Unknown" : player.getName();
    }

    private String header(String title) {
        return ChatColor.GOLD.toString() + ChatColor.BOLD + title + ChatColor.RESET + "\n"
                + ChatColor.DARK_GRAY + "--------------------------------";
    }

    private String line(String label, String value) {
        return ChatColor.YELLOW + label + ChatColor.GRAY + ": " + ChatColor.WHITE + value;
    }
}
