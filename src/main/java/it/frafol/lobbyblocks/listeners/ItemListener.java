package it.frafol.lobbyblocks.listeners;

import it.frafol.lobbyblocks.LobbyBlocks;
import it.frafol.lobbyblocks.enums.SpigotConfig;
import it.frafol.lobbyblocks.objects.BlockItem;
import it.frafol.lobbyblocks.objects.GuiUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class ItemListener implements Listener {

    private final LobbyBlocks plugin = LobbyBlocks.getInstance();

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {

        final ItemStack dropped = event.getItemDrop().getItemStack();
        final Player player = event.getPlayer();
        if (dropped.getItemMeta() == null) return;
        if (Objects.equals(dropped.getItemMeta().toString(), String.valueOf(plugin.getSettings().getItemMeta()))) {
            if (SpigotConfig.SETTINGS_DROP.get(Boolean.class)) event.setCancelled(true);
            return;
        }

        if (Objects.equals(dropped.getItemMeta().toString(), String.valueOf(BlockItem.getBlockItemStack(player).getItemMeta()))) {
            if (SpigotConfig.BLOCK_DROP.get(Boolean.class)) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerSwitch(InventoryClickEvent event) {

        final ItemStack item = event.getCurrentItem();
        final Player player = (Player) event.getWhoClicked();
        if (item == null || item.getItemMeta() == null) return;
        if (Objects.equals(item.getItemMeta().toString(), String.valueOf(plugin.getSettings().getItemMeta()))) {
            if (SpigotConfig.SETTINGS_MOVE.get(Boolean.class)) event.setCancelled(true);
            return;
        }

        if (Objects.equals(item.getItemMeta().toString(), String.valueOf(BlockItem.getBlockItemStack(player).getItemMeta()))) {
            if (SpigotConfig.BLOCK_MOVE.get(Boolean.class)) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (!player.getItemInHand().equals(plugin.getSettings())) return;
        event.setCancelled(true);
        GuiUtil.open(event.getPlayer());
    }
}
