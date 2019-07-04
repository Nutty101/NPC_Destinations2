package net.livecar.nuttyworks.npc_destinations.thirdpartyplugins.sentinel;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.MemoryDataKey;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.utilities.Utilities;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.mcmonkey.sentinel.SentinelTarget;
import org.mcmonkey.sentinel.SentinelTrait;

public class Sentinel_Plugin {
    public DestinationsPlugin destRef           = null;
    public Sentinel_Addon     getSentinelPlugin = null;
    public int[]              version;

    @SuppressWarnings("deprecation")
    public Sentinel_Plugin(DestinationsPlugin storageRef) {
        destRef = storageRef;

        version = new int[] { 0, 0, 0, 0 };

        String verString = Bukkit.getServer().getPluginManager().getPlugin("Sentinel").getDescription().getVersion();
        if (verString.contains(" ")) {
            verString = verString.substring(0, verString.indexOf(" "));
        }

        String[] versionSplit = verString.split("\\.");

        if (versionSplit.length > 0 && storageRef.getUtilitiesClass.isNumeric(versionSplit[0]))
            version[0] = Integer.parseInt(versionSplit[0]);

        if (versionSplit.length > 1 && storageRef.getUtilitiesClass.isNumeric(versionSplit[1]))
            version[1] = Integer.parseInt(versionSplit[1]);

        if (versionSplit.length > 2 && storageRef.getUtilitiesClass.isNumeric(versionSplit[2]))
            version[2] = Integer.parseInt(versionSplit[2]);

        this.getSentinelPlugin = new Sentinel_Addon(this);
        DestinationsPlugin.Instance.getPluginManager.registerPlugin(getSentinelPlugin);
        destRef.getCommandManager.registerCommandClass(Sentinel_Commands.class);

        new BukkitRunnable() {
            @Override
            public void run() {
                onStart();
            }
        }.runTask(destRef);
    }

    private void onStart() {
        // Not listening at this moment for the events. Leaving this empty.
    }

    public String getVersionString() {
        return Bukkit.getServer().getPluginManager().getPlugin("Sentinel").getDescription().getVersion();
    }

    public Sentinel_LocationSetting getCurrentSettings(NPC npc) {
        if (!npc.hasTrait(SentinelTrait.class)) {
            return null;
        }
        SentinelTrait sentTrait = npc.getTrait(SentinelTrait.class);
        Sentinel_LocationSetting sentStorage = new Sentinel_LocationSetting();

        sentStorage.lastSet = new Date();

        Field[] fieldList = SentinelTrait.class.getFields();
        sentStorage.sentinelSettings = new MemoryDataKey();
        for (Field fieldInfo : fieldList)
        {
            fieldInfo.setAccessible(true);
            try {
                sentStorage.sentinelSettings.setRaw(fieldInfo.getName(), fieldInfo.get(sentTrait));
            } catch (Exception err)
            {}
        }

        return sentStorage;
    }

    public void setCurrentSettings(NPC npc, Sentinel_LocationSetting sentStorage) {
        if (!npc.hasTrait(SentinelTrait.class)) {
            return;
        }
        SentinelTrait sentTrait = npc.getTrait(SentinelTrait.class);
        Field[] fieldList = SentinelTrait.class.getFields();
        for (Field fieldInfo : fieldList)
        {
            fieldInfo.setAccessible(true);
            try {
                fieldInfo.set(sentTrait, sentStorage.sentinelSettings.getRaw(fieldInfo.getName()));
            } catch (Exception err)
            {}
        }


    }
}
