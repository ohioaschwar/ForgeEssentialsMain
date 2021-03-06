package com.forgeessentials.util;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;

public class MiscEventHandler {
    public static boolean MajoritySleep = false;

    public static int majoritySleepThreshold;

    public static boolean checkSpacesInNames;
    private static MiscEventHandler instance;

    public MiscEventHandler()
    {
        MinecraftForge.EVENT_BUS.register(this);
        if (checkSpacesInNames)
        {
            FMLCommonHandler.instance().bus().register(this);
        }
        instance = this;
    }

	/*
     * MajoritySleep
	 */

    public static MiscEventHandler instance()
    {
        return instance;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void checkPlayerName(PlayerLoggedInEvent e)
    {
        String name = e.player.getDisplayName();
        if (name.contains(" "))
        {
            ((EntityPlayerMP) e.player).playerNetServerHandler.kickPlayerFromServer("Invalid name. Please change your name.");
        }
    }

    @SubscribeEvent
    public void playerSleepInBedEvent(PlayerSleepInBedEvent e)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            return;
        }

        if (MajoritySleep && FMLCommonHandler.instance().getEffectiveSide().isServer())
        {
            WorldServer world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServers[0];
            if (world.getWorldInfo().getWorldTime() % 24000L < 12000L){
                return;
            }
            int playersT = FMLCommonHandler.instance().getMinecraftServerInstance().getCurrentPlayerCount();
            int playersS = 1;
            for (Object obj : FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList)
            {
                EntityPlayerMP player = (EntityPlayerMP) obj;
                if (player.isPlayerSleeping())
                {
                    playersS++;
                }
            }

            float percent = playersS * 100.0f / playersT;
            OutputHandler.felog.finer("Players sleeping: " + percent + "%");
            if (percent > majoritySleepThreshold)
            {
                long time = world.getWorldInfo().getWorldTime() + 24000L;
                world.getWorldInfo().setWorldTime(time - time % 24000L);

                for (Object obj : world.playerEntities)
                {
                    EntityPlayer var2 = (EntityPlayer) obj;

                    if (var2.isPlayerSleeping())
                    {
                        var2.wakeUpPlayer(false, false, true);
                    }
                }

                world.provider.resetRainAndThunder();
            }
        }
    }

}
