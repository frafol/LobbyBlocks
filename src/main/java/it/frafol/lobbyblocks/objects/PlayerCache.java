package it.frafol.lobbyblocks.objects;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class PlayerCache {

    @Getter
    private final ConcurrentHashMap<UUID, Material> block = new ConcurrentHashMap<>();

    @Getter
    private Set<Block> breaking = ConcurrentHashMap.newKeySet();

    @Getter
    private Map<Block, Block> breakingReplaced = new ConcurrentHashMap<>();

    @Getter
    private Set<FallingBlock> breakingGravity = ConcurrentHashMap.newKeySet();
}