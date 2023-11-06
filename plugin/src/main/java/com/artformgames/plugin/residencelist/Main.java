package com.artformgames.plugin.residencelist;

import cc.carm.lib.easyplugin.EasyPlugin;
import cc.carm.lib.easyplugin.gui.GUI;
import cc.carm.lib.easyplugin.i18n.EasyPluginMessageProvider;
import cc.carm.lib.mineconfiguration.bukkit.MineConfiguration;
import com.artformgames.plugin.residencelist.command.AdminCommands;
import com.artformgames.plugin.residencelist.command.UserCommands;
import com.artformgames.plugin.residencelist.conf.PluginConfig;
import com.artformgames.plugin.residencelist.conf.PluginMessages;
import com.artformgames.plugin.residencelist.hooker.PluginExpansion;
import com.artformgames.plugin.residencelist.listener.EditHandler;
import com.artformgames.plugin.residencelist.listener.ResidenceListener;
import com.artformgames.plugin.residencelist.listener.UserListener;
import com.artformgames.plugin.residencelist.manager.ResidenceManagerImpl;
import com.artformgames.plugin.residencelist.manager.UserStorageManager;
import com.artformgames.plugin.residencelist.utils.GHUpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Main extends EasyPlugin implements ResidenceListPlugin {
    private static Main instance;


    public Main() {
        super(EasyPluginMessageProvider.EN_US);
        Main.instance = this;
        ResidenceListAPI.plugin = this;
    }

    protected MineConfiguration configuration;
    protected ResidenceManagerImpl residenceManager;
    protected UserStorageManager userManager;


    @Override
    protected void load() {

        log("Loading plugin configurations...");
        this.configuration = new MineConfiguration(this);
        this.configuration.initializeConfig(PluginConfig.class);
        this.configuration.initializeMessage(PluginMessages.class);

        log("Initialize users manager...");
        this.userManager = new UserStorageManager(this);

        log("Initialize residence manager...");
        this.residenceManager = new ResidenceManagerImpl(this);

    }

    @Override
    protected boolean initialize() {
        log("Loading all residence data...");
        int loaded = this.residenceManager.loadAllResidences();
        log("Successfully loaded " + loaded + " residence data.");

        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            log("Load online users' data...");
            getUserManager().loadOnline(Player::getUniqueId);
        }

        log("Register listeners...");
        GUI.initialize(this);
        registerListener(new EditHandler());
        registerListener(new ResidenceListener());
        registerListener(new UserListener());

        log("Register commands...");
        registerCommand("ResidenceList", new UserCommands(this));
        registerCommand("ResidenceListAdmin", new AdminCommands(this));

        log("Initializing Placeholders...");
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PluginExpansion(this, getName()).register();
        } else {
            log("PlaceholderAPI is not found, skipped.");
        }

        if (PluginConfig.METRICS.getNotNull()) {
            log("Initializing bStats...");
            new Metrics(this, 18946);
        }

        if (PluginConfig.CHECK_UPDATE.getNotNull()) {
            log("Start to check the plugin versions...");
            getScheduler().runAsync(GHUpdateChecker.runner(this));
        } else {
            log("Version checker is disabled, skipped.");
        }

        return true;
    }

    @Override
    protected void shutdown() {
        GUI.closeAll();

        log("Saving all users' data...");
        this.userManager.saveAll();

        log("Saving all residence data...");
        this.residenceManager.saveAll();

    }

    @Override
    public boolean isDebugging() {
        return PluginConfig.DEBUG.getNotNull();
    }

    public MineConfiguration getConfiguration() {
        return configuration;
    }

    public static void info(String... messages) {
        getInstance().log(messages);
    }

    public static void severe(String... messages) {
        getInstance().error(messages);
    }

    public static void debugging(String... messages) {
        getInstance().debug(messages);
    }

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void openGUI(@NotNull Player player) {

    }

    @Override
    public @NotNull ResidenceManagerImpl getResidenceManager() {
        return residenceManager;
    }

    @Override
    public @NotNull UserStorageManager getUserManager() {
        return userManager;
    }
}