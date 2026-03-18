package com.gus.simpleFactions.Commands.Faction.Sub;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.FactionHandlers.FactionObject;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class FactionSubRaid implements CommandInterface {

    private final SimpleFactions plugin;
    public FactionSubRaid(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "raid";
    }

    @Override
    public String getDescription() {
        return """
                This is the raid faction command
                With this command you can raid a factions weak chunks
                The raid date format is the following: DD-MM-YYYY:TTTT (UTC + 0)
                With DD being day, MM being month and YYYY being year and TTTTT being time (24 hour format or military time)
                The chunks cords format is the following: X,Z (you get those with BlueMap)
                """;
    }

    @Override
    public String getPermission() {
        return "simplefactions.raid";
    }

    @Override
    public String getUsage() {
        return "/faction raid <factionName> <raidDate> <chunksCords1> <chunksCoords2> ... <confirm>";
    }

    @Override
    public HashMap<String, CommandInterface> getSubCommands() {
        return new HashMap<>();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 5 || !args[args.length - 1].equalsIgnoreCase("confirm") || !(sender instanceof Player player) || !plugin.factionManager.playerFactionLink.containsKey(player.getUniqueId())) {
            sender.sendMessage(sendUsageError());
            return;
        }

        if (getPermission() != null && !player.hasPermission(getPermission())) return;
        
        FactionObject defendingFaction = null;
        for (FactionObject faction : plugin.factionManager.existingFactions){
            if (faction.getFactionName().equals(args[1])){
                defendingFaction = faction;
                break;
            }
            else {
                player.sendMessage(ChatColor.RED + "The defending faction you tried to attack doesn't exists!");
                return;
            }
        }

        ArrayList<Chunk> attackedChunks = new ArrayList<>();
        for (int i = 3; i < args.length - 2; i++) {
            attackedChunks.add(Bukkit.getWorld("world").getChunkAt(
                    Integer.parseInt(args[i].substring(0)),
                    Integer.parseInt(args[i].substring(2))
            ));
        }
        plugin.raidManager.SendRaidDeclaration(player, attackedChunks, defendingFaction, args[2]);

    }
}
