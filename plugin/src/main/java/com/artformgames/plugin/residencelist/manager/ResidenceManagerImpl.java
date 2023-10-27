package com.artformgames.plugin.residencelist.manager;

import cc.carm.lib.easyplugin.EasyPlugin;
import com.artformgames.plugin.residencelist.Main;
import com.artformgames.plugin.residencelist.api.ResidenceManager;
import com.artformgames.plugin.residencelist.api.residence.ResidenceData;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public class ResidenceManagerImpl implements ResidenceManager {
    public static final String RESIDENCE_DATA_FOLDER = "residences";

    protected final @NotNull File storageFolder;
    protected Map<String, ResidenceData> residences;

    public ResidenceManagerImpl(EasyPlugin plugin) {
        this.storageFolder = new File(plugin.getDataFolder(), RESIDENCE_DATA_FOLDER);
        if (!this.storageFolder.exists()) {
            this.storageFolder.mkdirs();
        } else if (!this.storageFolder.isDirectory()) {
            this.storageFolder.delete();
            this.storageFolder.mkdirs();
        }
    }

    public @NotNull File getStorageFolder() {
        return storageFolder;
    }

    public Map<String, ResidenceData> getResidences() {
        return residences;
    }

    public void loadAllResidences() {
        String[] filesList = getStorageFolder().list();
        if (filesList == null || filesList.length < 1) return;

        List<File> files = Arrays.stream(filesList)
                .map(s -> new File(getStorageFolder(), s))
                .filter(File::isFile).toList();

        HashMap<String, ResidenceData> loaded = new HashMap<>();

        if (!files.isEmpty()) {
            for (File file : files) {
                String residenceName = file.getName().substring(0, file.getName().lastIndexOf("."));
                try {
                    ResidenceData data = loadResidence(residenceName, file);
                    Main.debugging("Successfully loaded residence data for '" + residenceName + "' !");
                    loaded.put(residenceName, data);
                } catch (Exception ex) {
                    Main.severe("Error occurred when loading residence data #" + file.getAbsolutePath() + " !");
                    ex.printStackTrace();
                }
            }
        }

        this.residences = loaded;
    }

    public ResidenceData loadResidence(String residenceName, File file) throws Exception {
        ClaimedResidence residence = Residence.getInstance().getResidenceManager().getByName(residenceName);
        if (residence == null) throw new Exception("Residence not found: " + residenceName);
        return new ResidenceData(file, residence);
    }

    public void renameResidence(String oldName, String newName) {
        ResidenceData data = this.residences.get(oldName);
        if (data == null) return; // No data for this residence yet.

        this.residences.remove(oldName);
        this.residences.put(newName, data);

        File n = new File(this.storageFolder, newName + ".yaml");
        try {
            data.renameTo(n);
            Main.debugging("Successfully renamed residence data for '" + oldName + "' to '" + newName + "' !");

        } catch (Exception e) {
            Main.severe("Error occurred when renaming residence data for '" + oldName + "' to '" + newName + "' !");
            e.printStackTrace();
        }
    }

    @Override
    public @Unmodifiable @NotNull Set<ResidenceData> listData() {
        return Set.copyOf(this.residences.values());
    }

    @Override
    public @NotNull ResidenceData getData(@NotNull String name) {
        ResidenceData existed = this.residences.get(name);
        if (existed != null) return existed;

        ClaimedResidence residence = Residence.getInstance().getResidenceManager().getByName(name);
        if (residence == null) throw new IllegalArgumentException("Residence not found: " + name);

        File dataFile = new File(this.storageFolder, residence.getName() + ".yaml");
        ResidenceData data = new ResidenceData(dataFile, residence);
        this.residences.put(residence.getName(), data);

        return data;
    }

    @Override
    public @NotNull ResidenceData updateData(@NotNull String name,
                                             @NotNull Consumer<ResidenceData> dataConsumer) {
        ResidenceData data = getData(name);
        dataConsumer.accept(data);
        return data;
    }

    public void saveAll() {
        for (ResidenceData data : this.residences.values()) {
            try {
                data.save();
            } catch (Exception e) {
                Main.severe("Error occurred when saving residence data for '" + data.getName() + "' !");
                e.printStackTrace();
            }
        }
    }
}
