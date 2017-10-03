package utils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Logger;
import play.libs.F;
import play.libs.Json;
import play.libs.WS;
import utils.exceptions.ServerUnavailableException;

public class SearchUtil {

    public static final String MIZ_OWNER_NAME = "ownerName";

    public static F.Promise<Boolean> init(String serverLoc) {
        serverRunning = false;
        serverAddress = serverLoc;

        String authUser0 = PlayConfigUtil.getConfig("search.auth.user");
        String authSecret0 = PlayConfigUtil.getConfig("search.auth.secret");
        if (authUser0 != null && authSecret0 != null) {
            authUser = authUser0;
            authSecret = authSecret0;
        }

        F.Promise<WS.Response> respPromise = url(serverAddress).head();
        return respPromise.flatMap(new F.Function<WS.Response, F.Promise<Boolean>>() {
            @Override
            public F.Promise<Boolean> apply(WS.Response response) throws Throwable {
                if (response.getStatus() == 200) { //index exists great!
                    serverRunning = true;
                    return F.Promise.pure(true);
                } else if (response.getStatus() == 404) {  //index not yet created
                    return createIndex();
                } else {
                    Logger.error("Search server: " + serverAddress + " does not return status code 200, check that it is running.");
                    return F.Promise.pure(false);
                }
            }
        });
    }

    private static F.Promise<Boolean> createIndex() {
        ObjectNode settings = Json.newObject();
        settings.put("number_of_shards", PlayConfigUtil.getConfigI("search.shards"));
        settings.put("number_of_replicas", PlayConfigUtil.getConfigI("search.replicas"));
        ObjectNode request = Json.newObject();
        request.put("settings", settings);
        return url(serverAddress).put(request).map(new F.Function<WS.Response, Boolean>() {
            @Override
            public Boolean apply(WS.Response response) throws Throwable {
                if (response.getStatus() == 200) {
                    serverRunning = true;
                    return true;
                }
                return false;
            }
        });
    }

    //this deletes the search index, do not use unless you're very sure
    public static F.Promise<Boolean> resetIndex() {
        serverRunning = false;
        return url(serverAddress).delete().flatMap(
                new F.Function<WS.Response, F.Promise<Boolean>>() {
                   @Override
                   public F.Promise<Boolean> apply(WS.Response response) throws Throwable {
                       return createIndex();
                   }
                }
            );
    }

    public static WS.WSRequestHolder http() {
        if (!isServerRunning()) {
            throw new ServerUnavailableException("Search server is not running");
        }

        return url(serverAddress);
    }

    public static WS.WSRequestHolder httpMiz(String id) {
        if (!isServerRunning()) {
            throw new ServerUnavailableException("Search server is not running");
        }

        return url(serverAddress + "/miz/" + id);
    }

    public static WS.WSRequestHolder httpSearchMiz(String query) {
        if (!isServerRunning()) {
            throw new ServerUnavailableException("Search server is not running");
        }

        return url(serverAddress + "/miz/_search").setQueryParameter("q", query);
    }

    public static boolean isServerRunning() {
        if (!serverRunning) {
            Logger.warn("Search server is not running, turn on elastic search then go to " + (view.controllers.routes.MiscPageController.adminManagementPage()) + " and click on reconnect to search.");
        }
        return serverRunning;
    }

    private static WS.WSRequestHolder url(String serverAddress) {
        if (authUser != null && authSecret != null) {
            return WS.url(serverAddress).setAuth(authUser, authSecret);
        } else {
            return WS.url(serverAddress);
        }
    }

    private static boolean serverRunning;
    private static String serverAddress;
    private static String authUser;
    private static String authSecret;
}
