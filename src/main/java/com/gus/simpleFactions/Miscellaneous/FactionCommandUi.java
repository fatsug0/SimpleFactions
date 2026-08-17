package com.gus.simpleFactions.Miscellaneous;

import com.gus.simpleFactions.Commands.Builders.CommandInterface;
import com.gus.simpleFactions.SimpleFactions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FactionCommandUi {
    private final SimpleFactions plugin;
    public FactionCommandUi(SimpleFactions plugin) {
        this.plugin = plugin;
    }

    private static final String TITLE = ChatColor.GOLD.toString() + ChatColor.BOLD + "Faction Command";

    // All 28 interior slots of the 9x6 menu (everything but the header/footer rows and side columns).
    private static final int[] COMMAND_SLOTS = {
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    private static final int BACK_SLOT = 0;
    private static final int EXIT_SLOT = 49;

    private final Map<UUID, GuiSession> sessions = new HashMap<>();

    /** Per-player state for an open faction GUI. Rebuilt fresh every time OpenCommand is called. */
    private static class GuiSession {
        final Deque<CommandInterface> path = new ArrayDeque<>();
        final Map<Integer, CommandInterface> slotCommands = new HashMap<>();
        Inventory inventory;
    }

    private static class FactionGuiHolder implements InventoryHolder {
        private Inventory inventory;
        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    public void OpenCommand(Player sender, String commandName) {
        CommandInterface rootCommand = findCommandByName(commandName);
        if (rootCommand == null) {
            sender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Command not found: " + commandName + "!");
            return;
        }

        GuiSession session = new GuiSession();
        session.path.push(rootCommand);

        FactionGuiHolder holder = new FactionGuiHolder();
        session.inventory = Bukkit.createInventory(holder, 9 * 6, TITLE);
        holder.inventory = session.inventory;

        sessions.put(sender.getUniqueId(), session);
        populate(sender, session);
        sender.openInventory(session.inventory);
    }

    public boolean isFactionGuiInventory(Inventory inventory) {
        return inventory != null && inventory.getHolder() instanceof FactionGuiHolder;
    }

    public void handleClose(Player player) {
        sessions.remove(player.getUniqueId());
    }

    public void handleClick(Player player, int slot) {
        GuiSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        if (slot == EXIT_SLOT) {
            player.closeInventory();
            return;
        }

        if (slot == BACK_SLOT) {
            if (session.path.size() > 1) {
                session.path.pop();
                populate(player, session);
            }
            return;
        }

        CommandInterface clicked = session.slotCommands.get(slot);
        if (clicked == null) return; // decorative slot, nothing bound to it

        String permission = clicked.getPermission();
        if (permission != null && !permission.isBlank() && !player.hasPermission(permission)) {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You do not have permission for that command!");
            return;
        }

        if (!isLeaf(clicked)) {
            // Always navigate into commands that have subcommands.
            session.path.push(clicked);
            populate(player, session);
            return;
        }

        // Leaf command: either run it directly, or close and hand the player its usage.
        if (clicked.requiresInput()) {
            player.closeInventory();
            player.sendMessage(clicked.sendUsageError());
        } else {
            String[] args = buildArgs(session, clicked);
            player.closeInventory();
            // Deferred a tick in case the command itself opens another inventory (e.g. storage) -
            // doing that synchronously from inside this click event risks the same desync bug
            // that reopening our own menu had.
            Bukkit.getScheduler().runTask(plugin, () -> clicked.execute(player, args));
        }
    }

    private boolean isLeaf(CommandInterface command) {
        Map<String, CommandInterface> subCommands = command.getSubCommands();
        return subCommands == null || subCommands.isEmpty();
    }

    /** Builds the args array a directly-executed leaf command expects, e.g. ["home", "set"]. */
    private String[] buildArgs(GuiSession session, CommandInterface leaf) {
        List<CommandInterface> ordered = new ArrayList<>(session.path);
        Collections.reverse(ordered); // root ("faction") first

        List<String> args = new ArrayList<>();
        for (int i = 1; i < ordered.size(); i++) { // skip the root itself - args start after it
            args.add(ordered.get(i).getName());
        }
        args.add(leaf.getName());
        return args.toArray(new String[0]);
    }

    private void populate(Player player, GuiSession session) {
        CommandInterface current = session.path.peek();
        if (current == null || session.inventory == null) return;
        Inventory inv = session.inventory;

        createFrame(inv);
        inv.setItem(4, withBreadcrumb(current.getIcon(), breadcrumb(session)));
        inv.setItem(BACK_SLOT, session.path.size() > 1 ? backButton() : filler());

        session.slotCommands.clear();

        List<CommandInterface> visible = accessibleSubCommands(player, current);
        for (int i = 0; i < COMMAND_SLOTS.length; i++) {
            int slot = COMMAND_SLOTS[i];
            if (i >= visible.size()) {
                inv.setItem(slot, filler());
                continue;
            }

            CommandInterface subCommand = visible.get(i);
            inv.setItem(slot, subCommand.getIcon());
            session.slotCommands.put(slot, subCommand);
        }

        // Cheap safety net against client-side desync on rapid clicking.
        player.updateInventory();
    }

    private String breadcrumb(GuiSession session) {
        List<CommandInterface> ordered = new ArrayList<>(session.path);
        Collections.reverse(ordered);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < ordered.size(); i++) {
            if (i > 0) builder.append(" » ");
            builder.append(ordered.get(i).getName());
        }
        return builder.toString();
    }

    private List<CommandInterface> accessibleSubCommands(Player player, CommandInterface command) {
        boolean inFaction = plugin.factionManager.factionMembershipService.getPlayerFactionLink().containsKey(player.getUniqueId());

        List<CommandInterface> result = new ArrayList<>();
        Map<String, CommandInterface> subCommands = command.getSubCommands();
        if (subCommands == null) return result;

        for (CommandInterface subCommand : subCommands.values()) {
            String permission = subCommand.getPermission();
            boolean permissionOk = permission == null || permission.isBlank() || player.hasPermission(permission);
            boolean factionOk = !subCommand.requiresFaction() || inFaction;
            if (permissionOk && factionOk) {
                result.add(subCommand);
            }
        }
        result.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return result;
    }

    public CommandInterface findCommandByName(String commandName) {
        ArrayList<CommandInterface> toCheck = new ArrayList<>();
        toCheck.add(plugin.factionCommand);

        while (!toCheck.isEmpty()) {
            ArrayList<CommandInterface> nextToCheck = new ArrayList<>();
            for (CommandInterface command : toCheck) {
                if (command.getName().equalsIgnoreCase(commandName)) {
                    return command;
                }
                if (command.getSubCommands() != null) {
                    nextToCheck.addAll(command.getSubCommands().values());
                }
            }
            toCheck = nextToCheck;
        }
        return null;
    }

    //region Item builders

    private void createFrame(Inventory inv) {
        for (int i : new int[]{1, 2, 3, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 50, 51, 52, 53}) {
            inv.setItem(i, filler());
        }
        inv.setItem(EXIT_SLOT, exitButton());
    }

    private ItemStack filler() {
        return named(new ItemStack(Material.GRAY_STAINED_GLASS_PANE), " ");
    }

    private ItemStack exitButton() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Exit");
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack backButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "« Back");
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack withBreadcrumb(ItemStack item, String breadcrumb) {
        if (item == null) return null;
        ItemStack copy = item.clone();
        ItemMeta meta = copy.getItemMeta();
        if (meta == null) return copy;

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + breadcrumb);
        lore.add("");
        if (meta.hasLore() && meta.getLore() != null) lore.addAll(meta.getLore());
        meta.setLore(lore);
        copy.setItemMeta(meta);
        return copy;
    }

    private ItemStack named(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    //endregion
}
