package com.gus.simpleFactions.Commands.Builders;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.StringJoiner;

public interface CommandInterface {
    String getName();
    String getDescription();
    String getPermission();
    String getUsage();
    HashMap<String, CommandInterface> getSubCommands();

    default ItemStack getIcon() {
        HashMap<String, CommandInterface> subCommands = getSubCommands();
        if (subCommands != null && !subCommands.isEmpty()) {
            return null;
        }

        String usage = getUsage() == null ? "" : getUsage().toLowerCase();
        Material material;
        if (usage.contains("rank create")) {
            material = Material.ANVIL;
        } else if (usage.contains("rank delete")) {
            material = Material.BARRIER;
        } else if (usage.contains("rank info")) {
            material = Material.BOOKSHELF;
        } else if (usage.contains("permissions add")) {
            material = Material.ENCHANTED_BOOK;
        } else if (usage.contains("permissions list")) {
            material = Material.BOOKSHELF;
        } else if (usage.contains("permissions remove")) {
            material = Material.SHEARS;
        } else if (usage.contains("player add")) {
            material = Material.PLAYER_HEAD;
        } else if (usage.contains("player list")) {
            material = Material.PLAYER_HEAD;
        } else if (usage.contains("player remove")) {
            material = Material.SKELETON_SKULL;
        } else {
            material = switch (getName().toLowerCase()) {
                case "admin" -> Material.COMMAND_BLOCK;
                case "claim" -> Material.GRASS_BLOCK;
                case "create" -> Material.OAK_SIGN;
                case "disband" -> Material.TNT;
                case "help" -> Material.BOOK;
                case "info" -> Material.PAPER;
                case "invite" -> Material.WRITABLE_BOOK;
                case "join" -> Material.ENDER_PEARL;
                case "kick" -> Material.IRON_BOOTS;
                case "leave" -> Material.OAK_DOOR;
                case "prefix" -> Material.NAME_TAG;
                case "storage" -> Material.CHEST;
                case "unclaim" -> Material.IRON_SHOVEL;
                case "set" -> Material.RED_BED;
                case "select" -> Material.SPYGLASS;
                case "delete", "remove" -> Material.BARRIER;
                case "list" -> Material.MAP;
                case "add" -> Material.EMERALD;
                default -> Material.STONE_BUTTON;
            };
        }

        return new ItemStack(material);
    }

    void execute(CommandSender sender, String[] args);

    default String sendUsageError(){
        String accent = ChatColor.GOLD.toString();
        String muted = ChatColor.GRAY.toString();
        String label = ChatColor.YELLOW.toString();
        String value = ChatColor.WHITE.toString();

        StringBuilder message = new StringBuilder()
                .append(accent).append(ChatColor.BOLD).append("SimpleFactions Command").append(ChatColor.RESET).append("\n")
                .append(ChatColor.DARK_GRAY).append("--------------------------------").append(ChatColor.RESET).append("\n")
                .append(ChatColor.RED).append("Invalid usage for ").append(value).append("/").append(getName()).append(ChatColor.RESET).append("\n\n");

        String description = formatDescription(getDescription());
        if (!description.isBlank()) {
            message.append(muted).append(description).append(ChatColor.RESET).append("\n\n");
        }

        message.append(label).append("Usage: ").append(value).append(getUsage()).append(ChatColor.RESET);

        if (getPermission() != null && !getPermission().isBlank()) {
            message.append("\n").append(label).append("Permission: ").append(value).append(getPermission()).append(ChatColor.RESET);
        }

        if (getSubCommands() != null && !getSubCommands().isEmpty()) {
            message.append("\n").append(label).append("Subcommands: ").append(value).append(formatSubCommands()).append(ChatColor.RESET);
        }

        return message.toString();
    }

    private String formatDescription(String description) {
        if (description == null) return "";

        StringJoiner lines = new StringJoiner("\n");
        for (String line : description.strip().split("\\R")) {
            String trimmedLine = line.trim();
            if (!trimmedLine.isBlank()) {
                lines.add(trimmedLine);
            }
        }
        return lines.toString();
    }

    private String formatSubCommands() {
        StringJoiner subCommands = new StringJoiner(ChatColor.GRAY + ", " + ChatColor.WHITE);
        getSubCommands().keySet().stream()
                .sorted()
                .forEach(subCommands::add);
        return subCommands.toString();
    }
}
