package it.frafol.lobbyblocks.objects;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.samjakob.spigui.SpiGUI;
import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.menu.SGMenu;
import it.frafol.lobbyblocks.LobbyBlocks;
import it.frafol.lobbyblocks.enums.SpigotConfig;
import it.frafol.lobbyblocks.enums.SpigotMessages;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;

@UtilityClass
public class LegacyGuiUtil {

    private final LobbyBlocks instance = LobbyBlocks.getInstance();
    private SGMenu menu;

    public void createGUIMenu() {
        FileConfiguration cfg = instance.getGuiConfig();
        int size = cfg.getInt("gui.size", 9);
        String title = ChatUtil.color(cfg.getString("gui.title", "Menu"));
        menu = new SpiGUI(instance).create(title, size / 9);

        for (String key : Objects.requireNonNull(cfg.getConfigurationSection("gui.items")).getKeys(false)) {
            String base = "gui.items." + key;

            String matName = cfg.getString(base + ".material");
            if (matName == null) continue;

            Material mat = MaterialUtil.fromString(matName);
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                String displayName = ChatUtil.color(cfg.getString(base + ".name"));
                if (displayName != null) meta.setDisplayName(displayName);
                List<String> lore = ChatUtil.color(cfg.getStringList(base + ".lore"));
                if (!lore.isEmpty()) meta.setLore(lore);
                item.setItemMeta(meta);
            }

            String permission = cfg.getString(base + ".permission");
            if (permission != null && permission.equalsIgnoreCase("null")) {
                permission = null;
            }

            final String finalPermission = permission;
            SGButton btn = new SGButton(item).withListener((InventoryClickEvent ev) -> {
                ev.setCancelled(true);
                Player player = (Player) ev.getWhoClicked();
                if (finalPermission != null && !player.hasPermission(finalPermission)) {
                    player.sendMessage(SpigotMessages.NO_BLOCK_PERMISSION.color());
                    return;
                }

                BlockItem.saveBlockItem(player, item.getType());
                UniversalScheduler.getScheduler(instance).runTask(() -> {
                    player.closeInventory();
                    player.getInventory().setItem(
                            SpigotConfig.BLOCK_SLOT.get(Integer.class),
                            BlockItem.getBlockItemStack(player)
                    );
                    player.sendMessage(
                            SpigotMessages.SELECTED.color()
                                    .replace("%prefix%", SpigotMessages.PREFIX.color())
                                    .replace("%block%", item.getType().name())
                    );
                });
            });

            menu.setButton(Integer.parseInt(key), btn);
        }
    }

    public void open(Player player) {
        player.openInventory(menu.getInventory());
    }
}
