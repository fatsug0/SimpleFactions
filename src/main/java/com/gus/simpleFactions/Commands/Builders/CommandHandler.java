package com.gus.simpleFactions.Commands.Builders;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public abstract class CommandHandler extends BukkitCommand {


    public CommandHandler(String command, String[] aliases, String description, String permission, String usage) {
        super(command);

        this.setAliases(Arrays.asList(aliases));
        this.setDescription(description);
        this.setPermission(permission);
        this.setUsage(usage);
        this.setPermissionMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You do not have permission!");

        try {
            // This is used to register the command on the server and not use the plugin.yml
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap map = (CommandMap) field.get(Bukkit.getServer());
            map.register(this.getName(), this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean execute(@NonNull CommandSender sender, @NonNull String label, String @NonNull [] args) {
        execute(sender, args);
        return false;
    }

    @Override
    public List<String> tabComplete(@NonNull CommandSender sender, @NonNull String alias, @NonNull String[] args) throws IllegalArgumentException {
        return onTabComplete(sender, args);
    }


    public abstract void execute(CommandSender sender, String[] args);

    public abstract List<String> onTabComplete(CommandSender sender, String[] args);
}
