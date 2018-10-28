package net.livecar.nuttyworks.npc_destinations;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.event.CitizensDisableEvent;
import net.livecar.nuttyworks.npc_destinations.worldguard.WorldGuard_6_2_2;
import net.livecar.nuttyworks.npc_destinations.worldguard.WorldGuard_7_0_0;
import net.livecar.nuttyworks.npc_destinations.worldguard.WorldGuardInterface;
import net.livecar.nuttyworks.npc_destinations.bridges.*;
import net.livecar.nuttyworks.npc_destinations.citizens.Citizens_Processing;
import net.livecar.nuttyworks.npc_destinations.citizens.Citizens_Utilities;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import net.livecar.nuttyworks.npc_destinations.citizens.Citizens_WaypointProvider;
import net.livecar.nuttyworks.npc_destinations.lightapi.LightAPI_Plugin;
import net.livecar.nuttyworks.npc_destinations.listeners.BlockStickListener_NPCDest;
import net.livecar.nuttyworks.npc_destinations.listeners.PlayerJoinListener_NPCDest;
import net.livecar.nuttyworks.npc_destinations.listeners.commands.Command_Manager;
import net.livecar.nuttyworks.npc_destinations.listeners.commands.Commands_Location;
import net.livecar.nuttyworks.npc_destinations.listeners.commands.Commands_NPC;
import net.livecar.nuttyworks.npc_destinations.listeners.commands.Commands_Plugin;
import net.livecar.nuttyworks.npc_destinations.messages.Language_Manager;
import net.livecar.nuttyworks.npc_destinations.messages.Messages_Manager;
import net.livecar.nuttyworks.npc_destinations.metrics.BStat_Metrics;
import net.livecar.nuttyworks.npc_destinations.particles.*;
import net.livecar.nuttyworks.npc_destinations.pathing.AstarPathFinder;
import net.livecar.nuttyworks.npc_destinations.plugins.Plugin_Manager;
import net.livecar.nuttyworks.npc_destinations.thirdpartyplugins.betonquest.BetonQuest_Interface;
import net.livecar.nuttyworks.npc_destinations.thirdpartyplugins.betonquest.BetonQuest_Plugin_V1_9;
import net.livecar.nuttyworks.npc_destinations.thirdpartyplugins.jobsreborn.JobsReborn_Plugin;
import net.livecar.nuttyworks.npc_destinations.thirdpartyplugins.plotsquared.PlotSquared_Plugin;
import net.livecar.nuttyworks.npc_destinations.thirdpartyplugins.sentinel.Sentinel_Plugin;
import net.livecar.nuttyworks.npc_destinations.utilities.Utilities;
import net.livecar.nuttyworks.npc_destinations.messages.jsonChat;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

//  use regions to define he wonder area
public class DestinationsPlugin extends org.bukkit.plugin.java.JavaPlugin implements org.bukkit.event.Listener {

    public static DestinationsPlugin Instance            = null;

    // For quick reference to this instance of the plugin.
    public FileConfiguration         getDefaultConfig;

    // variables
    public List<DebugTarget>         debugTargets        = null;
    public jsonChat                  jsonChat            = null;
    public AstarPathFinder           getPathClass        = null;
    public String                    currentLanguage     = "en_def";
    public Level                     debugLogLevel       = Level.OFF;
    public int                       maxDistance         = 500;
    public int                       Version             = 10000;
    public int                       entityRadius        = 47 * 47;

    // Storage locations
    public File                      languagePath;
    public File                      loggingPath;

    // Links to classes
    public Language_Manager          getLanguageManager  = null;
    public Messages_Manager          getMessageManager   = null;
    public Citizens                  getCitizensPlugin   = null;
    public BetonQuest_Interface      bqPlugin            = null;
    public LightAPI_Plugin           getLightPlugin      = null;
    public JobsReborn_Plugin         getJobsRebornPlugin = null;
    public Sentinel_Plugin           getSentinelPlugin   = null;
    public Plugin_Manager            getPluginManager    = null;
    public WorldGuardInterface       getWorldGuardPlugin = null;
    public PlayParticleInterface     getParticleManager  = null;
    public Utilities                 getUtilitiesClass   = null;
    public Command_Manager           getCommandManager   = null;
    public Citizens_Processing       getCitizensProc     = null;
    public PlotSquared_Plugin        getPlotSquared      = null;
    public MCUtilsBridge             getMCUtils          = null;

    public void onLoad() {
        DestinationsPlugin.Instance = this;

        if (getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            getServer().getLogger().log(Level.WARNING, "Worldguard not found, custom flags are not enabled");
        } else {
            String wgVer = getServer().getPluginManager().getPlugin("WorldGuard").getDescription().getVersion();
            if (wgVer.contains(";"))
                wgVer = wgVer.substring(0, wgVer.indexOf(";"));
            if (wgVer.contains("-SNAPSHOT"))
                wgVer = wgVer.substring(0, wgVer.indexOf("-"));
            if (wgVer.startsWith("v"))
                wgVer = wgVer.substring(1);

            String[] parts = wgVer.split("[.]");

            int majorVersion = 0;

            boolean goodVersion = false;
            try {
                Integer[] verPart = new Integer[3];
                if (Utilities.isNumeric(parts[0])) {
                    verPart[0] = Integer.parseInt(parts[0]);
                }

                if (Utilities.isNumeric(parts[1])) {
                    verPart[1] = Integer.parseInt(parts[1]);
                }

                if (parts.length > 2 && Utilities.isNumeric(parts[2])) {
                    verPart[2] = Integer.parseInt(parts[2]);
                }

                if (verPart[0] == 6 && verPart[1] == 1 && verPart[2] >= 3) {
                    majorVersion = 6;
                    goodVersion = true;
                } else if (verPart[0] == 6 && verPart[1] > 1) {
                    majorVersion = 6;
                    goodVersion = true;
                } else if (verPart[0] == 6) {
                    goodVersion = true;
                    majorVersion = 6;
                } else if (verPart[0] >= 7) {
                    goodVersion = true;
                    majorVersion = 7;
                }

            } catch (Exception err) {
                goodVersion = false;
            }

            if (!goodVersion) {
                getServer().getLogger().log(Level.WARNING, "This Worldguard version is not supported, custom flags are not enabled");
            } else {
                if (majorVersion == 6)
                    this.getWorldGuardPlugin = new WorldGuard_6_2_2(this);
                else if (majorVersion == 7)
                    this.getWorldGuardPlugin = new WorldGuard_7_0_0(this);
                this.getWorldGuardPlugin.registerFlags();
            }
        }
    }

    public void onEnable() {

        // Setup defaults
        debugTargets = new ArrayList<DebugTarget>();
        getLanguageManager = new Language_Manager(this);
        getMessageManager = new Messages_Manager(this);
        getPluginManager = new Plugin_Manager(this);
        getUtilitiesClass = new Utilities(this);
        getCommandManager = new Command_Manager(this);
        getCitizensProc = new Citizens_Processing(this);

        // Setup the default paths in the storage folder.
        languagePath = new File(this.getDataFolder(), "/Languages/");
        loggingPath = new File(this.getDataFolder(), "/Logs/");

        // Generate the default folders and files.
        getDefaultConfigs();

        // Get languages
        getLanguageManager.loadLanguages();

        // Init Default settings
        if (this.getDefaultConfig.contains("language"))
            this.currentLanguage = this.getDefaultConfig.getString("language");
        if (this.currentLanguage.equalsIgnoreCase("en-default"))
            this.currentLanguage = "en_def";

        if (this.getDefaultConfig.contains("debug"))
            this.debugLogLevel = Level.parse(this.getDefaultConfig.getString("debug"));
        if (this.getDefaultConfig.contains("max-distance"))
            this.maxDistance = this.getDefaultConfig.getInt("max-distance", 500);
        if (this.getDefaultConfig.contains("max-distance"))
            this.maxDistance = this.getDefaultConfig.getInt("max-distance", 500);

        // Register commands
        getCommandManager.registerCommandClass(Commands_Plugin.class);
        getCommandManager.registerCommandClass(Commands_NPC.class);
        getCommandManager.registerCommandClass(Commands_Location.class);

        // Mark the version
        if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_8_R3")) {
            Version = 10808;
            getParticleManager = new PlayParticle_1_8_R3();
            getPathClass = new AstarPathFinder(this);
            getMCUtils = new MCUtil_1_8_R3();
            getMessageManager.consoleMessage(this, "destinations", "console_messages.plugin_version", getServer().getVersion().substring(getServer().getVersion().indexOf('(')));
        } else if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_9_R1")) {
            Version = 10900;
            getParticleManager = new PlayParticle_1_9_R1();
            getPathClass = new AstarPathFinder(this);
            getMCUtils = new MCUtil_1_9_R1();
            getMessageManager.consoleMessage(this, "destinations", "console_messages.plugin_version", getServer().getVersion().substring(getServer().getVersion().indexOf('(')));
        } else if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_9_R2")) {
            Version = 10902;
            getParticleManager = new PlayParticle_1_9_R2();
            getPathClass = new AstarPathFinder(this);
            getMCUtils = new MCUtil_1_9_R2();
            getMessageManager.consoleMessage(this, "destinations", "console_messages.plugin_version", getServer().getVersion().substring(getServer().getVersion().indexOf('(')));
        } else if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_10_R1")) {
            Version = 11000;
            getParticleManager = new PlayParticle_1_10_R1();
            getPathClass = new AstarPathFinder(this);
            getMCUtils = new MCUtil_1_10_R1();
            getMessageManager.consoleMessage(this, "destinations", "console_messages.plugin_version", getServer().getVersion().substring(getServer().getVersion().indexOf('(')));
        } else if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_11_R1") && getServer().getVersion().endsWith("MC: 1.11)")) {
            Version = 11100;
            getParticleManager = new PlayParticle_1_11_R1();
            getPathClass = new AstarPathFinder(this);
            getMCUtils = new MCUtil_1_11_R1();
            getMessageManager.consoleMessage(this, "destinations", "console_messages.plugin_version", getServer().getVersion().substring(getServer().getVersion().indexOf('(')));
        } else if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_11_R1") && getServer().getVersion().endsWith("MC: 1.11.1)")) {
            Version = 11110;
            getParticleManager = new PlayParticle_1_11_R1();
            getPathClass = new AstarPathFinder(this);
            getMCUtils = new MCUtil_1_11_R1();
            getMessageManager.consoleMessage(this, "destinations", "console_messages.plugin_version", getServer().getVersion().substring(getServer().getVersion().indexOf('(')));
        } else if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_11_R1") && getServer().getVersion().endsWith("MC: 1.11.2)")) {
            Version = 11120;
            getParticleManager = new PlayParticle_1_11_R1();
            getPathClass = new AstarPathFinder(this);
            getMCUtils = new MCUtil_1_11_R1();
            getMessageManager.consoleMessage(this, "destinations", "console_messages.plugin_version", getServer().getVersion().substring(getServer().getVersion().indexOf('(')));
        } else if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_12_R1")) {
            Version = 11200;
            getParticleManager = new PlayParticle_1_12_R1();
            getPathClass = new AstarPathFinder(this);
            getMCUtils = new MCUtil_1_12_R1();
            getMessageManager.consoleMessage(this, "destinations", "console_messages.plugin_version", getServer().getVersion().substring(getServer().getVersion().indexOf('(')));
        } else if (Bukkit.getServer().getClass().getPackage().getName().endsWith("v1_13_R2")) {
            Version = 11310;
            getParticleManager = new PlayParticle_1_13_R2();
            getPathClass = new AstarPathFinder(this);
            getMCUtils = new MCUtil_1_13_R2();
            getMessageManager.consoleMessage(this, "destinations", "console_messages.plugin_version", getServer().getVersion().substring(getServer().getVersion().indexOf('(')));
        } else {
            getMessageManager.consoleMessage(this, "destinations", "console_messages.plugin_unknownversion", getServer().getVersion());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Init links to other plugins
        if (getServer().getPluginManager().getPlugin("Citizens") == null || getServer().getPluginManager().getPlugin("Citizens").isEnabled() == false || !(getServer().getPluginManager().getPlugin("Citizens") instanceof Citizens)) {
            this.getMessageManager.debugMessage(Level.CONFIG, "nuNPCDestinations.onEnable()|CitizensNotFound");
            getMessageManager.consoleMessage(this, "destinations", "Console_Messages.citizens_notfound");
            getServer().getPluginManager().disablePlugin(this);
            return;
        } else {
            getCitizensPlugin = (Citizens) getServer().getPluginManager().getPlugin("Citizens");
            getMessageManager.consoleMessage(this, "destinations", "Console_Messages.citizens_found", getCitizensPlugin.getDescription().getVersion());
        }

        if (getServer().getPluginManager().getPlugin("BetonQuest") == null) {
            this.getMessageManager.debugMessage(Level.CONFIG, "nuNPCDestinations.onEnable()|BetonQuest_NotFound");
            getMessageManager.consoleMessage(this, "destinations", "Console_Messages.betonquest_notfound");
        } else {
            // Get version 1.8.x or 1.9x
            Bukkit.getServer().getLogger().log(Level.ALL, "Version Check" + getServer().getPluginManager().getPlugin("BetonQuest").getDescription().getVersion());
            if (getServer().getPluginManager().getPlugin("BetonQuest").getDescription().getVersion().startsWith("1.9") || getServer().getPluginManager().getPlugin("BetonQuest").getDescription().getVersion().startsWith("1.10")) {
                getMessageManager.consoleMessage(this, "destinations", "Console_Messages.betonquest_found", getServer().getPluginManager().getPlugin("BetonQuest").getDescription().getVersion());
                bqPlugin = new BetonQuest_Plugin_V1_9(this);
                this.getMessageManager.debugMessage(Level.CONFIG, "nuNPCDestinations.onEnable()|BetonQuestFound");
            }
        }

        if (getServer().getPluginManager().getPlugin("LightAPI") == null) {
            this.getMessageManager.debugMessage(Level.CONFIG, "nuNPCDestinations.onEnable()|LightAPI_NotFound");
            getMessageManager.consoleMessage(this, "destinations", "Console_Messages.lightapi_notfound");
        } else {
            getLightPlugin = new LightAPI_Plugin(this);
            this.getMessageManager.debugMessage(Level.CONFIG, "nuNPCDestinations.onEnable()|LightAPI_Found");
            getMessageManager.consoleMessage(this, "destinations", "Console_Messages.lightapi_found", getServer().getPluginManager().getPlugin("LightAPI").getDescription().getVersion());
        }

        // 1.31 - Jobs Reborn
        if (getServer().getPluginManager().getPlugin("Jobs") == null) {
            this.getMessageManager.debugMessage(Level.CONFIG, "nuNPCDestinations.onEnable()|JobsReborn_NotFound");
            getMessageManager.consoleMessage(this, "destinations", "Console_Messages.jobsreborn_notfound");
        } else {
            getJobsRebornPlugin = new JobsReborn_Plugin(this);
            this.getMessageManager.debugMessage(Level.CONFIG, "nuNPCDestinations.onEnable()|JobsReborn_Found");
            getMessageManager.consoleMessage(this, "destinations", "Console_Messages.jobsreborn_found");
        }

        // 1.39 - Sentinel!
        if (getServer().getPluginManager().getPlugin("Sentinel") == null) {
            this.getMessageManager.debugMessage(Level.CONFIG, "nuNPCDestinations.onEnable()|Sentinel_NotFound");
            getMessageManager.consoleMessage(this, "sentinel", "Console_Messages.sentinel_notfound");
        } else {
            this.getSentinelPlugin = new Sentinel_Plugin(this);
            getMessageManager.consoleMessage(this, "sentinel", "Console_Messages.sentinel_found", getSentinelPlugin.getVersionString());
        }

        // 2.1.8 - Plotsquared compliance
        if (getServer().getPluginManager().getPlugin("PlotSquared") == null) {
            this.getMessageManager.debugMessage(Level.CONFIG, "nuNPCDestinations.onEnable()|plotsquared_NotFound");
            getMessageManager.consoleMessage(this, "destinations", "Console_Messages.plotsquared_notfound");
        } else {
            this.getPlotSquared = new PlotSquared_Plugin(this);
            this.getMessageManager.debugMessage(Level.CONFIG, "nuNPCDestinations.onEnable()|plotsquared_Found");
            getMessageManager.consoleMessage(this, "destinations", "Console_Messages.plotsquared_found");
        }

        if (getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            getMessageManager.consoleMessage(this, "console_messages.worldguard_notfound", "");
        } else {
            String wgVer = getServer().getPluginManager().getPlugin("WorldGuard").getDescription().getVersion();
            if (wgVer.contains(";"))
                wgVer = wgVer.substring(0, wgVer.indexOf(";"));
            if (wgVer.contains("-SNAPSHOT"))
                wgVer = wgVer.substring(0, wgVer.indexOf("-"));
            if (wgVer.startsWith("v"))
                wgVer = wgVer.substring(1);

            String[] parts = wgVer.split("[.]");

            boolean goodVersion = false;
            Integer[] verPart = new Integer[3];
            if (Utilities.isNumeric(parts[0])) {
                verPart[0] = Integer.parseInt(parts[0]);
            }

            if (Utilities.isNumeric(parts[1])) {
                verPart[1] = Integer.parseInt(parts[1]);
            }

            if (parts.length < 3) {
                goodVersion = false;
            } else {

                if (parts.length > 2 && Utilities.isNumeric(parts[2])) {
                    verPart[2] = Integer.parseInt(parts[2]);
                }

                if (verPart[0] == 6 && verPart[1] == 1 && verPart[2] >= 3) {
                    goodVersion = true;
                } else if (verPart[0] == 6 && verPart[1] > 1) {
                    goodVersion = true;
                } else if (verPart[0] > 6) {
                    goodVersion = true;
                }
            }

            if (!goodVersion) {
                getMessageManager.consoleMessage(this, "destinations", "console_messages.worldguard_unsupported", getServer().getPluginManager().getPlugin("WorldGuard").getDescription().getVersion());
            } else {
                getMessageManager.consoleMessage(this, "destinations", "console_messages.worldguard_found", getServer().getPluginManager().getPlugin("WorldGuard").getDescription().getVersion());
                this.getWorldGuardPlugin.registerEvents();
                this.getWorldGuardPlugin.checkWorld();
            }
        }

        jsonChat = new net.livecar.nuttyworks.npc_destinations.messages.jsonChat(this);

        // Register your trait with Citizens.
        net.citizensnpcs.api.CitizensAPI.getTraitFactory().registerTrait(net.citizensnpcs.api.trait.TraitInfo.create(NPCDestinationsTrait.class).withName("npcdestinations"));

        // Register events
        Bukkit.getPluginManager().registerEvents(this, this);

        Bukkit.getPluginManager().registerEvents(new BlockStickListener_NPCDest(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener_NPCDest(this), this);

        net.citizensnpcs.trait.waypoint.Waypoints.registerWaypointProvider(Citizens_WaypointProvider.class, "npcdestinations");

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                try {
                    getPathClass.CheckStatus();
                } catch (Exception e) {
                }
            }
        }, 30L, 5L);

        // 1.34 - Citizens save.yml backup monitor
        final Citizens_Utilities backupClass = new Citizens_Utilities(this);

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                try {
                    backupClass.BackupConfig(false);
                } catch (Exception e) {
                }
            }
        }, 1200L, 1200L);

        final BStat_Metrics statsReporting = new BStat_Metrics(this);

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                statsReporting.Start();
            }
        });
    }

    public void onDisable() {
        if (isEnabled()) {

            this.getMessageManager.debugMessage(Level.CONFIG, "nuNPCDestinations.onDisable()|Stopping Internal Processes");
            Bukkit.getServer().getScheduler().cancelTasks(this);
            getPathClass.currentTask = null;
            getPathClass.path_Queue.clear();
            getPathClass = null;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] inargs) {
        if (cmd.getName().equalsIgnoreCase("npcdest") | cmd.getName().equalsIgnoreCase("nd")) {
            return getCommandManager.onCommand(sender, inargs);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String cmdLabel, String[] inargs) {
        if (cmd.getName().equalsIgnoreCase("npcdest") | cmd.getName().equalsIgnoreCase("nd")) {
            return getCommandManager.onTabComplete(sender, inargs);
        }
        return new ArrayList<String>();
    }

    @EventHandler
    public void CitizensDisabled(final CitizensDisableEvent event) {
        Bukkit.getServer().getScheduler().cancelTasks(this);
        getPathClass = null;
        getMessageManager.consoleMessage(this, "destinations", "Console_Messages.plugin_ondisable");
        getServer().getPluginManager().disablePlugin(this);
    }

    public Boolean hasPermissions(CommandSender player, String[] permissions) {
        for (String perm : permissions) {
            if (hasPermissions(player, perm))
                return true;
        }
        return false;
    }

    public Boolean hasPermissions(CommandSender player, String permission) {
        if (player instanceof Player) {
            if (player.isOp())
                return true;

            if (permission.toLowerCase().startsWith("npcdestinations.editall.") && player.hasPermission("npcdestinations.editall.*"))
                return true;

            if (permission.toLowerCase().startsWith("npcdestinations.editown.") && player.hasPermission("npcdestinations.editown.*"))
                return true;

            return player.hasPermission(permission);
        }
        return true;
    }

    private void getDefaultConfigs() {
        // Create the default folders
        if (!this.getDataFolder().exists())
            this.getDataFolder().mkdirs();
        if (!languagePath.exists())
            languagePath.mkdirs();
        if (!loggingPath.exists())
            loggingPath.mkdirs();

        // Validate that the default package is in the MountPackages folder. If
        // not, create it.
        if (!(new File(getDataFolder(), "config.yml").exists()))
            exportConfig(getDataFolder(), "config.yml");
        exportConfig(languagePath, "en_def-destinations.yml");
        exportConfig(languagePath, "en_def-jobsreborn.yml");
        exportConfig(languagePath, "en_def-sentinel.yml");

        this.getDefaultConfig = getUtilitiesClass.loadConfiguration(new File(this.getDataFolder(), "config.yml"));
    }

    private void exportConfig(File path, String filename) {
        this.getMessageManager.debugMessage(Level.FINEST, "nuDestinationsPlugin.exportConfig()|");
        File fileConfig = new File(path, filename);
        if (!fileConfig.isDirectory()) {
            // Reader defConfigStream = null;
            try {
                FileUtils.copyURLToFile(getClass().getResource("/" + filename), fileConfig);
                // defConfigStream = new
                // InputStreamReader(this.getResource(filename), "UTF8");
            } catch (IOException e1) {
                this.getMessageManager.debugMessage(Level.SEVERE, "nuDestinationsPlugin.exportConfig()|FailedToExtractFile(" + filename + ")");
                getMessageManager.logToConsole(this, " Failed to extract default file (" + filename + ")");
                return;
            }
        }
    }

}
