package com.artformgames.plugin.residencelist.conf;

import cc.carm.lib.configuration.core.ConfigurationRoot;
import cc.carm.lib.configuration.core.annotation.ConfigPath;
import cc.carm.lib.configuration.core.annotation.HeaderComment;
import cc.carm.lib.configuration.core.value.type.ConfiguredValue;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;

public class PluginConfig extends ConfigurationRoot {

    public static final ConfiguredValue<Boolean> DEBUG = ConfiguredValue.of(Boolean.class, false);

    @HeaderComment({
            "Statistics Settings",
            "This option is used to help developers count plug-in versions and usage, and it will never affect performance and user experience.",
            "Of course, you can also choose to turn it off here for this plugin,",
            "or turn it off for all plugins in the configuration file under \"plugins/bStats\"."
    })
    public static final ConfiguredValue<Boolean> METRICS = ConfiguredValue.of(Boolean.class, true);

    @HeaderComment({
            "Check update settings",
            "This option is used by the plug-in to determine whether to check for updates.",
            "If you do not want the plug-in to check for updates and prompt you, you can choose to close.",
            "Checking for updates is an asynchronous operation that will never affect performance and user experience."
    })
    public static final ConfiguredValue<Boolean> CHECK_UPDATE = ConfiguredValue.of(Boolean.class, true);


    public static final class INFORMATION extends ConfigurationRoot {

        public static final ConfiguredValue<Material> DEFAULT_ICON = ConfiguredValue.builderOf(Material.class).fromString()
                .parseValue((v, d) -> XMaterial.matchXMaterial(v).map(XMaterial::parseMaterial).orElse(null))
                .serializeValue(v -> XMaterial.matchXMaterial(v).name())
                .defaults(Material.GRASS_BLOCK).build();


    }

}
