package security;

import api.entity.UserSession;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

public class UnsecureLoggedInCookieCheck extends Security.Authenticator{
    @Override
    public String getUsername(Http.Context ctx) {
        Http.Cookie token = ctx.request().cookie("XSRF-TOKEN");

        if (token == null) {
            return null;
        }
        return UserSession.validateRequest(token.value(), ctx.request().remoteAddress());
    }

    @Override
    public Result onUnauthorized(Http.Context ctx) {
            return unauthorized();
    }
}
