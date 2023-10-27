package com.artformgames.plugin.residencelist.listener;

import com.artformgames.plugin.residencelist.Main;
import com.artformgames.plugin.residencelist.manager.ResidenceManagerImpl;
import com.bekvon.bukkit.residence.event.ResidenceRenameEvent;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ResidenceListener implements Listener {

    @EventHandler
    public void onRename(ResidenceRenameEvent event) {
        ClaimedResidence residence = event.getResidence();
        if (!residence.isMainResidence()) return; // Only main residence can be storaged

        ResidenceManagerImpl manager = Main.getInstance().getResidenceManager();
        manager.renameResidence(event.getOldResidenceName(), event.getNewResidenceName());
    }
}
