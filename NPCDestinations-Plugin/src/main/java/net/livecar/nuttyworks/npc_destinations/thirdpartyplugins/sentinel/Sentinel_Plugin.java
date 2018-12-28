package net.livecar.nuttyworks.npc_destinations.thirdpartyplugins.sentinel;

import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.utilities.Utilities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

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

        String[] versionSplit = verString.split(".");

        if (versionSplit.length > 0 && Utilities.isNumeric(versionSplit[0]))
            version[0] = Integer.parseInt(versionSplit[0]);

        if (versionSplit.length > 1 && Utilities.isNumeric(versionSplit[1]))
            version[1] = Integer.parseInt(versionSplit[1]);

        if (versionSplit.length > 2 && Utilities.isNumeric(versionSplit[2]))
            version[2] = Integer.parseInt(versionSplit[2]);

        this.getSentinelPlugin = new Sentinel_Addon(this);
        DestinationsPlugin.Instance.getPluginManager.registerPlugin(getSentinelPlugin);
        destRef.getCommandManager.registerCommandClass(Sentinel_Commands.class);

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(destRef, new BukkitRunnable() {
            @Override
            public void run() {
                onStart();
            }
        });
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

        if ((version[0] == 0 && version[1] > 4) || (version[0] >= 1)) {
            sentStorage.range = sentTrait.range;
            sentStorage.damage = sentTrait.damage;
            sentStorage.armor = sentTrait.armor;
            sentStorage.health = sentTrait.health;
            sentStorage.chaseRange = sentTrait.chaseRange;
            sentStorage.attackRate = sentTrait.attackRate;
            sentStorage.enemyDrops = sentTrait.enemyDrops;
            sentStorage.closeChase = sentTrait.closeChase;
            sentStorage.enemyDrops = sentTrait.enemyDrops;
            sentStorage.enemyTargetTime = sentTrait.enemyTargetTime;
            sentStorage.fightback = sentTrait.fightback;
            sentStorage.guardingLower = sentTrait.guardingLower;
            sentStorage.guardingUpper = sentTrait.guardingUpper;
            sentStorage.healRate = sentTrait.healRate;
            sentStorage.invincible = sentTrait.invincible;
            sentStorage.needsAmmo = sentTrait.needsAmmo;
            sentStorage.range = sentTrait.range;
            sentStorage.rangedChase = sentTrait.rangedChase;
            sentStorage.respawnTime = sentTrait.respawnTime;
            sentStorage.safeShot = sentTrait.safeShot;
            sentStorage.playerNameIgnores = sentTrait.playerNameIgnores;
            sentStorage.playerNameTargets = sentTrait.playerNameTargets;
            sentStorage.npcNameIgnores = sentTrait.npcNameIgnores;
            sentStorage.npcNameTargets = sentTrait.npcNameTargets;
            sentStorage.entityNameIgnores = sentTrait.entityNameIgnores;
            sentStorage.entityNameTargets = sentTrait.entityNameTargets;
            sentStorage.heldItemIgnores = sentTrait.heldItemIgnores;
            sentStorage.heldItemTargets = sentTrait.heldItemTargets;
            sentStorage.groupIgnores = sentTrait.groupIgnores;
            sentStorage.groupTargets = sentTrait.groupTargets;
            sentStorage.eventTargets = sentTrait.eventTargets;
            sentStorage.drops = sentTrait.drops;

            sentStorage.targets = new ArrayList<String>();
            for (String target : sentTrait.targets)
                sentStorage.targets.add(target);

            sentStorage.ignores = new ArrayList<String>();
            for (String ignore : sentTrait.ignores)
                sentStorage.ignores.add(ignore);

        }

        if ((version[0] == 0 && version[1] > 6) || (version[0] >= 1)) {
            sentStorage.greetRange = sentTrait.greetRange;
            sentStorage.speed = sentTrait.speed;
            sentStorage.accuracy = sentTrait.accuracy;
            sentStorage.greetingText = sentTrait.greetingText;
            sentStorage.warningText = sentTrait.warningText;
        }

        if ((version[0] == 0 && version[1] >= 9) || (version[0] >= 1)) {
            sentStorage.squad = sentTrait.squad;
            sentStorage.autoswitch = sentTrait.autoswitch;
        }

        return sentStorage;
    }

    public void setCurrentSettings(NPC npc, Sentinel_LocationSetting sentStorage) {
        if (!npc.hasTrait(SentinelTrait.class)) {
            return;
        }
        SentinelTrait sentTrait = npc.getTrait(SentinelTrait.class);

        if ((version[0] == 0 && version[1] > 4) || (version[0] >= 1)) {
            sentTrait.range = sentStorage.range;
            sentTrait.damage = sentStorage.damage;
            sentTrait.armor = sentStorage.armor;
            sentTrait.health = sentStorage.health;
            sentTrait.chaseRange = sentStorage.chaseRange;
            sentTrait.attackRate = sentStorage.attackRate;
            sentTrait.enemyDrops = sentStorage.enemyDrops;
            sentTrait.closeChase = sentStorage.closeChase;
            sentTrait.enemyDrops = sentStorage.enemyDrops;
            sentTrait.enemyTargetTime = sentStorage.enemyTargetTime;
            sentTrait.fightback = sentStorage.fightback;
            sentTrait.guardingLower = sentStorage.guardingLower;
            sentTrait.guardingUpper = sentStorage.guardingUpper;
            sentTrait.healRate = sentStorage.healRate;
            sentTrait.invincible = sentStorage.invincible;
            sentTrait.needsAmmo = sentStorage.needsAmmo;
            sentTrait.range = sentStorage.range;
            sentTrait.rangedChase = sentStorage.rangedChase;
            sentTrait.respawnTime = sentStorage.respawnTime;
            sentTrait.safeShot = sentStorage.safeShot;
            sentTrait.playerNameIgnores = sentStorage.playerNameIgnores;
            sentTrait.playerNameTargets = sentStorage.playerNameTargets;
            sentTrait.npcNameIgnores = sentStorage.npcNameIgnores;
            sentTrait.npcNameTargets = sentStorage.npcNameTargets;
            sentTrait.entityNameIgnores = sentStorage.entityNameIgnores;
            sentTrait.entityNameTargets = sentStorage.entityNameTargets;
            sentTrait.heldItemIgnores = sentStorage.heldItemIgnores;
            sentTrait.heldItemTargets = sentStorage.heldItemTargets;
            sentTrait.groupIgnores = sentStorage.groupIgnores;
            sentTrait.groupTargets = sentStorage.groupTargets;
            sentTrait.eventTargets = sentStorage.eventTargets;
            sentTrait.drops = sentStorage.drops;

            sentTrait.ignores = new HashSet<>();
            for (String name : sentStorage.ignores)
                sentTrait.ignores.add(name);

            sentTrait.targets = new HashSet<>();
            for (String name : sentStorage.targets)
                sentTrait.targets.add(name);

        }

        if ((version[0] == 0 && version[1] > 6) || (version[0] >= 1)) {
            sentTrait.greetRange = sentStorage.greetRange;
            sentTrait.speed = sentStorage.speed;
            sentTrait.accuracy = sentStorage.accuracy;
            sentTrait.greetingText = sentStorage.greetingText;
            sentTrait.warningText = sentStorage.warningText;
        }

        if ((version[0] == 0 && version[1] >= 9) || (version[0] >= 1)) {
            sentTrait.squad = sentStorage.squad;
            sentTrait.autoswitch = sentStorage.autoswitch;
        }

    }
}
