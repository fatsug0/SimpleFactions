package com.gus.simpleFactions.Commands.Builders;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.StringJoiner;

public interface CommandInterface {
    String getName();
    String getDescription();
    String getPermission();
    String getUsage();
    String getId();
    HashMap<String, CommandInterface> getSubCommands();
    ItemStack getIcon();

    void execute(CommandSender sender, String[] args);

    /**
     * Whether this command only makes sense for a player who is currently in a faction.
     * Defaults to true since most subcommands operate on "your faction". Commands like
     * create/join/help/admin, which are meant to be usable before joining a faction (or don't
     * concern faction membership at all), override this to false.
     */
    default boolean requiresFaction() {
        return true;
    }

    /**
     * Whether this command needs additional arguments beyond its own fixed path to run (a
     * player name, a rank name, a "confirm" safety token, etc.). Defaults to true (conservative:
     * show usage rather than guess). Leaf commands whose full usage is exactly their path with
     * nothing else needed (e.g. "/faction claim") override this to false, which lets the GUI run
     * them directly on click instead of just showing their usage.
     * <p>
     * Commands gated behind a literal "confirm" token deliberately keep the default (true) even
     * though the token itself is fixed - that confirmation step exists specifically to stop a
     * single accidental click/keypress from doing something destructive, and a GUI auto-execute
     * would defeat that safety net.
     */
    default boolean requiresInput() {
        return true;
    }

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
