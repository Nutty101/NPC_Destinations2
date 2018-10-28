package net.livecar.nuttyworks.npc_destinations.bridges;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface MCUtilsBridge {
    
    //Path Assistance
    boolean isLocationWalkable(Location l, Boolean gates, Boolean woodDoors, Boolean metalDoors);
    boolean isHalfBlock(Material mat);
    boolean isGate(Material mat);
    boolean isWoodDoor(Material mat);
    boolean isMetalDoor(Material mat);
    boolean requiresOpening(Location l);
    void sendClientBlock(Player target, Location blockLocation, Material material);
    boolean isHoldingBook(Player player);
    
    inHandLightSource isHoldingTorch(Material mat);
    
    SLABTYPE getSlabType(Block block);
    
    ItemStack getMainHand(Player plr);
    ItemStack getSecondHand(Player plr);
    
    void closeOpenable(Block oBlock);
    void openOpenable(Block oBlock);
    
    enum inHandLightSource
    {
        REDSTONE_TORCH,
        WOODEN_TORCH,
        NOLIGHT
    }
    
    public enum SLABTYPE {
    	TOP,
    	BOTTOM,
    	DOUBLE,
    	NONSLAB
    }
    
}
