package com.forgeessentials.commands.util;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.forgeessentials.commons.selections.Point;
import com.forgeessentials.core.misc.TaskRegistry;
import com.forgeessentials.core.misc.TaskRegistry.ITickTask;
import com.forgeessentials.util.OutputHandler;

import cpw.mods.fml.common.registry.GameData;

public class TickTaskBlockFinder implements ITickTask {

    private World world;
    private EntityPlayer player;
    private Block block;
    private String blockName;
    
    private int meta;
    private int targetRange;
    private int targetAmount;
    private int centerX, centerZ;
    private ItemStack stack;
    private int speed;

    // (di, dj) is a vector - direction in which we move right now
    private int di = 1;
    private int dj = 0;
    // length of current segment
    private int segment_length = 1;

    // current position (i, j) and how much of current segment we passed
    private int i = 0;
    private int j = 0;
    private int segment_passed = 0;

    ArrayList<Point> results = new ArrayList<Point>();

    public TickTaskBlockFinder(EntityPlayer player, String id, int meta, int range, int amount, int speed)
    {
        this.player = player;
        this.meta = meta;
        this.targetRange = range;
        this.targetAmount = amount;
        this.speed = speed;
        this.centerX = (int) player.posX;
        this.centerZ = (int) player.posZ;
        world = player.worldObj;

        block = GameData.getBlockRegistry().getObject(id);
        if (block == null) {
            try
            {
                int intId = Integer.parseInt(id);
                block = GameData.getBlockRegistry().getRaw(intId);
            }
            catch (NumberFormatException e)
            {
                /* ignore */
            }
        }
        if (block == null) {
            msg("Error: " + id + ":" + meta + " unkown.");
            return;
        }
        
        stack = new ItemStack(block, 1, meta);
        blockName = stack.getItem() != null ? stack.getDisplayName() : GameData.getBlockRegistry().getNameForObject(block);

        msg("Start the hunt for " + blockName);
        TaskRegistry.getInstance().schedule(this);
    }

    @Override
    public void tick()
    {
        int speedcounter = 0;
        while (!isComplete() && speedcounter < speed)
        {
            speedcounter++;

            int y = world.getActualHeight();
            while (!isComplete() && y >= 0)
            {
                Block b = world.getBlock(centerX + i, y, centerZ + j);
                if (b.equals(block) && (meta == -1 || world.getBlockMetadata(centerX + i, y, centerZ + j) == meta))
                {
                    Point p = new Point(centerX + i, y, centerZ + j);
                    results.add(p);
                    msg("Found " + blockName + " at " + p.getX() + ";" + p.getY() + ";" + p.getZ());

                }
                y--;
            }

            // make a step, add 'direction' vector (di, dj) to current position (i, j)
            i += di;
            j += dj;
            ++segment_passed;

            if (segment_passed == segment_length)
            {
                // done with current segment
                segment_passed = 0;

                // 'rotate' directions
                int buffer = di;
                di = -dj;
                dj = buffer;

                // increase segment length if necessary
                if (dj == 0)
                {
                    ++segment_length;
                }
            }
        }
    }

    private void msg(String string)
    {
        OutputHandler.chatNotification(player, string);
    }

    @Override
    public void onComplete()
    {
        if (results.isEmpty())
        {
            msg("Found nothing withing target range.");
        }
        else
        {
            msg("Stoped looking for " + blockName);
        }
    }

    @Override
    public boolean isComplete()
    {
        return results.size() >= targetAmount || segment_length > targetRange;
    }

    @Override
    public boolean editsBlocks()
    {
        return false;
    }
}