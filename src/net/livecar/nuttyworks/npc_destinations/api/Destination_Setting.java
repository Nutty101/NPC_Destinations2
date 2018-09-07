package net.livecar.nuttyworks.npc_destinations.api;

import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import net.livecar.nuttyworks.npc_destinations.api.enumerations.TriBoolean;

public class Destination_Setting {

    public UUID         LocationIdent;
    public String       Alias_Name;
    public int          TimeOfDay;
    public int          Probability;
    public int          Time_Minimum;
    public int          Time_Maximum;
    public Location     destination;
    public Boolean      Wandering_UseBlocks         = false;
    public int          Wait_Minimum;
    public int          Wait_Maximum;
    public int          Pause_Distance              = 5;
    public int          Pause_Timeout               = 25;
    public String       Pause_Type                  = "";
    public Boolean      items_Clear                 = false;
    public ItemStack    items_Head;
    public ItemStack    items_Chest;
    public ItemStack    items_Legs;
    public ItemStack    items_Boots;
    public ItemStack    items_Hand;
    public ItemStack    items_Offhand;

    public TriBoolean   citizens_Swim               = TriBoolean.NotSet;
    public TriBoolean   citizens_NewPathFinder      = TriBoolean.NotSet;
    public TriBoolean   citizens_AvoidWater         = TriBoolean.NotSet;
    public TriBoolean   citizens_DefaultStuck       = TriBoolean.NotSet;
    public Double       citizens_DistanceMargin     = -1D;
    public Double       citizens_PathDistanceMargin = -1D;

    public int          WeatherFlag;

    public String       player_Skin_Texture_Signature;
    public String       player_Skin_Texture_Metadata;
    public String       player_Skin_Name;
    public String       player_Skin_UUID;
    public Boolean      player_Skin_ApplyOnArrival;

    public List<String> arrival_Commands;
    public String       managed_Location            = "";
    public String       Wandering_Region            = "";

    private double      Max_Distance;
    private double      Max_DistanceSquared;
    private double      Wandering_Distance;
    private double      Wandering_DistanceSquared;

    public Destination_Setting() {
        citizens_Swim = TriBoolean.NotSet;
        citizens_NewPathFinder = TriBoolean.NotSet;
        citizens_AvoidWater = TriBoolean.NotSet;
        citizens_DefaultStuck = TriBoolean.NotSet;
        citizens_DistanceMargin = -1D;
        citizens_PathDistanceMargin = -1D;
    }

    public double getWanderingDistance() {
        return Wandering_Distance;
    }

    public double getWanderingDistanceSquared() {
        return Wandering_DistanceSquared;
    }

    public void setWanderingDistance(double distance) {
        Wandering_Distance = distance;
        this.Wandering_DistanceSquared = distance * distance;
    }

    public void setMaxDistance(double distance) {
        Max_Distance = distance;
        this.Max_DistanceSquared = distance * distance;
    }

    public double getMaxDistance() {
        return Max_Distance;
    }

    public double getMaxDistanceSquared() {
        return Max_DistanceSquared;
    }

    public void dispose() {
        arrival_Commands = null;
    }
}
