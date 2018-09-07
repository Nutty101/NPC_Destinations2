package net.livecar.nuttyworks.npc_destinations.bridges;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Openable;

public class MCUtil_1_9_R2 implements MCUtilsBridge {

    List<Material> metalDoors = null;
    List<Material> woodDoors  = null;
    List<Material> gates      = null;
    List<Material> fence      = null;

    public MCUtil_1_9_R2() {
        gates = new ArrayList<Material>();
        gates.add(Material.BIRCH_FENCE_GATE);
        gates.add(Material.ACACIA_FENCE_GATE);
        gates.add(Material.DARK_OAK_FENCE_GATE);
        gates.add(Material.FENCE_GATE);
        gates.add(Material.SPRUCE_FENCE_GATE);

        woodDoors = new ArrayList<Material>();
        woodDoors.add(Material.WOODEN_DOOR);
        woodDoors.add(Material.WOOD_DOOR);
        woodDoors.add(Material.ACACIA_DOOR);
        woodDoors.add(Material.BIRCH_DOOR);
        woodDoors.add(Material.DARK_OAK_DOOR);
        woodDoors.add(Material.SPRUCE_DOOR);
        woodDoors.add(Material.JUNGLE_DOOR);
        woodDoors.add(Material.TRAP_DOOR);

        metalDoors = new ArrayList<Material>();
        metalDoors.add(Material.IRON_DOOR);
        metalDoors.add(Material.IRON_DOOR_BLOCK);
        metalDoors.add(Material.IRON_TRAPDOOR);

        fence = new ArrayList<Material>();
        fence.add(Material.FENCE);
        fence.add(Material.ACACIA_FENCE);
        fence.add(Material.BIRCH_FENCE);
        fence.add(Material.DARK_OAK_FENCE);
        fence.add(Material.IRON_FENCE);
        fence.add(Material.JUNGLE_FENCE);
        fence.add(Material.SPRUCE_FENCE);
        fence.add(Material.COBBLE_WALL);
    }

    @SuppressWarnings("deprecation")
    public boolean isLocationWalkable(Location l, Boolean openGates, Boolean openWoodDoors, Boolean openMetalDoors) {
        Block b = l.getBlock();

        // Gates
        if (gates.contains(b.getRelative(0, 1, 0).getType()) && openGates) {
            if (gates.contains(b.getRelative(0, 2, 0).getType()) && openGates)
                return true;
            if (!b.getRelative(0, 2, 0).getType().isSolid())
                return true;
        }

        // Wood Doors
        if (woodDoors.contains(b.getRelative(0, 1, 0).getType()) && openWoodDoors) {
            if (woodDoors.contains(b.getRelative(0, 2, 0).getType()) && openWoodDoors)
                return true;
            if (!b.getRelative(0, 2, 0).getType().isSolid())
                return true;
        }

        // metal Doors
        if (metalDoors.contains(b.getRelative(0, 1, 0).getType()) && openMetalDoors) {
            if (metalDoors.contains(b.getRelative(0, 2, 0).getType()) && openMetalDoors)
                return true;
            if (!b.getRelative(0, 2, 0).getType().isSolid())
                return true;
        }

        if (!b.getType().isSolid() && b.getType() != Material.WATER_LILY)
            return false;

        // No walking on top of fences
        if (fence.contains(b.getType())) {
            if (!b.getRelative(0, 1, 0).getType().isSolid() && !b.getRelative(0, 2, 0).getType().isSolid()) {
                return false;
            }
        }

        // Validate liquid on the block above
        if (b.getRelative(0, 1, 0).isLiquid()) {
            if (b.getRelative(0, 1, 0).getType() == Material.LAVA)
                return false;

            BlockState bs = b.getRelative(0, 1, 0).getState();
            if (bs.getData().getData() > 3)
                return false;
        }

        // Slabs
        if (b.getType().toString().contains("SLAB") || b.getType().toString().contains("STEP")) {
            if (b.getData() < 8) {
                // Lower Slab, validate 2 above is a slab, if upper, we can walk
                if (b.getRelative(0, 2, 0).getType().toString().contains("SLAB") || b.getRelative(0, 2, 0).getType().toString().contains("STEP"))
                    if (b.getRelative(0, 2, 0).getData() > 7)
                        return true;
            }
        }

        // Validate if the blocks above are solid.
        if (b.getRelative(0, 1, 0).getType().isSolid())
            return false;

        if (b.getRelative(0, 2, 0).getType().isSolid())
            return false;

        if (b.getType().toString().contains("STAIR")) {
            return !b.getRelative(0, 3, 0).getType().isSolid();
        }

        return true;
    }

    public boolean requiresOpening(Location l) {
        Block b = l.getBlock();

        // Gates
        if (gates.contains(b.getRelative(0, 1, 0).getType()))
            return true;

        // Wood Doors
        if (woodDoors.contains(b.getRelative(0, 1, 0).getType()))
            return true;

        // metal Doors
        return metalDoors.contains(b.getRelative(0, 1, 0).getType());

    }


    @Override
    public boolean isHalfBlock(Material mat) {
        return mat.toString().contains("SLAB") || mat.toString().contains("STEP");
    }

    @SuppressWarnings("deprecation")
	@Override
    public SLABTYPE getSlabType(Block block)
    {
        if (block.getType() == Material.DOUBLE_STEP || block.getType() == Material.DOUBLE_STONE_SLAB2 || block.getType() == Material.WOOD_DOUBLE_STEP)
        	return SLABTYPE.DOUBLE;
        
        if (block.getType() == Material.STEP || block.getType() == Material.WOOD_STEP)
        {
        	if (block.getData() < 8)
        		return SLABTYPE.BOTTOM;
        	else 
        		return SLABTYPE.TOP;
        }
        
        return SLABTYPE.NONSLAB;
    }
    
    @Override
    public boolean isGate(Material mat) {
        return gates.contains(mat);
    }

    @Override
    public boolean isWoodDoor(Material mat) {
        return woodDoors.contains(mat);
    }

    @Override
    public boolean isMetalDoor(Material mat) {
        return metalDoors.contains(mat);
    }

    @Override
    public inHandLightSource isHoldingTorch(Material mat) {
        if (mat == Material.REDSTONE_TORCH_OFF || mat == Material.REDSTONE_TORCH_ON)
            return inHandLightSource.REDSTONE_TORCH;

        if (mat == Material.TORCH)
            return inHandLightSource.WOODEN_TORCH;

        return inHandLightSource.NOLIGHT;
    }

    @Override
    public void closeOpenable(Block oBlock) {
        BlockState oBlockState = oBlock.getState();
        Openable oOpenable = (Openable) oBlockState.getData();

        if (oOpenable.isOpen()) {
            oOpenable.setOpen(false);
            oBlockState.setData((MaterialData) oOpenable);
            oBlockState.update();
        }
    }

    @Override
    public void openOpenable(Block oBlock) {
        BlockState oBlockState = oBlock.getState();
        Openable oOpenable = (Openable) oBlockState.getData();

        if (oOpenable.isOpen()) {
            oOpenable.setOpen(true);
            oBlockState.setData((MaterialData) oOpenable);
            oBlockState.update();
        }
    }

    @Override
    public ItemStack getMainHand(Player plr) {
        return plr.getInventory().getItemInMainHand();
    }

    @Override
    public ItemStack getSecondHand(Player plr) {
        return plr.getInventory().getItemInOffHand();
    }

}