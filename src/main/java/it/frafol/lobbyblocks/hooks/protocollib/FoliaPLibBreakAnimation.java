package it.frafol.lobbyblocks.hooks.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import it.frafol.lobbyblocks.LobbyBlocks;
import it.frafol.lobbyblocks.objects.PlayerCache;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class FoliaPLibBreakAnimation {

    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    private final Map<Location, ScheduledTask> tasks = new ConcurrentHashMap<>();
    private final Map<Location, Integer> animationIds = new ConcurrentHashMap<>();
    private final Map<Location, Integer> crackLevels = new ConcurrentHashMap<>();

    public void startBlockAnimation(LobbyBlocks plugin, Block block, int seconds) {
        Location loc = block.getLocation();
        if (tasks.containsKey(loc)) return;
        final int entityId = UUID.randomUUID().hashCode();
        animationIds.put(loc, entityId);
        crackLevels.put(loc, 0);
        long period = (seconds * 20L) / 10;
        World world = block.getWorld();
        RegionScheduler scheduler = Bukkit.getServer().getRegionScheduler();
        ScheduledTask task = scheduler.runAtFixedRate(
                plugin,
                world,
                block.getChunk().getX(),
                block.getChunk().getZ(),
                scheduled -> {
                    Block b = world.getBlockAt(loc);
                    Integer crack = crackLevels.getOrDefault(loc, 0);

                    if (b.getType() == Material.AIR) {
                        cancel(loc);
                        return;
                    }

                    sendCrackPacket(b, crack, entityId);
                    crack++;

                    if (crack > 9) {
                        PlayerCache.getBreaking().remove(b);
                        if (PlayerCache.getBreakingReplaced().get(b) == null) {
                            b.setType(Material.AIR, false);
                        } else {
                            b.setType(PlayerCache.getBreakingReplaced().get(b).getType());
                        }
                        PlayerCache.getBreakingReplaced().remove(b);
                        resetAnimation(b, entityId);
                        cancel(loc);
                    } else {
                        crackLevels.put(loc, crack);
                    }
                },
                1L,
                period
        );

        tasks.put(loc, task);
    }

    private void cancel(Location loc) {
        ScheduledTask t = tasks.remove(loc);
        if (t != null) t.cancel();
        animationIds.remove(loc);
        crackLevels.remove(loc);
    }

    private void sendCrackPacket(Block block, int crackLevel, int entityId) {
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
        packet.getIntegers().write(0, entityId);
        packet.getBlockPositionModifier().write(0,
                new BlockPosition(block.getX(), block.getY(), block.getZ()));
        packet.getIntegers().write(1, crackLevel);
        for (Player p : Bukkit.getOnlinePlayers()) {
            protocolManager.sendServerPacket(p, packet);
        }
    }

    private void resetAnimation(Block block, int entityId) {
        PacketContainer pkt = protocolManager.createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
        pkt.getIntegers().write(0, entityId);
        pkt.getBlockPositionModifier().write(0,
                new BlockPosition(block.getX(), block.getY(), block.getZ()));
        pkt.getIntegers().write(1, -1);
        for (Player p : Bukkit.getOnlinePlayers()) {
            protocolManager.sendServerPacket(p, pkt);
        }
    }
}
