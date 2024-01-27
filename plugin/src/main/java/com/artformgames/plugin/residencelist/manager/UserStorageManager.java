package com.artformgames.plugin.residencelist.manager;

import cc.carm.lib.easyplugin.EasyPlugin;
import cc.carm.lib.easyplugin.user.UserDataManager;
import com.artformgames.plugin.residencelist.api.UserManager;
import com.artformgames.plugin.residencelist.api.sort.SortFunctions;
import com.artformgames.plugin.residencelist.storage.yaml.YAMLUserData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

public class UserStorageManager extends UserDataManager<UUID, YAMLUserData> implements UserManager {

    public static final String USER_DATA_FOLDER = "users";

    protected final @NotNull File folder;

    public UserStorageManager(@NotNull EasyPlugin plugin) {
        super(plugin);
        this.folder = new File(plugin.getDataFolder(), USER_DATA_FOLDER);
        if (!this.folder.exists()) {
            this.folder.mkdirs();
        } else if (!this.folder.isDirectory()) {
            this.folder.delete();
            this.folder.mkdirs();
        }
    }

    @Override
    public @NotNull YAMLUserData emptyUser(@NotNull UUID key) {
        return new YAMLUserData(key, new ArrayList<>(), SortFunctions.NAME, false);
    }

    @Override
    protected @Nullable YAMLUserData loadData(@NotNull UUID key) throws Exception {
        File userFile = new File(this.folder, key + ".yaml");
        if (userFile.exists()) {
            YamlConfiguration conf = YamlConfiguration.loadConfiguration(userFile);
            return new YAMLUserData(
                    key, new ArrayList<>(conf.getStringList("pinned")),
                    SortFunctions.parse(conf.getInt("sort", 0)),
                    conf.getBoolean("reversed", false)
            );
        }
        return null;
    }

    @Override
    protected void saveData(@NotNull YAMLUserData data) throws Exception {
        File userFile = new File(this.folder, data.getKey() + ".yaml");
        YamlConfiguration conf;

        if (!userFile.exists()) {
            conf = new YamlConfiguration();
            userFile.createNewFile();
        } else {
            conf = YamlConfiguration.loadConfiguration(userFile);
        }

        conf.set("pinned", data.getPinned());
        if (data.getSortFunction() != SortFunctions.NAME) {
            conf.set("sort", data.getSortFunction().ordinal());
        }
        if (data.isSortReversed()) conf.set("reversed", true);
        conf.save(userFile);
    }
}
