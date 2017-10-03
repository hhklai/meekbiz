package view.controllers;

import api.entity.MizContent;
import org.apache.commons.lang3.tuple.Pair;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import view.models.IndexViewModel;

import java.util.List;

public class IndexController extends Controller {

    //TODO cache this endpoint
    public static F.Promise<Result> index() {
        //TODO change query to get something more useful + paging will be needed
        return api.controllers.MizController.getMizByQuery("isPublic:true").map(
                new F.Function<List<Pair<String,MizContent>>, Result>() {
                    @Override
                    public Result apply(List<Pair<String,MizContent>> mizContents) throws Throwable {
                        IndexViewModel model = new IndexViewModel(mizContents);
                        return ok(view.views.html.index.index.render(model));
                    }
                }
        );
    }
}
