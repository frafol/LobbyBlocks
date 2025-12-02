package it.frafol.lobbyblocks.listeners;

import it.frafol.lobbyblocks.LobbyBlocks;
import it.frafol.lobbyblocks.enums.SpigotConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final LobbyBlocks plugin = LobbyBlocks.getInstance();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.startupPlayer(player);
        if (player.hasPermission(SpigotConfig.RELOAD_PERMISSION.get(String.class))) plugin.UpdateChecker(player);
    }
}
