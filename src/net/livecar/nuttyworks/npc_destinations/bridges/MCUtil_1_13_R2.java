package net.livecar.nuttyworks.npc_destinations.bridges;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MCUtil_1_13_R2 implements MCUtilsBridge {

    List<Material> metalDoors = null;
    List<Material> woodDoors  = null;
    List<Material> gates      = null;
    List<Material> fence      = null;

    public MCUtil_1_13_R2() {
        gates = new ArrayList<Material>();

        gates.add(Material.ACACIA_FENCE_GATE);
        gates.add(Material.BIRCH_FENCE_GATE);
        gates.add(Material.DARK_OAK_FENCE_GATE);
        gates.add(Material.JUNGLE_FENCE_GATE);
        gates.add(Material.OAK_FENCE_GATE);
        gates.add(Material.SPRUCE_FENCE_GATE);

        woodDoors = new ArrayList<Material>();
        woodDoors.add(Material.ACACIA_DOOR);
        woodDoors.add(Material.BIRCH_DOOR);
        woodDoors.add(Material.DARK_OAK_DOOR);
        woodDoors.add(Material.SPRUCE_DOOR);
        woodDoors.add(Material.JUNGLE_DOOR);
        woodDoors.add(Material.OAK_DOOR);
        woodDoors.add(Material.ACACIA_TRAPDOOR);
        woodDoors.add(Material.BIRCH_TRAPDOOR);
        woodDoors.add(Material.DARK_OAK_TRAPDOOR);
        woodDoors.add(Material.JUNGLE_TRAPDOOR);
        woodDoors.add(Material.OAK_TRAPDOOR);
        woodDoors.add(Material.SPRUCE_TRAPDOOR);

        metalDoors = new ArrayList<Material>();
        metalDoors.add(Material.IRON_DOOR);
        metalDoors.add(Material.IRON_TRAPDOOR);

        fence = new ArrayList<Material>();
        fence.add(Material.ACACIA_FENCE);
        fence.add(Material.BIRCH_FENCE);
        fence.add(Material.DARK_OAK_FENCE);
        fence.add(Material.OAK_FENCE);
        fence.add(Material.JUNGLE_FENCE);
        fence.add(Material.SPRUCE_FENCE);
        fence.add(Material.COBBLESTONE_WALL);
        fence.add(Material.MOSSY_COBBLESTONE_WALL);
    }

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

        if (!b.getType().isSolid() && b.getType() != Material.LILY_PAD)
            return false;

        // No walking on top of fences
        if (fence.contains(b.getType())) {
            if (!b.getRelative(0, 1, 0).getType().isSolid() && !b.getRelative(0, 2, 0).getType().isSolid()) {
                return false;
            }
        }

        // Validate liquid on the block above
        if (b.getRelative(0, 1, 0).isLiquid()) {

            if (b.getRelative(0, 1, 0) instanceof Levelled) {
                Levelled liquidLevel = (Levelled) b.getRelative(0, 1, 0).getBlockData();
                if (liquidLevel.getLevel() > 3)
                    return false;

                if (b.getRelative(0, 1, 0).getType() == Material.LAVA)
                    return false;
            }
        }

        // Slabs
        if (b.getType().toString().contains("SLAB") || b.getType().toString().contains("STEP")) {
            if (getSlabType(b) == SLABTYPE.BOTTOM) {
                // Lower Slab, validate 2 above is a slab, if upper, we can walk
                if (b.getRelative(0, 2, 0).getType().toString().contains("SLAB") || b.getRelative(0, 2, 0).getType().toString().contains("STEP"))
                    if (getSlabType(b.getRelative(0, 2, 0)) == SLABTYPE.TOP)
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

    public SLABTYPE getSlabType(Block block) {
        BlockData bd = block.getBlockData();
        if (bd instanceof Slab) {
            Slab slab = (Slab) bd;
            switch (slab.getType()) {
            case BOTTOM:
                return SLABTYPE.BOTTOM;
            case DOUBLE:
                return SLABTYPE.DOUBLE;
            case TOP:
                return SLABTYPE.TOP;
            default:
                return SLABTYPE.NONSLAB;

            }
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
        if (mat == Material.REDSTONE_TORCH)
            return inHandLightSource.REDSTONE_TORCH;

        if (mat == Material.TORCH)
            return inHandLightSource.WOODEN_TORCH;

        return inHandLightSource.NOLIGHT;
    }

    @Override
    public void closeOpenable(Block oBlock) {
        BlockState oBlockState = oBlock.getState();
        Openable oOpenable = (Openable) oBlockState.getBlockData();

        if (oOpenable.isOpen()) {
            oOpenable.setOpen(false);
            oBlockState.setBlockData((BlockData) oOpenable);
            oBlockState.update();
        }
    }

    @Override
    public void openOpenable(Block oBlock) {
        BlockState oBlockState = oBlock.getState();
        Openable oOpenable = (Openable) oBlockState.getBlockData();

        if (!oOpenable.isOpen()) {
            oOpenable.setOpen(true);
            oBlockState.setBlockData((BlockData) oOpenable);
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