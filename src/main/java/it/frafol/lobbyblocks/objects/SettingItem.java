package it.frafol.lobbyblocks.objects;

import it.frafol.lobbyblocks.enums.SpigotConfig;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class SettingItem {

    @Getter
    private ItemStack settings;

    public void loadSettings() {
        settings = new ItemStack(MaterialUtil.fromString(SpigotConfig.SETTINGS_TYPE.color()));
        ItemMeta settingsMeta = settings.getItemMeta();
        if (settingsMeta == null) return;
        settingsMeta.setDisplayName(SpigotConfig.SETTINGS_ITEMNAME.color());
        settingsMeta.setLore(getLore(SpigotConfig.SETTINGS_LORE.getStringList()));
        settings.setItemMeta(settingsMeta);
    }

    private List<String> getLore(List<String> list) {
        List<String> finalList = new ArrayList<>();
        for (String string : list) {
            color(string);
            finalList.add(color(string));
        }
        return finalList;
    }

    public String color(String string) {
        String hex = convertHexColors(string);
        return hex.replace("&", "§");
    }

    private String convertHexColors(String message) {

        if (!containsHexColor(message)) {
            return message;
        }

        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch) {
                builder.append("&").append(c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        return message;
    }

    private boolean containsHexColor(String message) {
        String hexColorPattern = "(?i)&#[a-f0-9]{6}";
        return message.matches(".*" + hexColorPattern + ".*");
    }
}
