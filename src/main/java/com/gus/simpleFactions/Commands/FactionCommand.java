package com.gus.simpleFactions.Commands;

import com.gus.simpleFactions.FactionManager;
import com.gus.simpleFactions.FactionObject;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FactionCommand implements CommandExecutor {

    private SimpleFactions plugin;
    public FactionCommand(JavaPlugin plugin) {
        this.plugin = (SimpleFactions) plugin;
    }

    private NamespacedKey hasFactionKey;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String [] strings) {
        if (commandSender instanceof Player player) {
            FactionObject playerFaction = plugin.factionManager.playerFactionLink == null ? null : plugin.factionManager.playerFactionLink.get(player.getUniqueId());
            FactionManager factionHandler = plugin.factionManager;

            if (playerFaction == null) {
                switch (strings[0]) {

                    case "create":
                        if (!player.hasPermission("factions.create")) break;
                        if (strings.length == 2) {
                            factionHandler.CreateFaction(player.getUniqueId(), strings[1]);
                        } else {
                            player.sendMessage("§4Wrong use of command, use /f help !");
                        }
                        break;

                    case "join":
                        if (!player.hasPermission("factions.join")) break;
                        if (strings.length == 2) {
                            factionHandler.JoinFaction(player.getUniqueId(), strings[1]);
                        } else {
                            player.sendMessage("§4Wrong use of command, use /f help !");
                        }
                        break;

                    default:
                        player.sendMessage("§4Wrong use of command, use /f help !");
                        break;
                }

            } else {
                switch (strings[0]) {
                    case "invite":
                        if (!player.hasPermission("factions.invite")) break;
                        if (strings.length == 2) {
                            playerFaction.InvitePlayer(strings[1]);
                        } else {
                            player.sendMessage("§4Wrong use of command, use /f help !");
                        }
                        break;

                    case "kick":
                        if (!player.hasPermission("factions.kick")) break;
                        if (strings.length == 2) {
                            playerFaction.KickPlayer(player.getUniqueId());
                        } else {
                            player.sendMessage("§4Wrong use of command, use /f help !");
                        }
                        break;

                    case "home":
                        if (!player.hasPermission("factions.home")) break;
                        if (strings.length == 1) {
                            playerFaction.TeleportHome(player.getUniqueId());
                        } else if (strings.length == 2) {
                            if (!player.hasPermission("factions.sethome")) break;
                            playerFaction.SetHome(player.getUniqueId());
                        } else {
                            player.sendMessage("§4Wrong use of command, use /f help !");
                        }
                        break;

                    case "claim":
                        if (!player.hasPermission("factions.claim")) break;
                        if (strings.length == 1) {
                            playerFaction.ClaimLand(player.getUniqueId());
                        } else {
                            player.sendMessage("§4Wrong use of command, use /f help !");
                        }
                        break;

                    case "unclaim":
                        if (!player.hasPermission("factions.unclaim")) break;
                        if (strings.length == 1) {
                            playerFaction.UnClaimLand(player.getUniqueId());
                        } else {
                            player.sendMessage("§4Wrong use of command, use /f help !");
                        }
                        break;

                    case "info":
                        if (!player.hasPermission("factions.info")) break;
                        if (strings.length == 1) {
                            playerFaction.SendFactionInfo(player.getUniqueId());
                        } else {
                            player.sendMessage("§4Wrong use of command, use /f help !");
                        }
                        break;

                    case "leave":
                        if (!player.hasPermission("factions.leave")) break;
                        if (strings.length == 2 && strings[1].equals("confirm")) {
                            playerFaction.LeaveFaction(player.getUniqueId());
                        } else {
                            player.sendMessage("§4Write /f leave confirm to leave your faction!");
                        }
                        break;

                    case "disband":
                        if (!player.hasPermission("factions.disband")) break;
                        if (strings.length == 2 && strings[1].equals("confirm")) {
                            playerFaction.DisbandFaction(player.getUniqueId());
                        } else {
                            player.sendMessage("§4Write /f leave confirm to leave your faction!");
                        }
                        break;

                    case "help":
                        if (!player.hasPermission("factions.help")) break;
                        if (strings.length == 1){
                            factionHandler.SendHelp(player.getUniqueId());
                        } else {
                            player.sendMessage("§4Wrong use of command, use /f help !");
                        }
                        break;

                    default:
                        player.sendMessage("§4Wrong use of command, use /f help !");
                        break;
                }
            }
        }
        return false;
    }
}
