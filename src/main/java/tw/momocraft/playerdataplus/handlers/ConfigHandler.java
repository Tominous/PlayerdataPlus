package tw.momocraft.playerdataplus.handlers;

import com.bekvon.bukkit.residence.protection.FlagPermissions;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import tw.momocraft.playerdataplus.Commands;
import tw.momocraft.playerdataplus.PlayerdataPlus;
import tw.momocraft.playerdataplus.utils.DependAPI;
import tw.momocraft.playerdataplus.utils.Logger;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.bukkit.Bukkit.getServer;

public class ConfigHandler {

    private static YamlConfiguration configYAML;
    private static DependAPI depends;
    private static UpdateHandler updater;
    private static Logger logger;


    public static void generateData(boolean reload) {
        configFile();
        setDepends(new DependAPI());
        sendUtilityDepends();
        setUpdater(new UpdateHandler());
        setLogger(new Logger());

        if (ConfigHandler.getDepends().ResidenceEnabled()) {
            FlagPermissions.addFlag("bypassclean");
        }

        if (!reload && getConfig("config.yml").getBoolean("Clean.Settings.Auto-Clean.Enable")) {
            long delay = getConfig("config.yml").getLong("Clean.Settings.Auto-Clean.Delay") * 20;
            new BukkitRunnable() {
                @Override
                public void run() {
                    ServerHandler.sendConsoleMessage("&6Starting to clean the expired data...");
                    PurgeHandler purgeHandler = new PurgeHandler();
                    purgeHandler.startClean();
                }
            }.runTaskLater(PlayerdataPlus.getInstance(), delay);
        }  else if (!reload && getConfig("config.yml").getBoolean("Clean.Settings.Schedule.Enable")) {
            BukkitScheduler scheduler = getServer().getScheduler();
            scheduler.scheduleSyncDelayedTask(PlayerdataPlus.getInstance(),
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            ServerHandler.sendConsoleMessage("&eStarting to clean the expired data...");
                            PurgeHandler purgeHandler = new PurgeHandler();
                            purgeHandler.startClean();
                        }
                    }, 200);
        }
        
    }

    public static void registerEvents() {
        PlayerdataPlus.getInstance().getCommand("playerdataplus").setExecutor(new Commands());
    }

    public static FileConfiguration getConfig(String path) {
        File file = new File(PlayerdataPlus.getInstance().getDataFolder(), path);
        if (configYAML == null) {
            getConfigData(path);
        }
        return getPath(path, file, false);
    }

    private static FileConfiguration getConfigData(String path) {
        File file = new File(PlayerdataPlus.getInstance().getDataFolder(), path);
        if (!(file).exists()) {
            try {
                PlayerdataPlus.getInstance().saveResource(path, false);
            } catch (Exception e) {
                PlayerdataPlus.getInstance().getLogger().warning("Cannot save " + path + " to disk!");
                return null;
            }
        }
        return getPath(path, file, true);
    }

    private static YamlConfiguration getPath(String path, File file, boolean saveData) {
        if (path.contains("config.yml")) {
            if (saveData) {
                configYAML = YamlConfiguration.loadConfiguration(file);
            }
            return configYAML;
        }
        return null;
    }

    private static void configFile() {
        getConfigData("config.yml");
        File File = new File(PlayerdataPlus.getInstance().getDataFolder(), "config.yml");
        if (File.exists() && getConfig("config.yml").getInt("Config-Version") != 1) {
            if (PlayerdataPlus.getInstance().getResource("config.yml") != null) {
                LocalDateTime currentDate = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
                String currentTime = currentDate.format(formatter);
                String newGen = "config " + currentTime + ".yml";
                File newFile = new File(PlayerdataPlus.getInstance().getDataFolder(), newGen);
                if (!newFile.exists()) {
                    File.renameTo(newFile);
                    File configFile = new File(PlayerdataPlus.getInstance().getDataFolder(), "config.yml");
                    configFile.delete();
                    getConfigData("config.yml");
                    ServerHandler.sendConsoleMessage("&e*            *            *");
                    ServerHandler.sendConsoleMessage("&e *            *            *");
                    ServerHandler.sendConsoleMessage("&e  *            *            *");
                    ServerHandler.sendConsoleMessage("&cYour config.yml is out of date, generating a new one!");
                    ServerHandler.sendConsoleMessage("&e    *            *            *");
                    ServerHandler.sendConsoleMessage("&e     *            *            *");
                    ServerHandler.sendConsoleMessage("&e      *            *            *");
                }
            }
        }
        getConfig("config.yml").options().copyDefaults(false);
    }

    private static void sendUtilityDepends() {
        ServerHandler.sendConsoleMessage("&fHooked: "
                + (getDepends().getVault().vaultEnabled() ? "Vault, " : "")
                + (getDepends().CMIEnabled() ? "CMI, " : "")
                + (getDepends().ResidenceEnabled() ? "Residence, " : "")
                + (getDepends().PlaceHolderAPIEnabled() ? "PlaceHolderAPI, " : "")
                + (getDepends().MySQLPlayerDataBridgeEnabled() ? "MySQLPlayerDataBridge, " : "")
                + (getDepends().SkinsRestorerEnabled() ? "SkinsRestorer, " : "")
                + (getDepends().ChatControlProEnabled() ? "ChatControlPro, " : "")
                + (getDepends().DiscordSRVEnabled() ? "DiscordSRV, " : "")
                + (getDepends().LuckPermsEnabled() ? "LuckPerms, " : "")
                + (getDepends().MyPetEnabled() ? "MyPet, " : "")
                + (getDepends().AuthMeEnabled() ? "Authme" : "")
        );
    }

    public static DependAPI getDepends() {
        return depends;
    }

    private static void setDepends(DependAPI depend) {
        depends = depend;
    }

    private static void setUpdater(UpdateHandler update) {
        updater = update;
    }

    static boolean getDebugging() {
        return ConfigHandler.getConfig("config.yml").getBoolean("Debugging");
    }

    public static UpdateHandler getUpdater() {
        return updater;
    }

    private static void setLogger(Logger log) {
        logger = log;
    }

    public static Logger getLogger() {
        return logger;
    }


    /**
     * Converts a serialized location to a Location. Returns null if string is empty
     *
     * @param s - serialized location in format "world:x:y:z"
     * @return Location
     */
    public static Location getLocationString(final String s) {
        if (s == null || s.trim().equals("")) {
            return null;
        }
        final String[] parts = s.split(":");
        if (parts.length == 4) {
            final World w = getServer().getWorld(parts[0]);
            final int x = Integer.parseInt(parts[1]);
            final int y = Integer.parseInt(parts[2]);
            final int z = Integer.parseInt(parts[3]);
            return new Location(w, x, y, z);
        }
        return null;
    }

    public static boolean getEnable(String path, Boolean empty) {
        String enable = ConfigHandler.getConfig("config.yml").getString(path);
        if (enable == null) {
            return empty;
        }
        return enable.equals("true");
    }
}
