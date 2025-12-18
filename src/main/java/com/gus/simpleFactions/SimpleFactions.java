package com.gus.simpleFactions;

import com.gus.simpleFactions.Commands.FactionCommand;
import com.gus.simpleFactions.Commands.FactionCommandCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SimpleFactions extends JavaPlugin {

    public FactionManager factionManager;
    public TeleportManager teleportManager;

    @Override
    public void onEnable() {

        factionManager = new FactionManager(this);
        teleportManager = new TeleportManager(this);

        Objects.requireNonNull(getCommand("faction")).setExecutor(new FactionCommand(this));
        Objects.requireNonNull(getCommand("faction")).setTabCompleter(new FactionCommandCompleter(this));

        System.out.println("Plugin enabled!");
    }

    @Override
    public void onDisable() {
    }
}
