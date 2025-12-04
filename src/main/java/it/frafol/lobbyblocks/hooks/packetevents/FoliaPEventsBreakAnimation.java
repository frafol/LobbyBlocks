package it.frafol.lobbyblocks.hooks.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockBreakAnimation;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import it.frafol.lobbyblocks.LobbyBlocks;
import it.frafol.lobbyblocks.objects.PlayerCache;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class FoliaPEventsBreakAnimation {

    private final Map<Location, ScheduledTask> runningTasks = new ConcurrentHashMap<>();
    private final Map<Location, Integer> animationIds = new ConcurrentHashMap<>();
    private final Map<Location, Integer> crackLevels = new ConcurrentHashMap<>();

    public void startBlockAnimationFolia(LobbyBlocks plugin, Block block, int seconds) {
        Location loc = block.getLocation();
        resetAnimation(plugin, block);
        if (runningTasks.containsKey(loc)) return;
        final int entityId = UUID.randomUUID().hashCode();
        animationIds.put(loc, entityId);
        crackLevels.put(loc, 0);
        long period = (seconds * 20L) / 10;
        RegionScheduler regionScheduler = plugin.getServer().getRegionScheduler();
        World world = block.getWorld();
        ScheduledTask task = regionScheduler.runAtFixedRate(
                plugin,
                world,
                block.getChunk().getX(),
                block.getChunk().getZ(),
                scheduled -> {
                    Block b = world.getBlockAt(loc);
                    Integer crack = crackLevels.getOrDefault(loc, 0);
                    if (b.getType() == Material.AIR) {
                        resetAnimation(plugin, b);
                        scheduled.cancel();
                        runningTasks.remove(loc);
                        animationIds.remove(loc);
                        crackLevels.remove(loc);
                        return;
                    }

                    sendCrackPacket(plugin, b, crack, entityId);
                    crack++;

                    if (crack > 9) {
                        PlayerCache.getBreaking().remove(b);
                        if (PlayerCache.getBreakingReplaced().get(b) == null) {
                            b.setType(Material.AIR);
                        } else {
                            b.setType(PlayerCache.getBreakingReplaced().get(b).getType());
                        }
                        PlayerCache.getBreakingReplaced().remove(b);
                        resetAnimation(plugin, b);
                        scheduled.cancel();
                        runningTasks.remove(loc);
                        animationIds.remove(loc);
                        crackLevels.remove(loc);
                    } else {
                        crackLevels.put(loc, crack);
                    }
                },
                1L,
                period
        );

        runningTasks.put(loc, task);
    }

    private void sendCrackPacket(LobbyBlocks plugin, Block block, int crackLevel, int entityId) {
        Vector3i pos = new Vector3i(block.getX(), block.getY(), block.getZ());
        WrapperPlayServerBlockBreakAnimation packet =
                new WrapperPlayServerBlockBreakAnimation(entityId, pos, (byte) crackLevel);
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(p, packet);
        }
    }

    private void resetAnimation(LobbyBlocks plugin, Block block) {
        Location loc = block.getLocation();
        Integer id = animationIds.get(loc);
        if (id == null) return;
        Vector3i pos = new Vector3i(block.getX(), block.getY(), block.getZ());
        WrapperPlayServerBlockBreakAnimation resetPacket =
                new WrapperPlayServerBlockBreakAnimation(id, pos, (byte) -1);
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(p, resetPacket);
        }
    }
}

