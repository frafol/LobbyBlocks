package it.frafol.lobbyblocks.enums;

import it.frafol.lobbyblocks.LobbyBlocks;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum SpigotConfig {

    PERMISSION("settings.usage_permission"),
    MINIMESSAGE("settings.minimessage"),
    SETUP_PERMISSION("settings.setup_permission"),
    RELOAD_PERMISSION("settings.reload_permission"),
    CREDIT_LESS("settings.credit_less"),
    REGION_INVERT("settings.region_invert"),

    BREAKING_ANIMATION("settings.breaking_animation"),
    BREAKING_SECONDS("settings.breaking_seconds"),

    DELAY("settings.item.delay"),

    SETTINGS_SLOT("settings.item.settings.slot"),
    SETTINGS_TYPE("settings.item.settings.type"),
    SETTINGS_ITEMNAME("settings.item.settings.name"),
    SETTINGS_DROP("settings.item.settings.prevent_drop"),
    SETTINGS_MOVE("settings.item.settings.prevent_move"),
    SETTINGS_LORE("settings.item.settings.lore"),

    BLOCK_SLOT("settings.item.block.slot"),
    BLOCK_ITEMNAME("settings.item.block.name"),
    BLOCK_DROP("settings.item.block.prevent_drop"),
    BLOCK_MOVE("settings.item.block.prevent_move"),

    UPDATE_CHECK("settings.update_check"),
    AUTO_UPDATE("settings.auto_update"),
    STATS("settings.stats");

    private final String path;
    public static final LobbyBlocks instance = LobbyBlocks.getInstance();

    SpigotConfig(String path) {
        this.path = path;
    }

    public String color() {
        String hex = convertHexColors(get(String.class));
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

    public List<String> getStringList() {
        return instance.getConfigTextFile().getStringList(path);
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getConfigTextFile().get(path));
    }

}