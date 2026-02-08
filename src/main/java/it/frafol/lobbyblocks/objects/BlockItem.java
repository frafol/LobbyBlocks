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
        if (getDefaultMaterial() == null) {
            instance.getLogger().severe("You did not configure any block! Please resolve the issue if you don't want errors.");
            block = new ItemStack(Material.AIR);
            return;
        }
        block = new ItemStack(getDefaultMaterial());
        ItemMeta blockMeta = block.getItemMeta();
        if (!SpigotConfig.BLOCK_ITEMNAME.get(String.class).equals("none")) blockMeta.setDisplayName(SpigotConfig.BLOCK_ITEMNAME.color());
        block.setAmount(64);
        blockMeta.setCustomModelData(getModelData(getDefaultMaterial()));
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
        ItemMeta meta = block.getItemMeta();
        if (!SpigotConfig.BLOCK_ITEMNAME.get(String.class).equals("none")) meta.setDisplayName(SpigotConfig.BLOCK_ITEMNAME.color());
        meta.setCustomModelData(getModelData(dataBlock));
        blockStack.setItemMeta(meta);
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

    public int getModelData(Material material) {
        if (!instance.getGuiConfig().isConfigurationSection("gui.items")) return 0;
        for (String key : instance.getGuiConfig().getConfigurationSection("gui.items").getKeys(false)) {
            String basePath = "gui.items." + key;
            String matName = instance.getGuiConfig().getString(basePath + ".material");
            if (matName == null) continue;
            Material configMat = Material.matchMaterial(matName);
            if (configMat != null && configMat == material) {
                return instance.getGuiConfig().getInt(basePath + ".modeldata", 0);
            }
        }
        return 0;
    }
}
