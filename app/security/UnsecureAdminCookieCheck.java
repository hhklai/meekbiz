package security;

import api.entity.UserSession;
import play.Logger;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

import java.util.Map;

public class UnsecureAdminCookieCheck extends Security.Authenticator{
    @Override
    public String getUsername(Http.Context ctx) {
        Http.Cookie token = ctx.request().cookie("XSRF-TOKEN");

        if (token == null) {
            return null;
        }
        return UserSession.validateAdmin(token.value(), ctx.request().remoteAddress());
    }

    @Override
    public Result onUnauthorized(Http.Context ctx) {
        return redirect(view.controllers.routes.IndexController.index());
    }
}
