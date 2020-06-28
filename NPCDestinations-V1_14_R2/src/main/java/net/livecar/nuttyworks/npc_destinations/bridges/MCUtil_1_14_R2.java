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
import org.bukkit.block.data.type.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftLivingEntity;

import net.minecraft.server.v1_14_R1.EntityInsentient;
import net.minecraft.server.v1_14_R1.EntityLiving;

public class MCUtil_1_14_R2 implements MCUtilsBridge {

    public MCUtil_1_14_R2() {
    }

    public boolean isLocationWalkable(Location l, Boolean openGates, Boolean openWoodDoors, Boolean openMetalDoors) {
        Block b = l.getBlock();
    
        // Gates
        if (!openGates && (isGate(b.getType()) || isGate(b.getRelative(0, 1, 0).getType())))
            return false;
    
        if (isGate(b.getRelative(0, 1, 0).getType())) {
            if (isGate(b.getRelative(0, 2, 0).getType()) && openGates)
                return true;
            if (!b.getRelative(0, 2, 0).getType().isSolid())
                return true;
        }
    
        // Wood Doors
        if (isWoodDoor(b.getRelative(0, 1, 0).getType()) && openWoodDoors) {
            if (isWoodDoor(b.getRelative(0, 2, 0).getType()) && openWoodDoors)
                return true;
            if (!b.getRelative(0, 2, 0).getType().isSolid())
                return true;
        }
    
        // metal Doors
        if (isMetalDoor(b.getRelative(0, 1, 0).getType()) && openMetalDoors) {
            if (isMetalDoor(b.getRelative(0, 2, 0).getType()) && openMetalDoors)
                return true;
            if (!b.getRelative(0, 2, 0).getType().isSolid())
                return true;
        }
    
        if (!b.getType().isSolid() && b.getType() != Material.LILY_PAD)
            return false;
    
        // No walking on top of fences
        if (isFence(b.getType())) {
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
        if (isGate(b.getRelative(0, 1, 0).getType()))
            return true;
        
        // Wood Doors
        if (isWoodDoor(b.getRelative(0, 1, 0).getType()))
            return true;
        
        // metal Doors
        return isMetalDoor(b.getRelative(0, 1, 0).getType());
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
    public boolean isGate(Material mat)
    {
        if (mat.getClass().isAssignableFrom(Gate.class))
            return true;
        return false;
    }
    
    @Override
    public boolean isWoodDoor(Material mat) {
        if (mat.getClass().isAssignableFrom(Door.class)) {
            if (!mat.toString().contains("METAL"))
                return true;
        }
        return false;
    }
    
    @Override
    public boolean isMetalDoor(Material mat) {
        if (mat.getClass().isAssignableFrom(Door.class)) {
            if (mat.toString().contains("METAL"))
                return true;
        }
        return false;
    }
    
    public boolean isFence(Material mat)
    {
        if (mat.getClass().isAssignableFrom(Fence.class))
            return true;
        return false;
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
    public boolean setTargetLocation(Entity entity, Double x, Double y, Double z, Float speed) {
        EntityLiving nmsEntity = ((CraftLivingEntity) entity).getHandle();
        if (nmsEntity instanceof EntityInsentient) {
            ((EntityInsentient) nmsEntity).getNavigation().a(x, y, z, speed);
            return true;
        }
        return false;
    }

    @Override
    public ItemStack getMainHand(Player plr) {
        return plr.getInventory().getItemInMainHand();
    }

    @Override
    public ItemStack getSecondHand(Player plr) {
        return plr.getInventory().getItemInOffHand();
    }

    @Override
    public void sendClientBlock(Player target, Location blockLocation, Material material) {
        if (material == null)
        {
            BlockData bData = blockLocation.getBlock().getBlockData().clone();
            if (blockLocation.getBlock().getBlockData() instanceof Bed)
            {
                //Why is this not sending right??
                Bed tmpBed = ((Bed)bData);
                
                ((Bed)bData).setFacing(((Bed)blockLocation.getBlock().getBlockData()).getFacing());
                ((Bed)bData).setPart(((Bed)blockLocation.getBlock().getBlockData()).getPart());
            }
            target.sendBlockChange(blockLocation, bData);
        }
        else
        {
            BlockData bData = material.createBlockData();
            target.sendBlockChange(blockLocation, bData);
        }
    }

    @Override
    public boolean isHoldingBook(Player player) {
        switch (player.getInventory().getItemInMainHand().getType()) {
        case WRITTEN_BOOK:
        case WRITABLE_BOOK:
        case BOOK:
            return true;
        default:
            return false;
        }
    }

}