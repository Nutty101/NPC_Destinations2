package net.livecar.nuttyworks.npc_destinations.thirdpartyplugins.sentinel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

public class Sentinel_LocationSetting {
    public UUID            locationID;

    public Date            lastSet           = new Date(0);

    public double          range             = 0.0;
    public double          damage            = 0.0;
    public double          armor             = 0.0;
    public double          health            = 0.0;
    public double          chaseRange        = 0;
    public double          greetRange        = 0;
    public double          accuracy          = 0;
    public double          speed             = 0;

    public boolean         rangedChase       = false;
    public boolean         closeChase        = false;
    public boolean         invincible        = false;
    public boolean         fightback         = false;
    public boolean         needsAmmo         = false;
    public boolean         safeShot          = false;
    public boolean         enemyDrops        = false;
    public boolean         autoswitch        = false;

    public int             attackRate        = 0;
    public int             healRate          = 0;

    public long            guardingUpper     = 0;
    public long            guardingLower     = 0;
    public long            respawnTime       = 0;
    public long            enemyTargetTime   = 0;

    public String          warningText       = "";
    public String          greetingText      = "";
    public String          squad             = "";

    public List<String>    playerNameTargets = new ArrayList<>();
    public List<String>    playerNameIgnores = new ArrayList<>();
    public List<String>    npcNameTargets    = new ArrayList<>();
    public List<String>    npcNameIgnores    = new ArrayList<>();
    public List<String>    entityNameTargets = new ArrayList<>();
    public List<String>    entityNameIgnores = new ArrayList<>();
    public List<String>    heldItemTargets   = new ArrayList<>();
    public List<String>    heldItemIgnores   = new ArrayList<>();
    public List<String>    groupTargets      = new ArrayList<>();
    public List<String>    groupIgnores      = new ArrayList<>();
    public List<String>    eventTargets      = new ArrayList<>();
    public List<String>    targets           = new ArrayList<>();
    public List<String>    ignores           = new ArrayList<>();

    public List<ItemStack> drops             = new ArrayList<>();

    public void dispose() {
        playerNameTargets = null;
        playerNameIgnores = null;
        npcNameTargets = null;
        npcNameIgnores = null;
        entityNameTargets = null;
        entityNameIgnores = null;
        heldItemTargets = null;
        heldItemIgnores = null;
        groupTargets = null;
        groupIgnores = null;
        eventTargets = null;
        targets = null;
        ignores = null;
        drops = null;
    }
}
