package it.frafol.lobbyblocks.listeners;

import it.frafol.lobbyblocks.objects.BlockItem;
import it.frafol.lobbyblocks.objects.PlayerCache;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveListener implements Listener {

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (PlayerCache.getBlock().get(player.getUniqueId()) == null) return;
        BlockItem.saveBlockItem(player, PlayerCache.getBlock().get(player.getUniqueId()));
    }
}
