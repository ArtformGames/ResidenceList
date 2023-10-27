package com.artformgames.plugin.residencelist;

import cc.carm.lib.easyplugin.EasyPlugin;
import cc.carm.lib.easyplugin.gui.GUI;
import cc.carm.lib.mineconfiguration.bukkit.MineConfiguration;
import com.artformgames.plugin.residencelist.conf.PluginConfig;
import com.artformgames.plugin.residencelist.conf.PluginMessages;
import com.artformgames.plugin.residencelist.listener.ResidenceListener;
import com.artformgames.plugin.residencelist.manager.ResidenceManagerImpl;
import com.artformgames.plugin.residencelist.manager.UserStorageManager;
import com.artformgames.plugin.residencelist.utils.GHUpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Main extends EasyPlugin implements ResidenceListPlugin {
    private static Main instance;


    public Main() {
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

        log("Initialize requests manager...");
        this.residenceManager = new ResidenceManagerImpl(this);
        this.residenceManager.loadAllResidences();


    }

    @Override
    protected boolean initialize() {

        log("Register listeners...");
        GUI.initialize(this);
        registerListener(new ResidenceListener());

        log("Register commands...");


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