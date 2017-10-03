package security;

import api.entity.UserSession;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

import java.util.Map;

public class AdminRole extends Security.Authenticator{
    @Override
    public String getUsername(Http.Context ctx) {
        Map<String, String[]> headers = ctx.request().headers();
        String[] tokens = headers.get("X-XSRF-TOKEN");
        ctx.response().setHeader("Access-Control-Allow-Origin", play.Play.application().configuration().getString("application.allow.origin"));
        ctx.response().setHeader("Access-Control-Allow-Methods", play.Play.application().configuration().getString("application.allow.methods"));
        ctx.response().setHeader("Access-Control-Allow-Headers", play.Play.application().configuration().getString("application.allow.headers"));

        if (tokens == null || tokens.length == 0) {
            return null;
        }
        return UserSession.validateAdmin(tokens[0], ctx.request().remoteAddress());
    }

    @Override
    public Result onUnauthorized(Http.Context ctx) {
            return unauthorized();
    }
}
