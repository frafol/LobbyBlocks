package it.frafol.lobbyblocks.objects;

import it.frafol.lobbyblocks.LobbyBlocks;
import it.frafol.lobbyblocks.enums.SpigotConfig;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;
import java.util.UUID;

@UtilityClass
public class BlockItem {

    private final LobbyBlocks instance = LobbyBlocks.getInstance();

    @Getter
    private ItemStack block;

    public void loadBlock() {
        block = new ItemStack(Material.AIR);
        ItemMeta blockMeta = block.getItemMeta();
        if (blockMeta == null) return;
        blockMeta.setDisplayName(SpigotConfig.BLOCK_ITEMNAME.color());
        block.setAmount(64);
        block.setItemMeta(blockMeta);
    }

    public ItemStack getBlockItemStack(Player player) {
        UUID uuid = player.getUniqueId();
        String path = "database." + uuid + ".material";
        String matName = instance.getDatabaseConfig().getString(path);
        if (matName == null && getDefaultMaterial() != null) return setupPlayer(player);
        Material dataBlock = MaterialUtil.fromString(matName);
        if (dataBlock == null) return null;
        ItemStack blockStack = new ItemStack(dataBlock);
        blockStack.setAmount(64);
        blockStack.setItemMeta(block.getItemMeta());
        PlayerCache.getBlock().put(player.getUniqueId(), dataBlock);
        return blockStack;
    }

    public void saveBlockItem(Player player, Material material) {
        UUID uuid = player.getUniqueId();
        String path = "database." + uuid + ".material";
        instance.getDatabaseConfig().set(path, material.name());
        instance.saveDatabases();
    }

    private ItemStack setupPlayer(Player player) {
        ItemStack blockStack = new ItemStack(Objects.requireNonNull(getDefaultMaterial()));
        blockStack.setItemMeta(block.getItemMeta());
        PlayerCache.getBlock().put(player.getUniqueId(), blockStack.getType());
        return blockStack;
    }

    private Material getDefaultMaterial() {
        if (!instance.getGuiConfig().isConfigurationSection("gui.items")) return null;
        for (String key : Objects.requireNonNull(instance.getGuiConfig().getConfigurationSection("gui.items")).getKeys(false)) {
            String path = "gui.items." + key + ".material";
            if (!instance.getGuiConfig().contains(path)) continue;
            String matName = instance.getGuiConfig().getString(path);
            if (matName == null) continue;
            Material mat = Material.matchMaterial(matName);
            if (mat != null) {
                return mat;
            }
        }
        return null;
    }
}
