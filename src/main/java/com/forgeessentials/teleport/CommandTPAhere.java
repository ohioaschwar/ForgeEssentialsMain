package com.forgeessentials.teleport;

import com.forgeessentials.api.UserIdent;
import com.forgeessentials.core.commands.ForgeEssentialsCommandBase;
import com.forgeessentials.core.misc.TeleportHelper;
import com.forgeessentials.core.misc.TranslatedCommandException;
import com.forgeessentials.core.misc.Translator;
import com.forgeessentials.teleport.util.TPAdata;
import com.forgeessentials.util.OutputHandler;
import com.forgeessentials.commons.selections.WarpPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.permissions.PermissionsManager;
import net.minecraftforge.permissions.PermissionsManager.RegisteredPermValue;

public class CommandTPAhere extends ForgeEssentialsCommandBase {
    @Override
    public String getCommandName()
    {
        return "tpahere";
    }

    @Override
    public void processCommandPlayer(EntityPlayerMP sender, String[] args)
    {
        if (args.length == 0)
            throw new TranslatedCommandException("Improper syntax. Please try this instead: /tpahere [player] <player|<x> <y> <z>|accept|decline>");

        if (args[0].equalsIgnoreCase("accept"))
        {
            for (TPAdata data : TeleportModule.tpaList)
            {
                if (data.tphere)
                {
                    if (data.receiver.getCommandSenderName().equalsIgnoreCase(sender.getCommandSenderName()))
                    {
                        OutputHandler.chatNotification(data.sender, "Teleport request accepted.");
                        OutputHandler.chatConfirmation(data.receiver, "Teleport request accepted by other party. Teleporting..");
                        TeleportModule.tpaListToRemove.add(data);
                        TeleportHelper.teleport(data.receiver, new WarpPoint(data.sender));
                        return;
                    }
                }
            }
            return;
        }

        if (args[0].equalsIgnoreCase("decline"))
        {
            for (TPAdata data : TeleportModule.tpaList)
            {
                if (data.tphere)
                {
                    if (data.receiver.getCommandSenderName().equalsIgnoreCase(sender.getCommandSenderName()))
                    {
                        OutputHandler.chatNotification(data.sender, "Teleport request declined.");
                        OutputHandler.chatError(data.receiver, "Teleport request declined by other party.");
                        TeleportModule.tpaListToRemove.add(data);
                        return;
                    }
                }
            }
            return;
        }

        if (!PermissionsManager.checkPermission(sender, TeleportModule.PERM_TPAHERE_SENDREQUEST))
            throw new TranslatedCommandException("You have insufficient permissions to do that. If you believe you received this message in error, please talk to a server admin.");

        EntityPlayerMP receiver = UserIdent.getPlayerByMatchOrUsername(sender, args[0]);
        if (receiver == null)
            throw new TranslatedCommandException("Player %s not found", args[0]);

        TeleportModule.tpaListToAdd.add(new TPAdata(sender, receiver, true));

        OutputHandler.chatNotification(sender, Translator.format("Teleport request sent to %s", receiver.getCommandSenderName()));
        OutputHandler.chatNotification(receiver,
                Translator.format("Received teleport request from %s. Enter '/tpahere accept' to accept, '/tpahere decline' to decline.",
                        sender.getCommandSenderName()));
    }

    @Override
    public void processCommandConsole(ICommandSender sender, String[] args)
    {
    }

    @Override
    public boolean canConsoleUseCommand()
    {
        return false;
    }

    @Override
    public String getPermissionNode()
    {
        return TeleportModule.PERM_TPAHERE;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender par1ICommandSender, String[] args)
    {
        if (args.length == 1)
        {
            ArrayList<String> list = new ArrayList<String>();
            list.add("accept");
            list.add("decline");
            list.addAll(Arrays.asList(MinecraftServer.getServer().getAllUsernames()));
            return getListOfStringsMatchingLastWord(args, list);
        }
        else
        {
            return null;
        }
    }

    @Override
    public RegisteredPermValue getDefaultPermission()
    {
        return RegisteredPermValue.TRUE;
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {

        return "/tpahere [player] <player|<x> <y> <z>|accept|decline> Teleports you or a player to a player or x y z.";
    }
}
