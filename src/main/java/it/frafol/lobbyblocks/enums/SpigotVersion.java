package it.frafol.lobbyblocks.enums;

import it.frafol.lobbyblocks.LobbyBlocks;

public enum SpigotVersion {

    VERSION("version");

    private final String path;
    public static final LobbyBlocks instance = LobbyBlocks.getInstance();

    SpigotVersion(String path) {
        this.path = path;
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getVersionTextFile().get(path));
    }

}