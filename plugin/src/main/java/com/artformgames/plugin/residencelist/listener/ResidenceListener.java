package com.artformgames.plugin.residencelist.listener;

import com.artformgames.plugin.residencelist.Main;
import com.artformgames.plugin.residencelist.api.ResidenceManager;
import com.artformgames.plugin.residencelist.api.residence.ResidenceData;
import com.bekvon.bukkit.residence.event.ResidenceChangedEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent;
import com.bekvon.bukkit.residence.event.ResidenceRenameEvent;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ResidenceListener implements Listener {

    /** Residence emits this after it accepts a change of the player's current residence. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onResidenceChanged(ResidenceChangedEvent event) {
        recordEntry(event, Main.getInstance().getResidenceManager());
    }

    static void recordEntry(ResidenceChangedEvent event, ResidenceManager<?> manager) {
        ClaimedResidence destination = event.getTo();
        if (destination == null || event.getPlayer() == null) return;

        // The list stores only top-level residences; moving between their subzones is not a new visit.
        ClaimedResidence top = destination.getTopParent();
        ClaimedResidence previous = event.getFrom();
        if (previous != null && previous.getTopParent().getName().equals(top.getName())) return;

        ResidenceData data = manager.getResidence(top.getName());
        if (data != null) data.recordVisit(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRename(ResidenceRenameEvent event) {
        // When a residence is renamed, we also need to update the residence name in our database.
        ClaimedResidence residence = event.getResidence();
        if (residence.isSubzone()) return; // Only main residence can be stored.
        ResidenceManager<?> manager = Main.getInstance().getResidenceManager();
        manager.renameResidence(event.getOldResidenceName(), event.getNewResidenceName());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDelete(ResidenceDeleteEvent event) {
        ClaimedResidence residence = event.getResidence();
        if (residence.isSubzone()) return; // Only main residence can be stored.
        ResidenceManager<?> manager = Main.getInstance().getResidenceManager();
        manager.removeResidence(residence.getResidenceName());
    }

}
