package net.livecar.nuttyworks.npc_destinations.thirdpartyplugins.sentinel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.livecar.nuttyworks.npc_destinations.api.Destination_Setting;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import net.livecar.nuttyworks.npc_destinations.plugins.DestinationsAddon;

public class Sentinel_Addon extends DestinationsAddon {
    public Sentinel_Plugin                   pluginReference = null;
    public Map<Integer, Sentinel_NPCSetting> npcSettings     = new HashMap<Integer, Sentinel_NPCSetting>();

    public Sentinel_Addon(Sentinel_Plugin instanceRef) {
        pluginReference = instanceRef;
    }

    @Override
    public String getPluginIcon() {
        return "☠";
    }

    @Override
    public String getActionName() {
        return "Sentinel";
    }

    @Override
    public String getQuickDescription() {
        String[] response = pluginReference.destRef.getMessageManager.buildMessage("sentinel", "sentinel.plugin_description", "");
        return response[0];
    }

    @Override
    public String getDestinationHelp(NPC npc, NPCDestinationsTrait npcTrait, Destination_Setting location) {
        String[] response = pluginReference.destRef.getMessageManager.buildMessage("sentinel", null, "sentinel.plugin_destination", npcTrait, location, npc, null, 0);
        return response[0];
    }

    public String parseLanguageLine(String message, NPCDestinationsTrait npcTrait, Destination_Setting locationSetting, Material blockMaterial, NPC npc, int ident) {
        if (locationSetting != null) {
            if (!npcSettings.containsKey(npc.getId())) {
                message = message.replaceAll("<location\\.sentinel>", "");
                return message;
            }

            if (npcSettings.get(npc.getId()).locations.containsKey(locationSetting.LocationIdent)) {
                Sentinel_LocationSetting locSetting = npcSettings.get(npc.getId()).locations.get(locationSetting.LocationIdent);

                if (message.toLowerCase().contains("<location.sentinel>")) {
                    if (locSetting.lastSet.getTime() == 0) {
                        message = message.replaceAll("<location\\.sentinel>", "No settings");
                    } else {
                        SimpleDateFormat format = new SimpleDateFormat("MMM dd yyyy");
                        message = message.replaceAll("<location\\.sentinel>", format.format(locSetting.lastSet));
                    }
                }
            } else {
                message = message.replaceAll("<location\\.sentinel>", "");
            }
        }
        return message;
    }

    public boolean isDestinationEnabled(NPC npc, NPCDestinationsTrait npcTrait, Destination_Setting location) {
        return true;
    }

    @SuppressWarnings("unchecked")
    public void onLocationLoading(NPC npc, NPCDestinationsTrait npcTrait, Destination_Setting location, DataKey storageKey) {
        if (!storageKey.keyExists("Sentinel"))
            return;

        Sentinel_NPCSetting npcSetting;
        if (!npcSettings.containsKey(npc.getId())) {
            npcSetting = new Sentinel_NPCSetting();
            npcSetting.setNPC(npc.getId());
            npcSettings.put(npc.getId(), npcSetting);
        } else {
            npcSetting = npcSettings.get(npc.getId());
        }

        Sentinel_LocationSetting oLoc = new Sentinel_LocationSetting();

        if (storageKey.keyExists("Sentinel.lastSet"))
            oLoc.lastSet = new Date(storageKey.getLong("Sentinel.lastSet"));

        if (storageKey.keyExists("Sentinel.range"))
            oLoc.range = storageKey.getDouble("Sentinel.range");
        if (storageKey.keyExists("Sentinel.armor"))
            oLoc.armor = storageKey.getDouble("Sentinel.armor");
        if (storageKey.keyExists("Sentinel.damage"))
            oLoc.damage = storageKey.getDouble("Sentinel.damage");
        if (storageKey.keyExists("Sentinel.armor"))
            oLoc.armor = storageKey.getDouble("Sentinel.armor");
        if (storageKey.keyExists("Sentinel.health"))
            oLoc.health = storageKey.getDouble("Sentinel.health");
        if (storageKey.keyExists("Sentinel.chaseRange"))
            oLoc.chaseRange = storageKey.getDouble("Sentinel.chaseRange");
        if (storageKey.keyExists("Sentinel.greetRange"))
            oLoc.greetRange = storageKey.getDouble("Sentinel.greetRange");
        if (storageKey.keyExists("Sentinel.accuracy"))
            oLoc.accuracy = storageKey.getDouble("Sentinel.accuracy");
        if (storageKey.keyExists("Sentinel.speed"))
            oLoc.speed = storageKey.getDouble("Sentinel.speed");

        if (storageKey.keyExists("Sentinel.rangedChase"))
            oLoc.rangedChase = storageKey.getBoolean("Sentinel.rangedChase");
        if (storageKey.keyExists("Sentinel.closeChase"))
            oLoc.closeChase = storageKey.getBoolean("Sentinel.closeChase");
        if (storageKey.keyExists("Sentinel.invincible"))
            oLoc.invincible = storageKey.getBoolean("Sentinel.invincible");
        if (storageKey.keyExists("Sentinel.fightback"))
            oLoc.fightback = storageKey.getBoolean("Sentinel.fightback");
        if (storageKey.keyExists("Sentinel.needsAmmo"))
            oLoc.needsAmmo = storageKey.getBoolean("Sentinel.needsAmmo");
        if (storageKey.keyExists("Sentinel.safeShot"))
            oLoc.safeShot = storageKey.getBoolean("Sentinel.safeShot");
        if (storageKey.keyExists("Sentinel.enemyDrops"))
            oLoc.enemyDrops = storageKey.getBoolean("Sentinel.enemyDrops");

        if (storageKey.keyExists("Sentinel.attackRate"))
            oLoc.attackRate = storageKey.getInt("Sentinel.attackRate");
        if (storageKey.keyExists("Sentinel.healRate"))
            oLoc.healRate = storageKey.getInt("Sentinel.healRate");

        if (storageKey.keyExists("Sentinel.guardingUpper"))
            oLoc.guardingUpper = storageKey.getLong("Sentinel.guardingUpper");
        if (storageKey.keyExists("Sentinel.guardingLower"))
            oLoc.guardingLower = storageKey.getLong("Sentinel.guardingLower");
        if (storageKey.keyExists("Sentinel.respawnTime"))
            oLoc.respawnTime = storageKey.getLong("Sentinel.respawnTime");
        if (storageKey.keyExists("Sentinel.enemyTargetTime"))
            oLoc.enemyTargetTime = storageKey.getLong("Sentinel.enemyTargetTime");

        if (storageKey.keyExists("Sentinel.warningText"))
            oLoc.warningText = storageKey.getString("Sentinel.warningText");
        if (storageKey.keyExists("Sentinel.greetingText"))
            oLoc.greetingText = storageKey.getString("Sentinel.greetingText");

        if (storageKey.keyExists("Sentinel.ignores"))
            oLoc.ignores = (ArrayList<String>) storageKey.getRaw("Sentinel.ignores");
        if (storageKey.keyExists("Sentinel.targets"))
            oLoc.targets = (ArrayList<String>) storageKey.getRaw("Sentinel.targets");

        if (storageKey.keyExists("Sentinel.playerNameTargets"))
            oLoc.playerNameTargets = (ArrayList<String>) storageKey.getRaw("Sentinel.playerNameTargets");
        if (storageKey.keyExists("Sentinel.playerNameIgnores"))
            oLoc.playerNameIgnores = (ArrayList<String>) storageKey.getRaw("Sentinel.playerNameIgnores");
        if (storageKey.keyExists("Sentinel.npcNameTargets"))
            oLoc.npcNameTargets = (ArrayList<String>) storageKey.getRaw("Sentinel.npcNameTargets");
        if (storageKey.keyExists("Sentinel.npcNameIgnores"))
            oLoc.npcNameIgnores = (ArrayList<String>) storageKey.getRaw("Sentinel.npcNameIgnores");
        if (storageKey.keyExists("Sentinel.entityNameTargets"))
            oLoc.entityNameTargets = (ArrayList<String>) storageKey.getRaw("Sentinel.entityNameTargets");
        if (storageKey.keyExists("Sentinel.entityNameIgnores"))
            oLoc.entityNameIgnores = (ArrayList<String>) storageKey.getRaw("Sentinel.entityNameIgnores");

        if (storageKey.keyExists("Sentinel.heldItemTargets"))
            oLoc.heldItemTargets = (ArrayList<String>) storageKey.getRaw("Sentinel.heldItemTargets");
        if (storageKey.keyExists("Sentinel.heldItemIgnores"))
            oLoc.heldItemIgnores = (ArrayList<String>) storageKey.getRaw("Sentinel.heldItemIgnores");

        if (storageKey.keyExists("Sentinel.groupTargets"))
            oLoc.groupTargets = (ArrayList<String>) storageKey.getRaw("Sentinel.groupTargets");
        if (storageKey.keyExists("Sentinel.groupIgnores"))
            oLoc.groupIgnores = (ArrayList<String>) storageKey.getRaw("Sentinel.groupIgnores");
        if (storageKey.keyExists("Sentinel.eventTargets"))
            oLoc.eventTargets = (ArrayList<String>) storageKey.getRaw("Sentinel.eventTargets");

        if (storageKey.keyExists("Sentinel.drops"))
            oLoc.drops = (ArrayList<ItemStack>) storageKey.getRaw("Sentinel.drops");

        if (storageKey.keyExists("Sentinel.autoswitch"))
            oLoc.autoswitch = storageKey.getBoolean("Sentinel.autoswitch");
        if (storageKey.keyExists("Sentinel.squad"))
            oLoc.squad = storageKey.getString("Sentinel.squad");

        oLoc.locationID = location.LocationIdent;
        npcSetting.locations.put(location.LocationIdent, oLoc);
        return;
    }

    public void onLocationSaving(NPC npc, NPCDestinationsTrait npcTrait, Destination_Setting location, DataKey storageKey) {
        if (!npcSettings.containsKey(npc.getId()))
            return;
        if (!npcSettings.get(npc.getId()).locations.containsKey(location.LocationIdent))
            return;

        Sentinel_LocationSetting locationConfig = npcSettings.get(npc.getId()).locations.get(location.LocationIdent);

        if (locationConfig.locationID != null) {
            // V1.39 - Sentinel
            storageKey.setLong("Sentinel.lastSet", locationConfig.lastSet.getTime());

            storageKey.setDouble("Sentinel.range", locationConfig.range);
            storageKey.setDouble("Sentinel.damage", locationConfig.damage);
            storageKey.setDouble("Sentinel.armor", locationConfig.armor);
            storageKey.setDouble("Sentinel.health", locationConfig.health);
            storageKey.setDouble("Sentinel.chaseRange", locationConfig.chaseRange);
            storageKey.setDouble("Sentinel.greetRange", locationConfig.greetRange);
            storageKey.setDouble("Sentinel.accuracy", locationConfig.accuracy);
            storageKey.setDouble("Sentinel.speed", locationConfig.speed);

            storageKey.setBoolean("Sentinel.rangedChase", locationConfig.rangedChase);
            storageKey.setBoolean("Sentinel.closeChase", locationConfig.closeChase);
            storageKey.setBoolean("Sentinel.invincible", locationConfig.invincible);
            storageKey.setBoolean("Sentinel.fightback", locationConfig.fightback);
            storageKey.setBoolean("Sentinel.needsAmmo", locationConfig.needsAmmo);
            storageKey.setBoolean("Sentinel.safeShot", locationConfig.safeShot);
            storageKey.setBoolean("Sentinel.enemyDrops", locationConfig.enemyDrops);

            storageKey.setInt("Sentinel.attackRate", locationConfig.attackRate);
            storageKey.setInt("Sentinel.healRate", locationConfig.healRate);

            storageKey.setLong("Sentinel.guardingUpper", locationConfig.guardingUpper);
            storageKey.setLong("Sentinel.guardingLower", locationConfig.guardingLower);
            storageKey.setLong("Sentinel.respawnTime", locationConfig.respawnTime);
            storageKey.setLong("Sentinel.enemyTargetTime", locationConfig.enemyTargetTime);

            storageKey.setString("Sentinel.warningText", locationConfig.warningText);
            storageKey.setString("Sentinel.greetingText", locationConfig.greetingText);

            storageKey.setRaw("Sentinel.targets", locationConfig.targets);
            storageKey.setRaw("Sentinel.ignores", locationConfig.ignores);
            storageKey.setRaw("Sentinel.playerNameTargets", locationConfig.playerNameTargets);
            storageKey.setRaw("Sentinel.playerNameIgnores", locationConfig.playerNameIgnores);
            storageKey.setRaw("Sentinel.npcNameTargets", locationConfig.npcNameTargets);
            storageKey.setRaw("Sentinel.npcNameIgnores", locationConfig.npcNameIgnores);
            storageKey.setRaw("Sentinel.entityNameTargets", locationConfig.entityNameTargets);
            storageKey.setRaw("Sentinel.entityNameIgnores", locationConfig.entityNameIgnores);
            storageKey.setRaw("Sentinel.heldItemTargets", locationConfig.heldItemTargets);
            storageKey.setRaw("Sentinel.heldItemIgnores", locationConfig.heldItemIgnores);
            storageKey.setRaw("Sentinel.groupTargets", locationConfig.groupTargets);
            storageKey.setRaw("Sentinel.groupIgnores", locationConfig.groupIgnores);
            storageKey.setRaw("Sentinel.eventTargets", locationConfig.eventTargets);

            storageKey.setRaw("Sentinel.drops", locationConfig.drops);
            storageKey.setRaw("Sentinel.squad", locationConfig.squad);
            storageKey.setRaw("Sentinel.autoswitch", locationConfig.autoswitch);
        }
        return;
    }

    public boolean onNavigationReached(NPC npc, NPCDestinationsTrait npcTrait, Destination_Setting location) {
        if (npcSettings.containsKey(npc.getId())) {
            if (npcSettings.get(npc.getId()).locations.containsKey(location.LocationIdent)) {
                pluginReference.destRef.getMessageManager.debugMessage(Level.INFO, "DestinationsEventsListener.onNavigationReached|NPC:" + npc.getId() + "|Monitored location reached, setting sentinel settings");
                pluginReference.setCurrentSettings(npc, npcSettings.get(npc.getId()).locations.get(location.LocationIdent));
            }
        }
        return false;
    }

    public boolean onNewDestination(NPC npc, NPCDestinationsTrait npcTrait, Destination_Setting location) {
        return false;
    }

    public void onEnableChanged(NPC npc, NPCDestinationsTrait npcTrait, boolean enabled) {
        return;
    }
}
