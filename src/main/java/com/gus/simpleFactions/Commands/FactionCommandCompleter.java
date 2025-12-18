package com.gus.simpleFactions.Commands;

import com.gus.simpleFactions.FactionObject;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class FactionCommandCompleter implements TabCompleter {

    private SimpleFactions plugin;
    public FactionCommandCompleter(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String [] strings) {
        if (commandSender instanceof Player player) {
            FactionObject playerFaction = plugin.factionManager.playerFactionLink == null ? null : plugin.factionManager.playerFactionLink.get(player.getUniqueId());

            switch (strings.length) {

                case 0:
                    commandSender.sendMessage("§4Usage: /faction help");
                    break;

                case 1:
                    if (playerFaction == null) {
                        if (plugin.factionManager.pendingFactionInvites == null || plugin.factionManager.PlayerInvitations(player.getUniqueId()) == null){
                            return StringUtil.copyPartialMatches(strings[0], List.of("create", "help"), new ArrayList<>());
                        }
                        return StringUtil.copyPartialMatches(strings[0], List.of("create", "join", "help"), new ArrayList<>());
                    } else  {
                        return StringUtil.copyPartialMatches(strings[0], List.of("invite", "kick", "home", "claim", "unclaim", "leave", "disband", "help"), new ArrayList<>());
                    }

                case  2:
                    switch (strings[0]) {
                        case "invite":
                            ArrayList<String> returnArray0 = new ArrayList<>();
                            for (Player onlinePlayer : Bukkit.getOnlinePlayers()){
                                if (onlinePlayer.getName().equals(player.getName())){
                                    break;
                                }
                                if (plugin.factionManager.playerFactionLink.get(player.getUniqueId()).getFactionMembers().contains(onlinePlayer)){
                                    break;
                                }
                                returnArray0.add(onlinePlayer.getName());
                            }

                            return StringUtil.copyPartialMatches(strings[1], returnArray0, new ArrayList<>());

                        case "kick":
                            ArrayList<String> returnArray1 = new ArrayList<>();
                            for (UUID playerInFaction : plugin.factionManager.playerFactionLink.get(player.getUniqueId()).getFactionMembers()){
                                if (Objects.requireNonNull(Bukkit.getPlayer(playerInFaction)).getName().equals(player.getName())){
                                    break;
                                }
                                returnArray1.add(Objects.requireNonNull(Bukkit.getPlayer(playerInFaction)).getName());
                            }
                            return StringUtil.copyPartialMatches(strings[1], returnArray1, new ArrayList<>());

                        case "home":
                            return StringUtil.copyPartialMatches(strings[1], List.of("set"), new ArrayList<>());

                        case "leave":
                            return StringUtil.copyPartialMatches(strings[1], List.of("confirm"), new ArrayList<>());

                        case "join":
                            if (plugin.factionManager.PlayerInvitations(player.getUniqueId()) == null){
                                return StringUtil.copyPartialMatches(strings[1], List.of(), new ArrayList<>());
                            }
                            return StringUtil.copyPartialMatches(strings[1], plugin.factionManager.PlayerInvitations(player.getUniqueId()), new ArrayList<>());
                    }

            }
        }
        return List.of();
    }
}
