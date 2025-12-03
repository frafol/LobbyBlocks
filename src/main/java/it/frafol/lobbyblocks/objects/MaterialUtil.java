package it.frafol.lobbyblocks.objects;

import com.cryptomorin.xseries.XMaterial;
import it.frafol.lobbyblocks.LobbyBlocks;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@UtilityClass
public class MaterialUtil {

    private final LobbyBlocks instance = LobbyBlocks.getInstance();

    public static Material fromString(String name) {
        if (name == null) return null;
        name = name.trim();
        XMaterial xMat = XMaterial.matchXMaterial(name).orElse(null);
        if (xMat == null) {
            instance.getLogger().severe("You are using an unsupported material in config.yml file: " + name + " does not exist!");
            return null;
        }
        ItemStack itemStack = xMat.parseItem();
        if (itemStack == null) {
            instance.getLogger().severe("You are using an unsupported material in config.yml file.");
            return null;
        }
        return itemStack.getType();
    }
}
