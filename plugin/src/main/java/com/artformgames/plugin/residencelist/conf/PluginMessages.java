package com.artformgames.plugin.residencelist.conf;

import cc.carm.lib.configuration.core.ConfigurationRoot;
import cc.carm.lib.mineconfiguration.bukkit.value.ConfiguredMessageList;
import cc.carm.lib.mineconfiguration.bukkit.value.ConfiguredSound;
import org.bukkit.Sound;

public class PluginMessages extends ConfigurationRoot {


    public static final ConfiguredMessageList<String> LOAD_FAILED = ConfiguredMessageList.asStrings()
            .defaults("&c&lSorry! &fBut your residence data failed to load, please rejoin!")
            .build();


    public static final class PIN extends ConfigurationRoot {
        public static final ConfiguredSound SOUND = ConfiguredSound.of("BLOCK_ANVIL_PLACE");
        public static final ConfiguredMessageList<String> MESSAGE = ConfiguredMessageList.asStrings()
                .defaults("&fYou have successfully pinned the residence &e%(residence)&f!")
                .params("residence")
                .build();
    }


    public static final class UNPIN extends ConfigurationRoot {
        public static final ConfiguredSound SOUND = ConfiguredSound.of(Sound.BLOCK_ANVIL_BREAK, 0.5F);
        public static final ConfiguredMessageList<String> MESSAGE = ConfiguredMessageList.asStrings()
                .defaults("&fYou have unpinned the residence &e%(residence)&f!")
                .params("residence")
                .build();
    }


    public static final class TELEPORT extends ConfigurationRoot {
        public static final ConfiguredSound SOUND = ConfiguredSound.of(Sound.ENTITY_ENDERMAN_TELEPORT, 0.5F);

        public static final ConfiguredMessageList<String> NO_LOCATION = ConfiguredMessageList.asStrings()
                .defaults("&c&lSorry! &fBut you cannot teleport to &e%(residence) &fyet!")
                .params("residence")
                .build();
    }


    public static final class COMMENT extends ConfigurationRoot {
        public static final ConfiguredSound ASK_SOUND = ConfiguredSound.of(Sound.ENTITY_CHICKEN_EGG, 0.5F);
        public static final ConfiguredSound YES_SOUND = ConfiguredSound.of(Sound.ENTITY_VILLAGER_YES, 0.5F);
        public static final ConfiguredSound NO_SOUND = ConfiguredSound.of(Sound.ENTITY_VILLAGER_NO, 0.5F);

        public static final ConfiguredMessageList<String> NOTIFY = ConfiguredMessageList.asStrings()
                .defaults(
                        "&fYou are commenting for residence &e%(residence)&f, please enter your comment in chat.",
                        "&fYou can enter '&e#cancel&f' to cancel this operation."
                ).params("residence").build();
    }

    public static final class EDIT extends ConfigurationRoot {
        public static final ConfiguredSound EDIT_SOUND = ConfiguredSound.of(Sound.ENTITY_CHICKEN_EGG, 0.5F);
        public static final ConfiguredSound SUCCESS_SOUND = ConfiguredSound.of(Sound.BLOCK_LEVER_CLICK, 0.5F);
        public static final ConfiguredSound FAILED_SOUND = ConfiguredSound.of(Sound.ENTITY_VILLAGER_NO, 0.5F);

        public static final ConfiguredMessageList<String> NAME = ConfiguredMessageList.asStrings()
                .defaults(
                        "&fYou are setting up nickname for residence &e%(residence)&f, please enter in chat.",
                        "&fRemember that a nickname should be &eless than 16 characters&f.",
                        "&fYou can enter '&e#cancel&f' to cancel this operation."
                ).params("residence").build();


        public static final ConfiguredMessageList<String> DESCRIPTION = ConfiguredMessageList.asStrings()
                .defaults(
                        "&fYou are editing description for residence &e%(residence)&f, please enter in chat.",
                        "&fRemember that you can use '&e\\n&f' to wrap lines.",
                        "&fYou can enter '&e#cancel&f' to cancel this operation."
                ).params("residence").build();

        public static final ConfiguredMessageList<String> NAME_TOO_LONG = ConfiguredMessageList.asStrings()
                .defaults(
                        "&c&lSorry! &fBut the nickname that you input is too long,",
                        "&fRemember that a nickname should be &eless than 16 characters&f."
                ).params("residence").build();


        public static final ConfiguredMessageList<String> NAME_UPDATED = ConfiguredMessageList.asStrings()
                .defaults("&fYou have successfully updated the nickname of residence &e%(residence)&f!")
                .params("residence")
                .build();

        public static final ConfiguredMessageList<String> DESCRIPTION_UPDATED = ConfiguredMessageList.asStrings()
                .defaults("&fYou have successfully updated the description of residence &e%(residence)&f!")
                .params("residence")
                .build();

        public static final ConfiguredMessageList<String> ICON_UPDATED = ConfiguredMessageList.asStrings()
                .defaults("&fYou have successfully updated the icon of residence &e%(residence)&f!")
                .params("residence")
                .build();
    }


}

