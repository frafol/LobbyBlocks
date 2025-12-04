package it.frafol.lobbyblocks.hooks.packetevents;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockBreakAnimation;
import it.frafol.lobbyblocks.LobbyBlocks;
import it.frafol.lobbyblocks.objects.PlayerCache;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class PEventsBreakAnimation {

    private final Map<Location, MyScheduledTask> runningAnimations = new ConcurrentHashMap<>();
    private final Map<Location, Integer> animationIds = new ConcurrentHashMap<>();

    public void startBlockAnimation(LobbyBlocks plugin, Block block, int seconds) {
        if (plugin.usingFolia()) {
            FoliaPEventsBreakAnimation.startBlockAnimationFolia(plugin, block, seconds);
            return;
        }
        Location loc = block.getLocation();
        resetAnimation(plugin, block);
        if (runningAnimations.containsKey(loc)) return;
        final int entityId = UUID.randomUUID().hashCode();
        animationIds.put(loc, entityId);
        MyScheduledTask task = UniversalScheduler.getScheduler(plugin).runTaskTimer(new Runnable() {
            int crack = 0;
            final int maxCrack = 9;
            @Override
            public void run() {
                if (block.getType() == Material.AIR) {
                    resetAnimation(plugin, block);
                    stopAnimation(loc);
                    return;
                }
                sendCrackPacket(plugin, block, crack, entityId);
                crack++;
                if (crack > maxCrack) {
                    PlayerCache.getBreaking().remove(block);
                    if (PlayerCache.getBreakingReplaced().get(block) == null) {
                        block.setType(Material.AIR);
                    } else {
                        block.setType(PlayerCache.getBreakingReplaced().get(block).getType());
                    }
                    PlayerCache.getBreakingReplaced().remove(block);
                    resetAnimation(plugin, block);
                    stopAnimation(loc);
                }
            }
        }, 0L, (seconds * 20L) / 10);

        runningAnimations.put(loc, task);
    }

    private void stopAnimation(Location loc) {
        MyScheduledTask task = runningAnimations.remove(loc);
        if (task != null) task.cancel();
        animationIds.remove(loc);
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
