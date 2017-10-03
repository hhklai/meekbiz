package view.controllers;

import api.entity.MizContent;
import api.entity.User;
import play.mvc.Result;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Security;
import security.UnsecureLoggedInCookieCheck;
import view.models.ProfileViewModel;

import java.util.List;

public class ProfileController extends Controller {

    @Security.Authenticated(UnsecureLoggedInCookieCheck.class)
    @Transactional(readOnly = true)
    public static Result privateProfile() {
        User user = User.getById(request().username());
        if (user == null) {
            return redirect(view.controllers.routes.IndexController.index());
        }
        List<MizContent> mizes = api.controllers.MizController.getPublicMizesOwnedByUser(user.getId());
        ProfileViewModel model = new ProfileViewModel(true, user, mizes);
        return ok(view.views.html.profile.profile.render(model));
    }

    @Transactional(readOnly = true)
    //TODO make this one cached
    public static Result profile(String name) {
        User user = User.getUserByName(name);
        if (user == null) {
            return redirect(view.controllers.routes.IndexController.index());
        }
        List<MizContent> mizes = api.controllers.MizController.getPublicMizesOwnedByUser(user.getId());
        ProfileViewModel model = new ProfileViewModel(false, user, mizes);
        return ok(view.views.html.profile.profile.render(model));
    }
}
