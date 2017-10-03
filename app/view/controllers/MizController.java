package view.controllers;

import api.entity.MizContent;
import play.db.jpa.Transactional;
import play.mvc.Result;
import play.mvc.Controller;
import view.models.MizViewModel;

public class MizController extends Controller {

    //TODO cache this endpoint
    @Transactional
    public static Result miz(String username, String mizTitle, boolean isOwner) {
        final MizViewModel model = new MizViewModel(isOwner, username, mizTitle);

        if (!isOwner) {
            MizContent content =  api.controllers.MizController.getPubicMiz(username, mizTitle);
            model.setMizContent(content);
            if (content == null) {
                return redirect(view.controllers.routes.IndexController.index());
            }
        }

        return ok(view.views.html.miz.render(model));
    }
}
