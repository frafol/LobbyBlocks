package it.frafol.lobbyblocks.commands;

import it.frafol.lobbyblocks.LobbyBlocks;
import it.frafol.lobbyblocks.enums.SpigotConfig;
import it.frafol.lobbyblocks.enums.SpigotMessages;
import it.frafol.lobbyblocks.objects.GuiUtil;
import it.frafol.lobbyblocks.objects.RegionUtil;
import it.frafol.lobbyblocks.objects.TextFile;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MainCommand implements CommandExecutor, TabCompleter {

    private final LobbyBlocks plugin = LobbyBlocks.getInstance();
    private final ConcurrentHashMap<UUID, Location> pos1Map = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Location> pos2Map = new ConcurrentHashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (args.length == 0) {
            handleDefaultCommand(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReloadCommand(sender);
                break;
            case "settings":
                handleSettingsCommand(sender);
                break;
            case "pos1":
                handlePos1Command(sender);
                break;
            case "pos2":
                handlePos2Command(sender);
                break;
            case "createregion":
                handleCreateCommand(sender, args);
                break;
            default:
                handleDefaultCommand(sender);
                break;
        }

        return true;
    }

    private void handleDefaultCommand(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission(SpigotConfig.RELOAD_PERMISSION.get(String.class))) {
                player.sendMessage(SpigotMessages.USAGE.color().replace("%prefix%", SpigotMessages.PREFIX.color()));
                return;
            }
        }

        if (SpigotConfig.CREDIT_LESS.get(Boolean.class)) {
            if (sender instanceof Player) {
                sender.sendMessage(SpigotMessages.NO_PERMISSION.color().replace("%prefix%", SpigotMessages.PREFIX.color()));
            }
            return;
        }

        sender.sendMessage("§7This server is using §dLobbyBlocks §7by §dfrafol§7.");
    }

    private void handleReloadCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            handleReload();
            sender.sendMessage(SpigotMessages.RELOADED.color().replace("%prefix%", SpigotMessages.PREFIX.color()));
            return;
        }

        Player player = (Player) sender;
        if (!player.hasPermission(SpigotConfig.RELOAD_PERMISSION.get(String.class))) {
            if (SpigotConfig.CREDIT_LESS.get(Boolean.class)) {
                player.sendMessage(SpigotMessages.NO_PERMISSION.color().replace("%prefix%", SpigotMessages.PREFIX.color()));
                return;
            }
            player.sendMessage("§7This server is using §dLobbyBlocks §7by §dfrafol§7.");
            return;
        }

        handleReload();
        player.sendMessage(SpigotMessages.RELOADED.color().replace("%prefix%", SpigotMessages.PREFIX.color()));
    }

    private void handleSettingsCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(SpigotMessages.PLAYER_ONLY.color().replace("%prefix%", SpigotMessages.PREFIX.color()));
            return;
        }

        Player player = (Player) sender;
        if (!player.hasPermission(SpigotConfig.PERMISSION.get(String.class))) {
            player.sendMessage(SpigotMessages.NO_PERMISSION.color().replace("%prefix%", SpigotMessages.PREFIX.color()));
            return;
        }

        GuiUtil.open(player);
    }

    private void handlePos1Command(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(SpigotMessages.PLAYER_ONLY.color().replace("%prefix%", SpigotMessages.PREFIX.color()));
            return;
        }

        Player player = (Player) sender;
        if (!player.hasPermission(SpigotConfig.SETUP_PERMISSION.get(String.class))) {
            player.sendMessage(SpigotMessages.NO_PERMISSION.color().replace("%prefix%", SpigotMessages.PREFIX.color()));
            return;
        }

        pos1Map.put(player.getUniqueId(), player.getLocation());
        player.sendMessage(SpigotMessages.POS1.color().replace("%prefix%", SpigotMessages.PREFIX.color()));
    }

    private void handlePos2Command(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(SpigotMessages.PLAYER_ONLY.color().replace("%prefix%", SpigotMessages.PREFIX.color()));
            return;
        }

        Player player = (Player) sender;
        if (!player.hasPermission(SpigotConfig.SETUP_PERMISSION.get(String.class))) {
            player.sendMessage(SpigotMessages.NO_PERMISSION.color().replace("%prefix%", SpigotMessages.PREFIX.color()));
            return;
        }

        pos2Map.put(player.getUniqueId(), player.getLocation());
        player.sendMessage(SpigotMessages.POS2.color().replace("%prefix%", SpigotMessages.PREFIX.color()));
    }

    private void handleCreateCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(SpigotMessages.PLAYER_ONLY.color().replace("%prefix%", SpigotMessages.PREFIX.color()));
            return;
        }

        Player player = (Player) sender;
        if (!player.hasPermission(SpigotConfig.SETUP_PERMISSION.get(String.class))) {
            player.sendMessage(SpigotMessages.NO_PERMISSION.color().replace("%prefix%", SpigotMessages.PREFIX.color()));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(SpigotMessages.NO_NAME.color().replace("%prefix%", SpigotMessages.PREFIX.color()));
            return;
        }

        String regionName = args[1];
        UUID uuid = player.getUniqueId();

        Location p1 = pos1Map.get(uuid);
        Location p2 = pos2Map.get(uuid);
        if (p1 == null || p2 == null) {
            player.sendMessage(SpigotMessages.NO_POSITIONS.color().replace("%prefix%", SpigotMessages.PREFIX.color()));
            return;
        }

        RegionUtil.saveRegion(regionName, p1, p2);
        plugin.saveDatabases();
        pos1Map.remove(uuid);
        pos2Map.remove(uuid);
        player.sendMessage(SpigotMessages.CREATED.color()
                .replace("%prefix%", SpigotMessages.PREFIX.color())
                .replace("%name%", regionName));
    }

    private void handleReload() {
        TextFile.reloadAll();
        if (!plugin.getServer().getOnlinePlayers().isEmpty()) {
            for (Player players : plugin.getServer().getOnlinePlayers()) plugin.startupPlayer(players);
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) return Arrays.asList("reload", "settings", "pos1", "pos2", "createregion");
        return Collections.emptyList();
    }
}