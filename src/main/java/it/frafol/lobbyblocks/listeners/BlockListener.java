package it.frafol.lobbyblocks.listeners;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import it.frafol.lobbyblocks.LobbyBlocks;
import it.frafol.lobbyblocks.enums.SpigotConfig;
import it.frafol.lobbyblocks.hooks.packetevents.PEventsBreakAnimation;
import it.frafol.lobbyblocks.hooks.protocollib.PLibBreakAnimation;
import it.frafol.lobbyblocks.objects.BlockItem;
import it.frafol.lobbyblocks.objects.PlayerCache;
import it.frafol.lobbyblocks.objects.RegionUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;

public class BlockListener implements Listener {

    private final LobbyBlocks plugin = LobbyBlocks.getInstance();

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack block = player.getItemInHand();

        if (!block.getType().equals(BlockItem.getBlockItemStack(player).getType())) return;
        if (player.getInventory().getHeldItemSlot() != SpigotConfig.BLOCK_SLOT.get(Integer.class)) return;

        try {
            if (!event.getBlockReplacedState().getBlockData().getMaterial().equals(Material.AIR)) {
                event.setCancelled(true);
                return;
            }
        } catch (NoSuchMethodError ignored) {
            PlayerCache.getBreakingReplaced().put(event.getBlockPlaced(), event.getBlockReplacedState().getBlock());
        }

        if (!canContinue(event)) return;
        Block placedBlock = event.getBlockPlaced();
        PlayerCache.getBreaking().add(placedBlock);
        event.setCancelled(false);
        UniversalScheduler.getScheduler(plugin).runTask(() -> {
            block.setAmount(64);
            player.setItemInHand(block);
        });

        if (block.getType().hasGravity()) {
            placedBlock.setType(Material.AIR);
            setNoGravity(placedBlock);
        }
        startRemovalTask(placedBlock);
    }

    @EventHandler
    private void onPhysics(BlockPhysicsEvent event) {
        if (PlayerCache.getBreaking().contains(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFall(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof FallingBlock) {
            if (PlayerCache.getBreaking().contains(event.getBlock())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void onBreak(BlockBreakEvent event) {
        if (PlayerCache.getBreaking().contains(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    private boolean canContinue(BlockPlaceEvent event) {
        if (!RegionUtil.isInARegion(event.getBlock().getLocation())) {
            if (SpigotConfig.REGION_INVERT.get(Boolean.class)) {
                event.setCancelled(true);
                PlayerCache.getBreakingReplaced().remove(event.getBlockPlaced());
                return false;
            }
        } else {
            if (!SpigotConfig.REGION_INVERT.get(Boolean.class)) {
                event.setCancelled(true);
                PlayerCache.getBreakingReplaced().remove(event.getBlockPlaced());
                return false;
            }
        }
        return true;
    }

    private void startRemovalTask(Block block) {
        final int removalTime = SpigotConfig.BREAKING_SECONDS.get(Integer.class);
        if (plugin.isPacketevents()) {
            PEventsBreakAnimation.startBlockAnimation(plugin, block, removalTime);
            return;
        }
        if (plugin.isProtocollib()) {
            PLibBreakAnimation.startBlockAnimation(plugin, block, removalTime);
            return;
        }
        removeTask(block, removalTime);
    }

    private void removeTask(Block block, int removal) {
        if (!plugin.usingFolia()) {
            UniversalScheduler.getScheduler(plugin).runTaskLater(() -> {
                block.setType(Material.AIR);
                PlayerCache.getBreakingReplaced().remove(block);
                PlayerCache.getBreaking().remove(block);
            }, 20L * removal);
            return;
        }

        plugin.getServer().getRegionScheduler().runDelayed(plugin, block.getLocation(), task -> {
            block.setType(Material.AIR);
            PlayerCache.getBreakingReplaced().remove(block);
            PlayerCache.getBreaking().remove(block);
        }, 20L * removal);
    }

    private void setNoGravity(Block block) {
        if (!plugin.usingFolia()) UniversalScheduler.getScheduler(plugin).runTask(() -> block.setType(Material.SAND, false));
        else plugin.getServer().getRegionScheduler().run(plugin, block.getLocation(), task -> block.setType(Material.SAND, false));
    }
}