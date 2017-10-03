package api.controllers;

import api.entity.Miz;
import play.db.jpa.JPA;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import security.AdminRole;
import utils.PlayConfigUtil;
import utils.SearchUtil;

public class SearchController extends Controller {

    @Security.Authenticated(AdminRole.class)
    public static F.Promise<Result> reInitSearch() {
        return SearchUtil.init(PlayConfigUtil.getConfig("search.url")).map(new F.Function<Boolean, Result>() {
            @Override
            public Result apply(Boolean success) throws Throwable {
                return ok(success.toString());
            }
        });
    }

    @Security.Authenticated(AdminRole.class)
    public static F.Promise<Result> rebuildSearchIndex() {
        return SearchUtil.resetIndex().map(new F.Function<Boolean, Result>() {
            @Override
            public Result apply(Boolean success) throws Throwable {
                setAllSynchedSearchInDbToFalse();
                return ok(success.toString());
            }
        });
    }

    @Security.Authenticated(AdminRole.class)
    public static Result synchSearchIndexWithNewMizes() {
        MizController.syncMizes();
        return ok();
    }

    private static void setAllSynchedSearchInDbToFalse() {
        JPA.withTransaction(new F.Callback0() {
            @Override
            public void invoke() throws Throwable {
                Miz.clearAllSearchSync();
            }
        });
    }
}
