package it.frafol.lobbyblocks.objects.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class CustomGui implements InventoryHolder {

    protected Inventory inventory;
    protected final Player player;
    private final Map<Integer, Consumer<InventoryClickEvent>> clickHandlers = new HashMap<>();

    public CustomGui(Player player, int size, String title) {
        this.player = player;
        this.inventory = GuiCreator.createInventory(this, size, title);
    }

    protected void setItemWithAction(int slot, ItemStack item, Consumer<InventoryClickEvent> action) {
        GuiCreator.setItem(inventory, item, slot);
        if (action != null) {
            clickHandlers.put(slot, action);
        }
    }

    public void handleClick(InventoryClickEvent event) {
        Consumer<InventoryClickEvent> action = clickHandlers.get(event.getSlot());
        if (action != null) {
            action.accept(event);
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    protected abstract void setupItems();
}