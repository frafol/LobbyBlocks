package it.frafol.lobbyblocks.objects;

import it.frafol.lobbyblocks.LobbyBlocks;
import it.frafol.lobbyblocks.objects.gui.GuiListener;
import it.frafol.lobbyblocks.objects.gui.LegacyGuiUtil;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

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

    public void setupEvents() {
        instance.getServer().getPluginManager().registerEvents(new GuiListener(), instance);
    }

    private void start(Player player) {
        new BlockSelectionGui(player).open();
    }
}
