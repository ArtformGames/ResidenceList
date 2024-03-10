package com.artformgames.plugin.residencelist.listener;

import com.artformgames.plugin.residencelist.Main;
import com.artformgames.plugin.residencelist.api.ResidenceManager;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent;
import com.bekvon.bukkit.residence.event.ResidenceRenameEvent;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ResidenceListener implements Listener {

    @EventHandler
    public void onRename(ResidenceRenameEvent event) {
        // When a residence is renamed, we also need to update the residence name in our database.
        ClaimedResidence residence = event.getResidence();
        if (residence.isSubzone()) return; // Only main residence can be stored.
        ResidenceManager<?> manager = Main.getInstance().getResidenceManager();
        manager.renameResidence(event.getOldResidenceName(), event.getNewResidenceName());
    }

    @EventHandler
    public void onDelete(ResidenceDeleteEvent event) {
        ClaimedResidence residence = event.getResidence();
        if (residence.isSubzone()) return; // Only main residence can be stored.
        ResidenceManager<?> manager = Main.getInstance().getResidenceManager();
        manager.removeResidence(residence.getResidenceName());
    }

}
