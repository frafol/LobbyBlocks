package it.frafol.lobbyblocks;

import com.cryptomorin.xseries.XMaterial;
import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tchristofferson.configupdater.ConfigUpdater;
import it.frafol.lobbyblocks.commands.MainCommand;
import it.frafol.lobbyblocks.enums.SpigotConfig;
import it.frafol.lobbyblocks.enums.SpigotVersion;
import it.frafol.lobbyblocks.listeners.BlockListener;
import it.frafol.lobbyblocks.listeners.ItemListener;
import it.frafol.lobbyblocks.listeners.JoinListener;
import it.frafol.lobbyblocks.listeners.LeaveListener;
import it.frafol.lobbyblocks.objects.*;
import lombok.Getter;
import lombok.SneakyThrows;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import net.byteflux.libby.relocation.Relocation;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class LobbyBlocks extends JavaPlugin {

	private TextFile configTextFile;
	private TextFile messagesTextFile;
	private TextFile versionTextFile;

    private File regionsFile;
    private File databaseFile;
    private File guiFile;

	private boolean updated = false;
    private boolean update = false;

	@Getter
	public static LobbyBlocks instance;

    @Getter
    private boolean protocollib = false;

    @Getter
    private boolean packetevents = false;

    @Getter
    private FileConfiguration databaseConfig;

    @Getter
    private FileConfiguration regionConfig;

    @Getter
    private FileConfiguration guiConfig;

	@Getter
	private ItemStack settings;

	@Override
	public void onEnable() {

		instance = this;

        getLogger().info("\n __    _____  ____  ____  _  _    ____  __    _____  ___  _  _  ___ \n" +
                "(  )  (  _  )(  _ \\(  _ \\( \\/ )  (  _ \\(  )  (  _  )/ __)( )/ )/ __)\n" +
                " )(__  )(_)(  ) _ < ) _ < \\  /    ) _ < )(__  )(_)(( (__  )  ( \\__ \\\n" +
                "(____)(_____)(____/(____/ (__)   (____/(____)(_____)\\___)(_)\\_)(___/\n");

		loadDependencies();
		checkSupportedVersion();

		loadConfigurations();
		updateConfiguration();
        loadHooks();
        loadDatabases();

		getLogger().info("Loading commands...");
        getCommand("lobbyblocks").setExecutor(new MainCommand());
        getCommand("lobbyblocks").setTabCompleter(new MainCommand());

		getLogger().info("Loading events...");
		getServer().getPluginManager().registerEvents(new JoinListener(), this);
		getServer().getPluginManager().registerEvents(new ItemListener(), this);
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
		getServer().getPluginManager().registerEvents(new LeaveListener(), this);

		getLogger().info("Loading tasks...");

		if (SpigotConfig.STATS.get(Boolean.class)) {
			new Metrics(this, 28134);
			getLogger().info("Metrics loaded successfully!");
		}

		if (SpigotConfig.UPDATE_CHECK.get(Boolean.class)) {
			UniversalScheduler.getScheduler(this).runTaskTimer(this::UpdateChecker, 20L, 3600 * 20L);
		}

		SettingItem.loadSettings();
        settings = SettingItem.getSettings();
        BlockItem.loadBlock();
        if (isLegacyServer()) {
            getLogger().warning("Detected a Legacy server, please consider upgrading to new versions.");
            LegacyGuiUtil.createGUIMenu();
        }
		if (!getServer().getOnlinePlayers().isEmpty()) for (Player players : getServer().getOnlinePlayers()) startupPlayer(players);
		getLogger().info("Plugin successfully loaded!");
	}

	private void loadDependencies() {

		BukkitLibraryManager bukkitLibraryManager = new BukkitLibraryManager(this);
		bukkitLibraryManager.addJitPack();

		final Relocation yamlrelocation = new Relocation("yaml", "it{}frafol{}libs{}yaml");
		Library yaml = Library.builder()
				.groupId("me{}carleslc{}Simple-YAML")
				.artifactId("Simple-Yaml")
				.version("1.8.4")
				.url("https://github.com/Carleslc/Simple-YAML/releases/download/1.8.4/Simple-Yaml-1.8.4.jar")
				.relocate(yamlrelocation)
				.build();

		final Relocation updaterelocation = new Relocation("updater", "it{}frafol{}libs{}updater");
		Library updater = Library.builder()
				.groupId("com{}tchristofferson")
				.artifactId("ConfigUpdater")
				.version("2.1-SNAPSHOT")
				.url("https://github.com/frafol/Config-Updater/releases/download/compile/ConfigUpdater-2.1-SNAPSHOT.jar")
				.relocate(updaterelocation)
				.build();

		final Relocation schedulerrelocation = new Relocation("scheduler", "it{}frafol{}libs{}scheduler");
		Library scheduler = Library.builder()
				.groupId("com{}github{}Anon8281")
				.artifactId("UniversalScheduler")
				.version("0.1.6")
				.relocate(schedulerrelocation)
				.build();

        final Relocation legacyguirelocation = new Relocation("spigui", "it{}frafol{}libs{}spigui");
        Library legacygui = Library.builder()
                .groupId("com{}samjakob")
                .artifactId("SpiGUI")
                .version("1.4.1")
                .relocate(legacyguirelocation)
                .build();

        final Relocation xseriesrelocation = new Relocation("com{}github{}cryptomorin", "it{}frafol{}libs{}xseries");
        Library xseries = Library.builder()
                .groupId("com{}github{}cryptomorin")
                .artifactId("XSeries")
                .version(fetchXSeriesVersion())
                .relocate(xseriesrelocation)
                .build();

		bukkitLibraryManager.loadLibrary(yaml);
        bukkitLibraryManager.loadLibrary(xseries);
        bukkitLibraryManager.loadLibrary(legacygui);
		bukkitLibraryManager.loadLibrary(scheduler);
		bukkitLibraryManager.loadLibrary(updater);
	}

    @SuppressWarnings("deprecation")
    public static String fetchXSeriesVersion() {
        final String fallbackVersion = "13.5.1";
        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://api.github.com/repos/CryptoMorin/XSeries/releases/latest");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            if (conn.getResponseCode() != 200) return fallbackVersion;
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) sb.append(line);
            in.close();
            String json = sb.toString();
            JsonParser parser = new JsonParser();
            JsonElement root = parser.parse(json);
            if (root.isJsonObject()) {
                JsonObject obj = root.getAsJsonObject();
                if (obj.has("tag_name")) {
                    String tag = obj.get("tag_name").getAsString();
                    if (tag.startsWith("v") || tag.startsWith("V")) {
                        return tag.substring(1);
                    } else {
                        return tag;
                    }
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return fallbackVersion;
    }

	private void checkSupportedVersion() {
		getLogger().info("Server version: " + getServer().getBukkitVersion() + ".");
		if (getServer().getBukkitVersion().startsWith("1.7.")
                || getServer().getBukkitVersion().startsWith("1.6.")
				|| getServer().getBukkitVersion().startsWith("1.5.")
				|| getServer().getBukkitVersion().startsWith("1.4.")
				|| getServer().getBukkitVersion().startsWith("1.3.")) {
			getLogger().severe("Support for your version was declined.");
			getServer().getPluginManager().disablePlugin(this);
		}
	}

	private void loadConfigurations() {
		getLogger().info("Loading configuration...");
		configTextFile = new TextFile(getDataFolder().toPath(), "config.yml");
		messagesTextFile = new TextFile(getDataFolder().toPath(), "messages.yml");
		versionTextFile = new TextFile(getDataFolder().toPath(), "version.yml");
        regionsFile = new File(getDataFolder(), "regions.yml");
        databaseFile = new File(getDataFolder(), "database.yml");
        guiFile = new File(getDataFolder(), "gui.yml");
	}

    private FileConfiguration startDatabase(File file) {
        if (file.exists()) return YamlConfiguration.loadConfiguration(file);
        saveResource(file.getName(), false);
        return YamlConfiguration.loadConfiguration(file);
    }

    private void loadHooks() {
        if (getServer().getPluginManager().isPluginEnabled("packetevents")) {
            packetevents = true;
            getLogger().info("Hooked in PacketEvents successfully.");
            return;
        }
        if (getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            protocollib = true;
            getLogger().info("Hooked in ProtocolLib successfully.");
        }
        getLogger().warning("You need PacketEvents/ProtocolLib to see block destroy animations.");
    }

    private void loadDatabases() {
        databaseConfig = startDatabase(databaseFile);
        regionConfig = startDatabase(regionsFile);
        guiConfig = startDatabase(guiFile);
    }

    @SneakyThrows
    public void saveDatabases() {
        databaseConfig.save(databaseFile);
        regionConfig.save(regionsFile);
    }

	@SneakyThrows
	private void updateConfiguration() {
		File configFile = new File(getDataFolder(), "config.yml");
        File updatedGuiFile = new File(getDataFolder(), "gui.yml");
		File messagesFile = new File(getDataFolder(), "messages.yml");
		if (!getDescription().getVersion().equals(SpigotVersion.VERSION.get(String.class))) {
			getLogger().info("Creating new configurations...");
			try {
				ConfigUpdater.update(this, "config.yml", configFile, Collections.emptyList());
                ConfigUpdater.update(this, "gui.yml", updatedGuiFile, Collections.emptyList());
				ConfigUpdater.update(this, "messages.yml", messagesFile, Collections.emptyList());
			} catch (IOException ignored) {
				getLogger().severe("Unable to update configuration files.");
			}

			versionTextFile.getConfig().set("version", getDescription().getVersion());
			versionTextFile.getConfig().save();
			configTextFile = new TextFile(getDataFolder().toPath(), "config.yml");
			messagesTextFile = new TextFile(getDataFolder().toPath(), "messages.yml");
			versionTextFile = new TextFile(getDataFolder().toPath(), "version.yml");
            guiFile = new File(getDataFolder(), "gui.yml");
		}
	}

	public void startupPlayer(Player player) {

        if (!player.hasPermission(SpigotConfig.PERMISSION.get(String.class))) return;
		for (ItemStack items : player.getInventory().getContents()) {
            if (items != null && (items.equals(BlockItem.getBlockItemStack(player)) || items.equals(settings))) items.setAmount(0);
        }

		UniversalScheduler.getScheduler(this).runTaskLater(() -> player.getInventory().setItem(SpigotConfig.SETTINGS_SLOT.get(Integer.class), LobbyBlocks.getInstance().getSettings()), (long) SpigotConfig.DELAY.get(Integer.class));
        UniversalScheduler.getScheduler(this).runTaskLater(() -> {
            ItemStack block = BlockItem.getBlockItemStack(player);
            block.setAmount(64);
            player.getInventory().setItem(SpigotConfig.BLOCK_SLOT.get(Integer.class), block);
        }, (long) SpigotConfig.DELAY.get(Integer.class));
	}

	public YamlFile getConfigTextFile() {
		return getInstance().configTextFile.getConfig();
	}

	public YamlFile getMessagesTextFile() {
		return getInstance().messagesTextFile.getConfig();
	}

	public YamlFile getVersionTextFile() {
		return getInstance().versionTextFile.getConfig();
	}

	@Override
	public void onDisable() {
        removeBlocks();
        savePlayerCache();
		getLogger().info("Clearing instances...");
		instance = null;
		getLogger().info("Plugin successfully disabled!");
	}

    private void removeBlocks() {
        for (Block block : PlayerCache.getBreaking()) {
            block.setType(Material.AIR);
            PlayerCache.getBreaking().remove(block);
        }
    }

    private void savePlayerCache() {
        for (Player player : getServer().getOnlinePlayers()) {
            if (PlayerCache.getBlock().get(player.getUniqueId()) == null) return;
            BlockItem.saveBlockItem(player, PlayerCache.getBlock().get(player.getUniqueId()));
        }
    }

	public void UpdateChecker(Player player) {
        if (!update) return;
        player.sendMessage("§eThere is a new update for LobbyBlocks, download it on SpigotMC!");
	}

	private void UpdateChecker() {
		if (!SpigotConfig.UPDATE_CHECK.get(Boolean.class)) return;
		new UpdateCheck(this).getVersion(version -> {
			if (Integer.parseInt(getDescription().getVersion().replace(".", "")) < Integer.parseInt(version.replace(".", ""))) {
				if (SpigotConfig.AUTO_UPDATE.get(Boolean.class) && !updated) {
					autoUpdate();
					return;
				}
				if (!updated) {
                    update = true;
					getLogger().warning("§eThere is a new update available, download it on SpigotMC!");
				}
			}
			if (Integer.parseInt(getDescription().getVersion().replace(".", "")) > Integer.parseInt(version.replace(".", ""))) {
				getLogger().warning("§eYou are using a development version, please report any bugs!");
			}
		});
	}

	public void autoUpdate() {
        String fileUrl = "https://github.com/frafol/LobbyBlocks/releases/download/release/LobbyBlocks.jar";
        String destination = "./plugins/";
        String fileName = getFileNameFromUrl(fileUrl);
        File outputFile = new File(destination, fileName);
        downloadFile(fileUrl, outputFile);
        updated = true;
        getLogger().warning("LobbyBlocks successfully updated, a restart is required.");
    }

	private String getFileNameFromUrl(String fileUrl) {
		int slashIndex = fileUrl.lastIndexOf('/');
		if (slashIndex != -1 && slashIndex < fileUrl.length() - 1) {
			return fileUrl.substring(slashIndex + 1);
		}
		throw new IllegalArgumentException("Invalid file URL");
	}

	private void downloadFile(String fileUrl, File outputFile) {
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(fileUrl);
                try (InputStream inputStream = url.openStream()) {
                    Files.copy(inputStream, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception ignored) {
                getLogger().severe("Unable to update the LobbyBlocks plugin, update it manually.");
            }
        });
	}

    public boolean isLegacyServer() {
        return !XMaterial.supports(21);
    }

    public boolean usingFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}