package it.frafol.lobbyblocks.enums;

import it.frafol.lobbyblocks.LobbyBlocks;
import it.frafol.lobbyblocks.objects.ChatUtil;

public enum SpigotMessages {

    PREFIX("messages.prefix"),

    PLAYER_ONLY("messages.player_only"),
    USAGE("messages.usage"),
    NO_PERMISSION("messages.no_permission"),
    NO_BLOCK_PERMISSION("messages.no_permission"),
    RELOADED("messages.reloaded"),

    POS1("messages.regions.pos1"),
    POS2("messages.regions.pos2"),
    CREATED("messages.regions.created"),
    NO_POSITIONS("messages.regions.no_positions"),
    NO_NAME("messages.regions.no_name"),
    CANNOT_PLACE("messages.regions.cannot_place"),

    SELECTED("messages.gui.selected");

    private final String path;
    public static final LobbyBlocks instance = LobbyBlocks.getInstance();

    SpigotMessages(String path) {
        this.path = path;
    }

    public String color() {
        return ChatUtil.color(get(String.class));
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getMessagesTextFile().get(path));
    }

}
