package com.forgeessentials.remote.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.permissions.PermissionsManager.RegisteredPermValue;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.api.UserIdent;
import com.forgeessentials.api.remote.FERemoteHandler;
import com.forgeessentials.api.remote.GenericRemoteHandler;
import com.forgeessentials.api.remote.RemoteRequest;
import com.forgeessentials.api.remote.RemoteResponse;
import com.forgeessentials.api.remote.RemoteSession;
import com.forgeessentials.api.remote.data.DataFloatLocation;

@FERemoteHandler(id = "query_player")
public class QueryPlayerHandler extends GenericRemoteHandler<QueryPlayerHandler.Request> {

    public static final String PERM = PERM_REMOTE + ".query.player";
    public static final String PERM_LOCATION = PERM + ".location";
    public static final String PERM_DETAIL = PERM + ".detail";

    public QueryPlayerHandler()
    {
        super(PERM, QueryPlayerHandler.Request.class);
        APIRegistry.perms.registerPermission(PERM, RegisteredPermValue.OP, "Allows querying player data");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected RemoteResponse<QueryPlayerHandler.Response> handleData(RemoteSession session, RemoteRequest<QueryPlayerHandler.Request> request)
    {
        for (String flag : request.data.flags)
        {
            switch (flag)
            {
            case "location":
                checkPermission(session, PERM_LOCATION);
                break;
            case "detail":
                checkPermission(session, PERM_DETAIL);
                break;
            }
        }

        Response response = new Response();
        if (request.data.name == null)
        {
            for (EntityPlayerMP player : (List<EntityPlayerMP>) MinecraftServer.getServer().getConfigurationManager().playerEntityList)
                response.players.add(getPlayerInfoResponse(session, UserIdent.get(player), request.data.flags));
        }
        else
        {
            UserIdent ident = UserIdent.get(request.data.name);
            if (!ident.hasPlayer())
                error("player not found");
            response.players.add(getPlayerInfoResponse(session, ident, request.data.flags));
        }
        return new RemoteResponse<QueryPlayerHandler.Response>(request, response);
    }

    public PlayerInfoResponse getPlayerInfoResponse(RemoteSession session, UserIdent ident, Set<String> flags)
    {
        PlayerInfoResponse pi = new PlayerInfoResponse(ident.getUuid().toString(), ident.getUsername());
        for (String flag : flags)
        {
            switch (flag)
            {
            case "location":
                pi.data.put(flag, new DataFloatLocation(ident.getPlayerMP()));
                break;
            case "detail":
                pi.data.put("health", ident.getPlayerMP().getHealth());
                pi.data.put("armor", ident.getPlayerMP().getTotalArmorValue());
                pi.data.put("hunger", ident.getPlayerMP().getFoodStats().getFoodLevel());
                pi.data.put("saturation", ident.getPlayerMP().getFoodStats().getSaturationLevel());
                break;
            }
        }
        return pi;
    }

    public static class Request {

        public String name;

        public Set<String> flags;

        public Request(String name, String... flags)
        {
            this.name = name;
            int i = flags.length;
            for (int j = 0; j < i; ++j)
            {
                this.flags.add(flags[i]);
            }
        }
    }

    public static class Response {

        public List<PlayerInfoResponse> players = new ArrayList<>();

    }

    public static class PlayerInfoResponse {

        public String uuid;

        public String name;

        public Map<String, Object> data = new HashMap<>();

        public PlayerInfoResponse(String uuid, String name)
        {
            this.uuid = uuid;
            this.name = name;
        }

    }

}
