package it.frafol.lobbyblocks.objects.gui;

import it.frafol.lobbyblocks.LobbyBlocks;
import it.frafol.lobbyblocks.objects.ChatUtil;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class SpigotGuiCreator {

    private final LobbyBlocks plugin = LobbyBlocks.getInstance();

    public Inventory createInventory(InventoryHolder inventoryHolder, Integer size, String title) {
        title = ChatUtil.color(title);
        return plugin.getServer().createInventory(inventoryHolder, size, Component.text(title));
    }

    public void setItem(Inventory inventory, ItemStack item, int slot) {
        inventory.setItem(slot, item);
    }

    public void setItemName(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        item.setItemMeta(meta);
    }

    public void setItemLore(ItemStack item, List<String> lore) {
        if (lore == null || lore.isEmpty()) return;
        lore = ChatUtil.color(lore);
        ItemMeta meta = item.getItemMeta();
        List<Component> componentLore = lore.stream()
                .map(Component::text)
                .collect(Collectors.toList());
        meta.lore(componentLore);
        item.setItemMeta(meta);
    }
}