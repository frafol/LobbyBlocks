package it.frafol.lobbyblocks.objects;

import it.frafol.lobbyblocks.LobbyBlocks;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

@UtilityClass
public class RegionUtil {

    private final LobbyBlocks plugin = LobbyBlocks.getInstance();

    public void saveRegion(String regionKey, Location pos1, Location pos2) {
        if (pos1.getWorld() == null || pos2.getWorld() == null) return;
        if (!pos1.getWorld().equals(pos2.getWorld())) throw new IllegalArgumentException("Le due posizioni devono essere nello stesso mondo!");
        String base = "regions." + regionKey;
        plugin.getRegionConfig().set(base + ".world", pos1.getWorld().getName());
        plugin.getRegionConfig().set(base + ".x1", pos1.getX());
        plugin.getRegionConfig().set(base + ".y1", pos1.getY());
        plugin.getRegionConfig().set(base + ".z1", pos1.getZ());
        plugin.getRegionConfig().set(base + ".x2", pos2.getX());
        plugin.getRegionConfig().set(base + ".y2", pos2.getY());
        plugin.getRegionConfig().set(base + ".z2", pos2.getZ());
    }

    public boolean isInARegion(Location loc) {
        FileConfiguration cfg = plugin.getRegionConfig();
        if (loc == null || loc.getWorld() == null) return false;
        String worldName = loc.getWorld().getName();
        if (!cfg.isConfigurationSection("regions")) return false;
        for (String regionKey : cfg.getConfigurationSection("regions").getKeys(false)) {
            String base = "regions." + regionKey;
            String regionWorld = cfg.getString(base + ".world");
            if (regionWorld == null || !regionWorld.equals(worldName)) continue;
            double x1 = cfg.getDouble(base + ".x1");
            double y1 = cfg.getDouble(base + ".y1");
            double z1 = cfg.getDouble(base + ".z1");
            double x2 = cfg.getDouble(base + ".x2");
            double y2 = cfg.getDouble(base + ".y2");
            double z2 = cfg.getDouble(base + ".z2");
            double minX = Math.min(x1, x2);
            double maxX = Math.max(x1, x2);
            double minY = Math.min(y1, y2);
            double maxY = Math.max(y1, y2);
            double minZ = Math.min(z1, z2);
            double maxZ = Math.max(z1, z2);
            double x = loc.getX();
            double y = loc.getY();
            double z = loc.getZ();
            if (x >= minX && x <= maxX
                    && y >= minY && y <= maxY
                    && z >= minZ && z <= maxZ) {
                return true;
            }
        }
        return false;
    }
}
