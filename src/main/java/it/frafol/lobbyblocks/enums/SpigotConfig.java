package it.frafol.lobbyblocks.enums;

import it.frafol.lobbyblocks.LobbyBlocks;
import it.frafol.lobbyblocks.objects.ChatUtil;

import java.util.List;

public enum SpigotConfig {

    PERMISSION("settings.usage_permission"),
    MINIMESSAGE("settings.minimessage"),
    SETUP_PERMISSION("settings.setup_permission"),
    RELOAD_PERMISSION("settings.reload_permission"),
    CREDIT_LESS("settings.credit_less"),
    REGION_INVERT("settings.region_invert"),
    PREVENT_OTHER_BLOCKS("settings.prevent_other_blocks"),

    BREAKING_ANIMATION("settings.breaking_animation"),
    BREAKING_SECONDS("settings.breaking_seconds"),

    DELAY("settings.item.delay"),

    SETTINGS("settings.item.settings.enabled"),
    SETTINGS_SLOT("settings.item.settings.slot"),
    SETTINGS_TYPE("settings.item.settings.type"),
    SETTINGS_ITEMNAME("settings.item.settings.name"),
    SETTINGS_MODELDATA("settings.item.settings.modeldata"),
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
        return ChatUtil.color(get(String.class));
    }

    public List<String> getStringList() {
        return instance.getConfigTextFile().getStringList(path);
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getConfigTextFile().get(path));
    }

}