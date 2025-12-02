package it.frafol.lobbyblocks.hooks.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import it.frafol.lobbyblocks.LobbyBlocks;
import it.frafol.lobbyblocks.objects.PlayerCache;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class PLibBreakAnimation {

    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    private final Map<Location, MyScheduledTask> runningAnimations = new ConcurrentHashMap<>();
    private final Map<Location, Integer> animationIds = new ConcurrentHashMap<>();

    public void startBlockAnimation(LobbyBlocks plugin, Block block, int seconds) {
        Location loc = block.getLocation();
        resetAnimation(block);
        if (runningAnimations.containsKey(loc)) return;
        final int entityId = UUID.randomUUID().hashCode();
        animationIds.put(loc, entityId);
        MyScheduledTask task = UniversalScheduler.getScheduler(plugin).runTaskTimer(new Runnable() {
            int crack = 0;
            final int maxLevel = 9;
            @Override
            public void run() {
                if (block.getType() == Material.AIR) {
                    resetAnimation(block);
                    stopAnimation(loc);
                    return;
                }
                sendCrackAnimation(block, crack, entityId);
                crack++;
                if (crack > maxLevel) {
                    PlayerCache.getBreaking().remove(block);
                    if (PlayerCache.getBreakingReplaced().get(block) == null) {
                        block.setType(Material.AIR);
                    } else {
                        block.setType(PlayerCache.getBreakingReplaced().get(block).getType());
                    }
                    PlayerCache.getBreakingReplaced().remove(block);
                    resetAnimation(block);
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

    private void sendCrackAnimation(Block block, int crackLevel, int entityId) {
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
        packet.getIntegers().write(0, entityId);
        packet.getBlockPositionModifier().write(0,
                new BlockPosition(block.getX(), block.getY(), block.getZ()));
        packet.getIntegers().write(1, crackLevel);
        for (Player p : Bukkit.getOnlinePlayers()) {
            protocolManager.sendServerPacket(p, packet);
        }
    }

    private void resetAnimation(Block block) {
        Location loc = block.getLocation();
        Integer id = animationIds.get(loc);
        if (id == null) return;
        PacketContainer packet = protocolManager.createPacket(
                PacketType.Play.Server.BLOCK_BREAK_ANIMATION
        );
        packet.getIntegers().write(0, id);
        packet.getBlockPositionModifier().write(0,
                new BlockPosition(block.getX(), block.getY(), block.getZ()));
        packet.getIntegers().write(1, -1); // reset
        for (Player p : Bukkit.getOnlinePlayers()) {
            protocolManager.sendServerPacket(p, packet);
        }
    }
}
