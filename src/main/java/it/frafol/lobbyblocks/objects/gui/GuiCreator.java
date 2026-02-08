package it.frafol.lobbyblocks.objects.gui;

import it.frafol.lobbyblocks.LobbyBlocks;
import it.frafol.lobbyblocks.objects.ChatUtil;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class GuiCreator {

    private final LobbyBlocks plugin = LobbyBlocks.getInstance();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public Inventory createInventory(InventoryHolder inventoryHolder, Integer size, String title) {
        if (!ChatUtil.supportsMiniMessage()) return SpigotGuiCreator.createInventory(inventoryHolder, size, title);
        return plugin.getServer().createInventory(inventoryHolder, size, miniMessage.deserialize(title));
    }

    public void setItem(Inventory inventory, ItemStack item, int slot) {
        if (ChatUtil.supportsMiniMessage()) inventory.setItem(slot, item);
        else SpigotGuiCreator.setItem(inventory, item, slot);
    }

    public void setItemName(ItemStack item, String name) {
        if (!ChatUtil.supportsMiniMessage()) SpigotGuiCreator.setItemName(item, name);
        else {
            if (name == null) return;
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return;
            meta.displayName(miniMessage.deserialize(name));
            item.setItemMeta(meta);
        }
    }

    public void setItemLore(ItemStack item, List<String> lore) {
        if (!ChatUtil.supportsMiniMessage()) SpigotGuiCreator.setItemLore(item, lore);
        else {
            if (lore == null || lore.isEmpty()) return;
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return;
            List<Component> componentLore = lore.stream()
                    .map(miniMessage::deserialize)
                    .collect(Collectors.toList());
            meta.lore(componentLore);
            item.setItemMeta(meta);
        }
    }
}