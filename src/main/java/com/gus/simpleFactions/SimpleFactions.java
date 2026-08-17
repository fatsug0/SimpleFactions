package com.gus.simpleFactions;

import com.gus.simpleFactions.Commands.Faction.FactionCommand;
import com.gus.simpleFactions.EventListeners.ClaimedChunksChecker;
import com.gus.simpleFactions.FactionHandlers.FactionManager;
import com.gus.simpleFactions.EventListeners.MainEventListener;
import com.gus.simpleFactions.Json.JsonHandler;
import com.gus.simpleFactions.Miscellaneous.FactionCommandUi;
import com.gus.simpleFactions.Miscellaneous.TeleportManager;
import com.gus.simpleFactions.RaidHandlers.RaidManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleFactions extends JavaPlugin {

    public FactionManager factionManager;
    public TeleportManager teleportManager;
    public RaidManager raidManager;
    public JsonHandler jsonHandler;
    public FactionCommandUi factionCommandUi;

    public FactionCommand factionCommand;

    @Override
    public void onEnable() {

        // Load config file
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        // Initiate managers
        try {
            factionManager = new FactionManager(this);
            teleportManager = new TeleportManager(this);
            raidManager = new RaidManager(this);
            jsonHandler = new JsonHandler(this);
            factionCommandUi = new FactionCommandUi(this);
        } catch (Exception e) {
            Bukkit.getPluginManager().disablePlugin(this);
        }

        // Load all json data
        jsonHandler.LoadSequence();

        // DEBUG ONLY: wipe everything that was just loaded if configured to (see config.yml)
        if (getConfig().getBoolean("debug.wipe-factions-on-start")) {
            factionManager.WipeAllFactionData();
        }

        // Redraw every remaining faction's claims on BlueMap - the map addon only hears about
        // claim changes reactively (on claim/unclaim), so without this, claims loaded from disk
        // would stay invisible on the map until something claimed/unclaimed next to them.
        if (factionManager.factionMapRenderService.getUSE_BLUEMAP_ADDON()) {
            for (var faction : factionManager.factionMembershipService.getExistingFactions()) {
                factionManager.factionMapRenderService.RedrawClaims(faction);
            }
        }

        // initiate event listeners
        Bukkit.getPluginManager().registerEvents(new MainEventListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ClaimedChunksChecker(this), this);

        // Initiate commands
        InitiateCommands();

        // Start raid checking
        raidManager.StartCheckForWaitingRaids();

        // Others
        Misc();
    }

    @Override
    public void onDisable() {

        // Save all json data
        jsonHandler.SaveSequence();

        // Stop all current raids
        if (raidManager.task != null) {
            raidManager.task.cancel();
        }
    }

    private void InitiateCommands() {
        factionCommand = new FactionCommand(this);
    }

    private void StartUpBanner() {
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

    private void Misc() {
        StartUpBanner();
        System.out.println(getConfig().getBoolean("enable-bluemap-addon") ? "[+] BlueMap addon enabled !" : "[-] BlueMap addon disabled !");
    }
}
