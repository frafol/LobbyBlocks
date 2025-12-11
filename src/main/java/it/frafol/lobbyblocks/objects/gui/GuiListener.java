package it.frafol.lobbyblocks.objects.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class GuiListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof CustomGui) {
            CustomGui customGui = (CustomGui) holder;
            event.setCancelled(true);
            if (event.getClickedInventory() == null || !event.getClickedInventory().equals(customGui.getInventory())) {
                return;
            }
            customGui.handleClick(event);
        }
    }
}
