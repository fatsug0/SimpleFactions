package com.gus.simpleFactions;

import com.gus.simpleFactions.Commands.FactionCommand;
import com.gus.simpleFactions.Commands.FactionCommandCompleter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SimpleFactions extends JavaPlugin {

    public FactionManager factionManager;
    public TeleportManager teleportManager;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        factionManager = new FactionManager(this);
        teleportManager = new TeleportManager(this);

        Objects.requireNonNull(getCommand("faction")).setExecutor(new FactionCommand(this));
        Objects.requireNonNull(getCommand("faction")).setTabCompleter(new FactionCommandCompleter(this));

        Bukkit.getPluginManager().registerEvents(new MainEventListener(this), this);

        StartUpBanner();
    }

    @Override
    public void onDisable() {

    }

    private void StartUpBanner(){
        System.out.println("\n" +
                "\n" +
                "+============================================+\n" +
                "|     ____ ___ __  __ ____  _     _____      |\n" +
                "|    / ___|_ _|  \\/  |  _ \\| |   | ____|     |\n" +
                "|    \\___ \\| || |\\/| | |_) | |   |  _|       |\n" +
                "|     ___) | || |  | |  __/| |___| |___      |\n" +
                "|    |____/___|_|  |_|_|   |_____|_____|     |\n" +
                "| _____ _    ____ _____ ___ ___  _   _ ____  |\n" +
                "||  ___/ \\  / ___|_   _|_ _/ _ \\| \\ | / ___| |\n" +
                "|| |_ / _ \\| |     | |  | | | | |  \\| \\___ \\ |\n" +
                "||  _/ ___ \\ |___  | |  | | |_| | |\\  |___) ||\n" +
                "||_|/_/   \\_\\____| |_| |___\\___/|_| \\_|____/ |\n" +
                "+============================================+\n" +
                "             Developed by fatsug0 \n" +
                "\n");
    }
}
