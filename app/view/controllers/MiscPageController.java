package view.controllers;

import play.mvc.Result;
import play.mvc.Controller;
import play.mvc.Security;
import security.Authenticated;
import security.UnsecureAdminCookieCheck;

public class MiscPageController extends Controller {
    public static Result reportAbuse() {
        return ok(view.views.html.abuse.render());
    }

    @Security.Authenticated(UnsecureAdminCookieCheck.class)
    public static Result adminManagementPage() {
        return ok(view.views.html.admin.render());
    }
}
