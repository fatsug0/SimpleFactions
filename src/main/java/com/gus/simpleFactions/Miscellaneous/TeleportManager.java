package com.gus.simpleFactions.Miscellaneous;

import com.gus.simpleFactions.SimpleFactions;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class TeleportManager {

    private final SimpleFactions plugin;
    public TeleportManager(SimpleFactions plugin){
        this.plugin = plugin;
    }

    private Map<UUID, BukkitTask> currentTeleports = new HashMap<>();
    private final Map<UUID, BukkitTask> teleportEffects = new HashMap<>();

    public void StartTeleport(UUID player, int delay, Location toTeleport){

        // Cannot teleport twice or more at the same time
        CancelTeleport(player, false);

        // Get the start location, the reference
        Location startLocationCache = Objects.requireNonNull(Bukkit.getPlayer(player)).getLocation().clone();

        // Start the Bukkit Task to check for movement
        BukkitTask task = new BukkitRunnable() {

            int timer = delay * 20; // *20 because there are 20 ticks in a second

            @Override
            public void run() {
                // Start particle effect
                playTeleportWarmup(Objects.requireNonNull(Bukkit.getPlayer(player)));

                // Check if the player has moved
                if (hasMoved(startLocationCache, Objects.requireNonNull(Bukkit.getPlayer(player)).getLocation())){
                    Objects.requireNonNull(Bukkit.getPlayer(player)).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("\"Teleportation cancelled, you moved !"));
                    CancelTeleport(player, true);
                    cancel();
                    return;
                }

                // Check if the timer is finished
                if (timer <= 0){
                    Objects.requireNonNull(Bukkit.getPlayer(player)).teleport(toTeleport);
                    Objects.requireNonNull(Bukkit.getPlayer(player)).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Teleporting ..."));
                    playTeleportSuccess(Objects.requireNonNull(Bukkit.getPlayer(player)));
                    currentTeleports.remove(player);
                    cancel();
                    return;
                }

                // Send a status message every second
                if (timer % 20 == 0) Objects.requireNonNull(Bukkit.getPlayer(player)).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Teleporting in: " + timer / 20 + " !"));

                // Else continue counting
                timer -= 5;
            }
        }.runTaskTimer(plugin, 0, 5);

        currentTeleports.put(player, task);
    }

    // Check if the start location is the same as the current location
    private boolean hasMoved(Location startLocation, Location currentLocation){
        return startLocation.getBlockX() != currentLocation.getBlockX() ||
                startLocation.getBlockY() != currentLocation.getBlockY() ||
                startLocation.getBlockZ() != currentLocation.getBlockZ();
    }

    // Cancel the Bukkit task, in consequence, the teleport
    private void CancelTeleport(UUID player, boolean vfx){
        BukkitTask task = currentTeleports.remove(player);
        if (vfx) playTeleportCancel(Objects.requireNonNull(Bukkit.getPlayer(player)));
        if (task != null) task.cancel();
    }

    public static void playTeleportWarmup(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);

        // Large portal swirl around player
        player.getWorld().spawnParticle(
                Particle.PORTAL,
                loc.clone().add(0, 1, 0),
                150,
                0.8, 1.2, 0.8,
                0.15
        );

        // Rising enchant particles
        player.getWorld().spawnParticle(
                Particle.ENCHANT,
                loc.clone().add(0, 0.2, 0),
                60,
                0.6, 1.0, 0.6,
                0.5
        );

        // Charging sound
        player.playSound(
                loc,
                Sound.BLOCK_BEACON_POWER_SELECT,
                0.6f,
                1.2f
        );
    }
    public static void playTeleportSuccess(Player player) {
        Location loc = player.getLocation();

        // Bright flash
        player.getWorld().spawnParticle(
                Particle.FLASH,
                loc.clone().add(0, 1, 0),
                1,
                Color.WHITE
        );

        // Reverse portal burst
        player.getWorld().spawnParticle(
                Particle.REVERSE_PORTAL,
                loc.clone().add(0, 1, 0),
                200,
                1.0, 1.5, 1.0,
                0.15
        );

        // Spark burst
        player.getWorld().spawnParticle(
                Particle.END_ROD,
                loc.clone().add(0, 1, 0),
                80,
                0.6, 1.0, 0.6,
                0.05
        );

        // Strong teleport sound
        player.playSound(
                loc,
                Sound.ENTITY_ENDERMAN_TELEPORT,
                1.2f,
                0.9f
        );
    }
    public static void playTeleportCancel(Player player) {
        Location loc = player.getLocation();

        // Smoke burst
        player.getWorld().spawnParticle(
                Particle.LARGE_SMOKE,
                loc.clone().add(0, 1, 0),
                50,
                0.6, 0.8, 0.6,
                0.02
        );

        // Angry particles
        player.getWorld().spawnParticle(
                Particle.ANGRY_VILLAGER,
                loc.clone().add(0, 1.8, 0),
                15,
                0.4, 0.2, 0.4,
                0.01
        );

        // Fail sound
        player.playSound(
                loc,
                Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR,
                0.8f,
                0.7f
        );
    }

}