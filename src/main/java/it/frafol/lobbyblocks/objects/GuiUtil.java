package it.frafol.lobbyblocks.objects;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import it.frafol.lobbyblocks.LobbyBlocks;
import it.frafol.lobbyblocks.enums.SpigotConfig;
import it.frafol.lobbyblocks.enums.SpigotMessages;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@UtilityClass
public class GuiUtil {

    private final LobbyBlocks instance = LobbyBlocks.getInstance();

    public void open(Player player) {
        if (instance.isLegacyServer()) {
            LegacyGuiUtil.open(player);
            return;
        }
        start(player);
    }

    private void start(Player player) {
        FileConfiguration cfg = instance.getGuiConfig();
        int size = cfg.getInt("gui.size", 9);
        String title = ChatUtil.color(cfg.getString("gui.title", "Menu"));
        int rows = size / 9;

        String[] structure = new String[rows];
        Arrays.fill(structure, ".........");

        Gui gui = Gui.normal().setStructure(structure).build();
        if (cfg.isConfigurationSection("gui.items")) {

            for (String key : Objects.requireNonNull(cfg.getConfigurationSection("gui.items")).getKeys(false)) {
                String base = "gui.items." + key;

                String matName = cfg.getString(base + ".material");
                if (matName == null) continue;

                Material mat = MaterialUtil.fromString(matName);
                if (mat == null) continue;

                String permission = cfg.getString(base + ".permission");
                if (permission != null && permission.equalsIgnoreCase("null")) {
                    permission = null;
                }

                String displayName = ChatUtil.color(cfg.getString(base + ".name"));
                List<String> lore = ChatUtil.color(cfg.getStringList(base + ".lore"));

                ItemBuilder builder = new ItemBuilder(mat);
                if (displayName != null) builder.setDisplayName(displayName);
                if (!lore.isEmpty()) builder.addLoreLines(lore.toArray(new String[0]));

                try {
                    int slot = Integer.parseInt(key);
                    gui.setItem(slot, new BlockSelectionItem(builder, mat, permission));
                } catch (NumberFormatException ignored) {}
            }
        }

        Window.single()
                .setViewer(player)
                .setTitle(title)
                .setGui(gui)
                .build()
                .open();
    }

    private static class BlockSelectionItem extends SimpleItem {

        private final Material material;
        private final String permission;

        public BlockSelectionItem(ItemProvider itemProvider, Material material, String permission) {
            super(itemProvider);
            this.material = material;
            this.permission = permission;
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {

            if (permission != null && !player.hasPermission(permission)) {
                player.sendMessage(SpigotMessages.NO_BLOCK_PERMISSION.color());
                return;
            }

            BlockItem.saveBlockItem(player, material);
            UniversalScheduler.getScheduler(instance).runTask(() -> {
                player.closeInventory();
                player.getInventory().setItem(
                        SpigotConfig.BLOCK_SLOT.get(Integer.class),
                        BlockItem.getBlockItemStack(player)
                );
                player.sendMessage(SpigotMessages.SELECTED.color()
                        .replace("%prefix%", SpigotMessages.PREFIX.color())
                        .replace("%block%", material.name()));
            });
        }
    }
}
