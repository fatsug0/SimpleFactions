package com.gus.simpleFactions.Commands.Faction;

import com.gus.simpleFactions.Commands.Builders.CommandHandler;
import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.Commands.Faction.Sub.*;
import com.gus.simpleFactions.Commands.Faction.Sub.Home.FactionSubHome;
import com.gus.simpleFactions.Commands.Faction.Sub.Rank.FactionSubRank;
import com.gus.simpleFactions.FactionHandlers.FactionObject;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class FactionCommand extends CommandHandler implements CommandInterface {

    private final SimpleFactions plugin;

    public FactionCommand(SimpleFactions plugin) {
        super(
                "faction",
                new String[]{"f", "fac"},
                """
                        This is the faction command
                        This is the main command for factions
                        You can claim land, invite players, create ranks, etc""",
                null,
                "/faction <options>"
        );
        this.plugin = plugin;
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>(){{
            put("home", new FactionSubHome(plugin));
            put("rank", new FactionSubRank(plugin));
            put("claim", new FactionSubClaim(plugin));
            put("create", new FactionSubCreate(plugin));
            put("disband", new FactionSubDisband(plugin));
            put("help", new FactionSubHelp(plugin));
            put("info", new FactionSubInfo(plugin));
            put("invite", new FactionSubInvite(plugin));
            put("join", new FactionSubJoin(plugin));
            put("kick", new FactionSubKick(plugin));
            put("leave", new FactionSubLeave(plugin));
            put("unclaim", new FactionSubUnclaim(plugin));
            put("prefix", new FactionSubPrefix(plugin));
            put("raid", new FactionSubRaid(plugin));
            put("toggle", new FactionSubToggle(plugin));
            put("storage", new FactionSubStorage(plugin));
            put("admin", new  FactionSubAdmin(plugin));
        }};
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Get the last argument and execute the command linked to it
        // Need to get the last argument subcommand and return them
        // Last argument -> args[args.length - 1]
        if (args.length == 0 || !getSubCommands().containsKey(args[0])) {
            sender.sendMessage(sendUsageError());
            return;
        }

        CommandInterface command = this;
        for (String arg : args) {
            if (command.getSubCommands().containsKey(arg)) {
                command = command.getSubCommands().get(arg);
            }
        }
        command.execute(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();

        // Same as the execute command logic, but return the tab completions
        CommandInterface command = this;
        for (String arg : args) {
            if (command.getSubCommands().containsKey(arg)) {
                command = command.getSubCommands().get(arg);
            }
        }

        //region Special cases for onTabComplete
        switch (command.getName()) {
            case "invite":
                ArrayList<Player> invitePlayers = new ArrayList<>(plugin.getServer().getOnlinePlayers());

                // Get all online players and offline players
                for (OfflinePlayer offlinePlayer : plugin.getServer().getOfflinePlayers()) {
                    invitePlayers.add(offlinePlayer.getPlayer());
                }

                // Remove the sender from the list
                invitePlayers.remove(player);


                // Remove all players already in the faction
                for (UUID factionPlayerUUID : plugin.factionManager.playerFactionLink.get(player.getUniqueId()).getFactionMembers()){
                    if (checkPlayer(factionPlayerUUID) != null) invitePlayers.remove(checkPlayer(factionPlayerUUID));
                }

                // Transform all players into strings (their names)
                ArrayList<String> invitePlayersNames = new ArrayList<>();
                for (Player invitePlayer : invitePlayers) {
                    invitePlayersNames.add(invitePlayer.getName());
                }

                return invitePlayersNames;

            case "kick":

                // Get all players in the faction
                ArrayList<UUID> kickPlayers = new ArrayList<>(plugin.factionManager.playerFactionLink.get(player.getUniqueId()).getFactionMembers());

                // Remove the sender from the list
                kickPlayers.remove(player.getUniqueId());

                // Transform all players into strings (their names)
                ArrayList<String> kickPlayersNames = new ArrayList<>();
                for (UUID kickPlayer : kickPlayers) {
                    if (checkPlayer(kickPlayer) != null) kickPlayersNames.add(checkPlayer(kickPlayer).getName());
                }

                return kickPlayersNames;

            case "add":
                if (command.getPermission().equalsIgnoreCase("simplefactions.rank.manage.player.add")){
                    // Get all players in the faction
                    FactionObject playerFaction = plugin.factionManager.playerFactionLink.get(player.getUniqueId());
                    ArrayList<UUID> factionPlayers = new ArrayList<>(playerFaction.getFactionMembers());

                    // Remove all players already in the rank
                    for (UUID factionPlayerUUID : factionPlayers){
                        if (playerFaction.factionRanks.get(factionPlayerUUID).getRankName().equalsIgnoreCase(args[args.length - 2])) {
                            factionPlayers.remove(factionPlayerUUID);
                        }
                    }

                    // Transform all players into strings (their names)
                    ArrayList<String> factionPlayersNames = new ArrayList<>();
                    for (UUID factionPlayer : factionPlayers) {
                        if (checkPlayer(factionPlayer) != null) factionPlayersNames.add(checkPlayer(factionPlayer).getName());
                    }

                    return factionPlayersNames;

                } else if (command.getPermission().equalsIgnoreCase("simplefactions.rank.manage.permissions.add")){
                    // Get all permissions

                    // Remove all permissions already in the rank
                } else {
                    return new ArrayList<>(command.getSubCommands().keySet());
                }

            case "remove":
                if (command.getPermission().equalsIgnoreCase("simplefactions.rank.manage.player.remove")){
                    // Get all players in the rank

                    // Transform all players into strings (their names)
                } else if (command.getPermission().equalsIgnoreCase("simplefactions.rank.manage.permissions.remove")){
                    // Get all permissions in the rank
                } else {
                    return new ArrayList<>(command.getSubCommands().keySet());
                }

            case "toggle":
                switch (args.length) {
                    case 2:
                        if (command.getPermission().equalsIgnoreCase("simplefactions.toggle")){
                            ArrayList<String> factionNames = new ArrayList<>();
                            for (FactionObject faction : plugin.factionManager.existingFactions) {
                                factionNames.add(faction.getFactionName());
                            }
                            return factionNames;
                        }

                    case 3:
                        return Arrays.asList("hard", "weak", "all");

                    case 4:
                        return Arrays.asList("enable", "disable");
                }
                }

        //endregion

        return new ArrayList<>(command.getSubCommands().keySet());
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
}
