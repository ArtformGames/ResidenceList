package com.artformgames.plugin.residencelist.conf;

import cc.carm.lib.configuration.source.ConfigurationHolder;
import cc.carm.lib.mineconfiguration.bukkit.source.BukkitConfigFactory;
import cc.carm.lib.mineconfiguration.common.AbstractConfiguration;
import com.artformgames.plugin.residencelist.Main;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class PluginConfiguration extends AbstractConfiguration<ConfigurationHolder<?>> {

    public PluginConfiguration(@NotNull Main main) {
        super(
                BukkitConfigFactory.from(new File(main.getDataFolder(), "config.yml")).build(),
                BukkitConfigFactory.from(new File(main.getDataFolder(), "messages.yml")).build()
        );
    }

}
