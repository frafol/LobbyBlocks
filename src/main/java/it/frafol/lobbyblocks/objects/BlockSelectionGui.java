package it.frafol.lobbyblocks.objects;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import it.frafol.lobbyblocks.LobbyBlocks;
import it.frafol.lobbyblocks.enums.SpigotConfig;
import it.frafol.lobbyblocks.enums.SpigotMessages;
import it.frafol.lobbyblocks.objects.gui.CustomGui;
import it.frafol.lobbyblocks.objects.gui.GuiCreator;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
public class BlockSelectionGui extends CustomGui {

    private final LobbyBlocks instance;
    private final FileConfiguration cfg;

    private static int getGuiSize() {
        return LobbyBlocks.getInstance().getGuiConfig().getInt("gui.size", 9);
    }

    private static String getGuiTitle() {
        return LobbyBlocks.getInstance().getGuiConfig().getString("gui.title", "Menu");
    }

    public BlockSelectionGui(Player player) {
        super(player,
                getGuiSize(),
                ChatUtil.color(getGuiTitle())
        );
        this.instance = LobbyBlocks.getInstance();
        this.cfg = instance.getGuiConfig();
        setupItems();
    }

    @Override
    protected void setupItems() {
        ConfigurationSection itemsSection = cfg.getConfigurationSection("gui.items");
        if (itemsSection == null) return;
        for (String key : itemsSection.getKeys(false)) {
            String base = "gui.items." + key;
            String matName = cfg.getString(base + ".material");
            if (matName == null) continue;
            Material mat = MaterialUtil.fromString(matName);
            if (mat == null) continue;
            String permission = cfg.getString(base + ".permission");
            if (permission != null && permission.equalsIgnoreCase("null")) permission = null;
            String displayName = ChatUtil.color(cfg.getString(base + ".name"));
            List<String> lore = ChatUtil.color(cfg.getStringList(base + ".lore"));
            ItemStack item = new ItemStack(mat);
            if (displayName != null) GuiCreator.setItemName(item, displayName);
            if (!lore.isEmpty()) GuiCreator.setItemLore(item, lore);
            try {
                int slot = Integer.parseInt(key);
                final String itemPermission = permission;
                setItemWithAction(slot, item, event -> {
                    if (itemPermission != null && !player.hasPermission(itemPermission)) {
                        player.sendMessage(SpigotMessages.NO_BLOCK_PERMISSION.color());
                        return;
                    }
                    BlockItem.saveBlockItem(player, mat);
                    UniversalScheduler.getScheduler(this.instance).runTask(() -> {
                        player.closeInventory();
                        Integer blockSlot = SpigotConfig.BLOCK_SLOT.get(Integer.class);
                        if (blockSlot != null) {
                            player.getInventory().setItem(
                                    blockSlot,
                                    BlockItem.getBlockItemStack(player)
                            );
                        }
                        player.sendMessage(SpigotMessages.SELECTED.color()
                                .replace("%prefix%", SpigotMessages.PREFIX.color())
                                .replace("%block%", mat.name()));
                    });
                });
            } catch (NumberFormatException ignored) {}
        }
    }
}