package net.livecar.nuttyworks.npc_destinations.citizens;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.api.Destination_Setting;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.material.Openable;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class NPCDestinationsTrait extends Trait {
    @Persist
    public int          PauseForPlayers          = 5;
    @Persist
    public int          PauseTimeout             = 25;
    // @Persist public Boolean LookOneBlockDown = false;
    @Persist
    public int          blocksUnderSurface       = 0;
    // Format X:Y:Z:TIMEOFDAY -Old, will convert this to new format on loading
    @Persist
    public List<String> Locations                = new ArrayList<String>();
    @Persist
    public Boolean      OpensGates               = false;
    @Persist
    public Boolean      OpensWoodDoors           = false;
    @Persist
    public Boolean      OpensMetalDoors          = false;
    @Persist
    public Boolean      TeleportOnFailedStartLoc = true;
    @Persist
    public Boolean      TeleportOnNoPath         = true;
    @Persist
    public int          MaxDistFromDestination   = 2;

    public enum en_CurrentAction {
        RANDOM_MOVEMENT, PATH_HUNTING, PATH_FOUND, TRAVELING, IDLE, IDLE_FAILED,
    }

    public enum en_RequestedAction {
        NORMAL_PROCESSING, NO_PROCESSING, SET_LOCATION,
    }

    public List<Destination_Setting> NPCLocations                = new ArrayList<Destination_Setting>();
    public String                    lastResult                  = "Idle";
    public List<Material>            AllowedPathBlocks           = new ArrayList<Material>();
    public Date                      lastPositionChange;
    public Date                      lastPlayerPause;
    public Location                  lastPauseLocation;
    public Location                  lastNavigationPoint;
    public Date                      lastPathCalc;
    public Destination_Setting       currentLocation             = new Destination_Setting();
    public Destination_Setting       setLocation                 = new Destination_Setting();
    public Destination_Setting       lastLocation                = new Destination_Setting();
    public Destination_Setting       monitoredLocation           = null;
    public Date                      locationLockUntil;
    public List<String>              enabledPlugins              = new ArrayList<String>();
    public Boolean                   citizens_Swim               = true;
    public Boolean                   citizens_NewPathFinder      = true;
    public Boolean                   citizens_AvoidWater         = true;
    public Boolean                   citizens_DefaultStuck       = true;
    public Double                    citizens_DistanceMargin     = 1D;
    public Double                    citizens_PathDistanceMargin = 1D;

    public Location                  lastLighting_Loc            = null;
    public Date                      lastLighting_Time           = null;
    public Integer                   lightTask                   = 0;
    public Long                      processingTime              = 0L;
    public Long                      blocksPerSec                = 0L;

    public Integer                   maxProcessingTime           = -1;

    // Inner namespace variables
    ArrayList<Location>              pendingDestinations         = new ArrayList<Location>();
    ArrayList<Location>              processedDestinations       = new ArrayList<Location>();
    ArrayList<Block>                 openedObjects               = new ArrayList<Block>();
    en_CurrentAction                 currentAction               = en_CurrentAction.IDLE;
    en_RequestedAction               requestedAction             = en_RequestedAction.NORMAL_PROCESSING;
    private Plugin                   monitoringPlugin            = null;

    UUID                             last_Loc_Reached;

    // Public methods
    public NPCDestinationsTrait() {
        super("npcdestinations");
        this.lastPositionChange = new Date();
        this.lastPlayerPause = new Date();
    }

    @Override
    public void onAttach() {
        load(new net.citizensnpcs.api.util.MemoryDataKey());
    }

    public Plugin getMonitoringPlugin() {
        return monitoringPlugin;
    }

    public void unsetMonitoringPlugin(String reason) {
        if (DestinationsPlugin.Instance.debugTargets != null) {
            if (monitoringPlugin != null)
                DestinationsPlugin.Instance.getMessageManager.sendDebugMessage("destinations", "Debug_Messages.trait_unmonitored", npc, monitoringPlugin.getName() + (reason.equals("") ? "" : "(" + reason + ")"));
        }
        monitoringPlugin = null;
        monitoredLocation = null;
    }

    public void unsetMonitoringPlugin() {
        unsetMonitoringPlugin("");
    }

    public void setMonitoringPlugin(Plugin plugin, Destination_Setting monitoredDestination) {
        monitoringPlugin = plugin;
        monitoredLocation = monitoredDestination;
        if (monitoringPlugin != null)
            DestinationsPlugin.Instance.getMessageManager.sendDebugMessage("destinations", "Debug_Messages.trait_monitored", npc, monitoringPlugin.getName());
    }

    public Destination_Setting GetCurrentLocation() {
        return GetCurrentLocation(false);
    }

    public Destination_Setting GetCurrentLocation(Boolean noNull) {
        Destination_Setting locReturn = Citizens_Processing.trait_getCurLocation(this, noNull);
        return locReturn;
    }

    public void setRequestedAction(en_RequestedAction action) {
        Citizens_Processing.debugMessage(Level.FINE, "NPCDestinations_Trait.setRequestedAction()|NPC:" + this.npc.getId() + "|" + action.toString());
        this.requestedAction = action;
    }

    public void setPendingDestinations(ArrayList<Location> newDestinations) {
        if (pendingDestinations.size() > 0) {
            clearPendingDestinations();
            this.processedDestinations.clear();
        }
        pendingDestinations = newDestinations;
    }

    public ArrayList<Location> getPendingDestinations() {
        return pendingDestinations;
    }

    public void removePendingDestination(int index) {
        Citizens_Processing.trait_removePendingDestination(this, index);
        this.processedDestinations.add(this.pendingDestinations.get(index));
        this.pendingDestinations.remove(index);
    }

    public void clearPendingDestinations() {
        Citizens_Processing.trait_clearPendingDestinations(this);
        this.pendingDestinations.clear();
        this.processedDestinations.clear();
    }

    @Override
    public void load(DataKey key) {
        Citizens_Processing.trait_loadSettings(this, key);
    }

    @Override
    public void save(DataKey key) {
        Citizens_Processing.trait_saveSettings(this, key);
    }

    public en_RequestedAction getRequestedAction() {
        return this.requestedAction;
    }

    public void setCurrentAction(en_CurrentAction action) {
        Citizens_Processing.debugMessage(Level.FINE, "NPCDestinations_Trait.setCurrentAction()|NPC:" + this.npc.getId() + "|" + action.toString() + Arrays.toString(Thread.currentThread().getStackTrace()));
        this.currentAction = action;
    }

    public en_CurrentAction getCurrentAction() {
        return this.currentAction;
    }

    public void locationReached() {
        Citizens_Processing.trait_locationReached(this);
    }

    public void setCurrentLocation(Destination_Setting location) {
        if (this.currentLocation.destination == null) {
            if (location.destination.distanceSquared(this.npc.getEntity().getLocation()) > 5) {
                this.currentLocation = location;
            } else {
                this.currentLocation = location;
                this.locationReached();
            }
        } else
            this.currentLocation = location;
    }

    public Destination_Setting getCurrentLocation() {
        if (this.currentLocation == null)
            return new Destination_Setting();
        return this.currentLocation;
    }

    public void processOpenableObjects() {
        for (Iterator<Block> iterator = openedObjects.iterator(); iterator.hasNext();) {
            Block opened = iterator.next();
            if (opened.getLocation().distanceSquared(this.npc.getEntity().getLocation()) > 4 || (this.pendingDestinations.size() == 0 && this.processedDestinations.size() == 0)) {
                closeOpenable(opened);
                iterator.remove();
            }
        }

        if (npc.getEntity().getLocation().getBlock().getState().getData() instanceof Openable) {
            if (!openedObjects.contains(npc.getEntity().getLocation().getBlock())) {
                Block oBlock = npc.getEntity().getLocation().getBlock();
                if (oBlock.getRelative(0, -1, 0).getState().getData() instanceof Openable) {
                    oBlock = oBlock.getRelative(0, -1, 0);
                } else if (oBlock.getRelative(0, 1, 0).getState().getData() instanceof Openable) {
                    oBlock = oBlock.getRelative(0, 1, 0);
                }
                this.openOpenable(oBlock);
            }
        }
        getOpenableInFront();

    }

    private void closeOpenable(Block oBlock) {
        DestinationsPlugin.Instance.getMCUtils.closeOpenable(oBlock);
    }

    private void openOpenable(Block oBlock) {
        BlockState oBlockState = oBlock.getState();
        Openable oOpenable = (Openable) oBlockState.getData();

        if (DestinationsPlugin.Instance.getMCUtils.isGate(oBlock.getType()) && OpensGates)
        {
            if (!oOpenable.isOpen()) {
                DestinationsPlugin.Instance.getMCUtils.openOpenable(oBlock);
                this.openedObjects.add(oBlock);
            }
        } else if (DestinationsPlugin.Instance.getMCUtils.isWoodDoor(oBlock.getType()) && OpensWoodDoors) {
            if (!oOpenable.isOpen()) {
                DestinationsPlugin.Instance.getMCUtils.openOpenable(oBlock);
                this.openedObjects.add(oBlock);
            }
        } else if (DestinationsPlugin.Instance.getMCUtils.isMetalDoor(oBlock.getType()) && OpensMetalDoors) {
            if (!oOpenable.isOpen()) {
                DestinationsPlugin.Instance.getMCUtils.openOpenable(oBlock);
                this.openedObjects.add(oBlock);
            }
        }
    }
    
    private void getOpenableInFront()
    {
        //Validate is the NPC is in the same block as an openable
        if (npc.getEntity().getLocation().getBlock().getState().getData() instanceof Openable)
        {
            final Openable openableBlock = (Openable) npc.getEntity().getLocation().getBlock().getState().getData();
            if (!openableBlock.isOpen())
            {
                this.openOpenable(npc.getEntity().getLocation().getBlock());
                return;
            }
        }

        
        
        int xAxis = 0;
        int zAxis = 0;

        double rotation = this.npc.getEntity().getLocation().getYaw();

        // North: -Z
        // East: +X
        // South: +Z
        // West: -X

        if (rotation < 30.0) {
            xAxis = 0;
            zAxis = 1;
        } else if (rotation < 60) {
            xAxis = -1;
            zAxis = 1;
        } else if (rotation < 120) {
            xAxis = -1;
            zAxis = 0;
        } else if (rotation < 150) {
            xAxis = -1;
            zAxis = -1;
        } else if (rotation < 210) {
            xAxis = 0;
            zAxis = -1;
        } else if (rotation < 240) {
            xAxis = 1;
            zAxis = -1;
        } else if (rotation < 300) {
            xAxis = 1;
            zAxis = 0;
        } else if (rotation < 330) {
            xAxis = 1;
            zAxis = 1;
        } else {
            xAxis = 0;
            zAxis = 1;
        }
        
        for (byte y = -1; y <= 1; y++) {
            final Location openableLocation = this.npc.getEntity().getLocation().add(xAxis, y, zAxis);
            final Block openableBlock = openableLocation.getBlock();
            
            if (openableBlock.getState().getData() instanceof Openable)
            {
                if (!openedObjects.contains(openableBlock)) {
                    this.openOpenable(openableBlock);
                }
            }
        }
    }

}
