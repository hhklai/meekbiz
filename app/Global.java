import api.controllers.MizController;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.libs.F;
import play.libs.Json;
import play.libs.Time;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.SimpleResult;
import scala.concurrent.duration.Duration;
import utils.AkkaScheduleUtil;
import utils.PlayConfigUtil;
import utils.SearchUtil;
import utils.exceptions.ServerUnavailableException;

import java.text.ParseException;
import java.util.concurrent.TimeUnit;

import static com.groupon.uuid.UUID.useSequentialIds;
import static play.mvc.Results.internalServerError;
import static play.mvc.Results.notFound;

public class Global extends GlobalSettings {

    static {
        useSequentialIds();
    }

    private class ActionWrapper extends Action.Simple {
        public ActionWrapper(Action action) {
            this.delegate = action;
        }

        @Override
        public F.Promise<SimpleResult> call(Http.Context ctx) throws java.lang.Throwable {
            F.Promise<SimpleResult> result = this.delegate.call(ctx);
            Http.Response response = ctx.response();
            response.setHeader("Access-Control-Allow-Origin", play.Play.application().configuration().getString("application.allow.origin"));
            response.setHeader("Access-Control-Allow-Methods", play.Play.application().configuration().getString("application.allow.methods"));
            response.setHeader("Access-Control-Allow-Headers", play.Play.application().configuration().getString("application.allow.headers"));
            return result;
        }
    }

    @Override
    public Action onRequest(Http.Request request, java.lang.reflect.Method actionMethod) {
        return new ActionWrapper(super.onRequest(request, actionMethod));
    }

    @Override
    public F.Promise<SimpleResult> onError(Http.RequestHeader request, Throwable t) {
        if (t instanceof ServerUnavailableException) {
            //TODO send back 503 instead of 500
            return F.Promise.<SimpleResult>pure(internalServerError(
                    view.views.html.error.internalError.render()
            ));
        }
        return F.Promise.<SimpleResult>pure(internalServerError(
                view.views.html.error.internalError.render()
        ));
    }

    @Override
    public F.Promise<SimpleResult> onHandlerNotFound(Http.RequestHeader requestHeader) {
        return F.Promise.<SimpleResult>pure(notFound(
                view.views.html.error.pageNotFound.render()
        ));
    }

    @Override
    public void onStart(Application app) {
        //set json to accept objects with unknown properties
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Json.setObjectMapper(mapper);

        try {
            SearchUtil.init(PlayConfigUtil.getConfig("search.url")).get(5000);
        } catch (Throwable e) {
            Logger.error("ERROR!!!! Search server is not running.  Search functionality will be disabled");
        }

        MizController.syncMizes();
        scheduleOnceADayCheckOnSearchData();
    }

    @Override
    public void onStop(Application app) {
        AkkaScheduleUtil.stopAllSchedulers();
    }

    private void scheduleOnceADayCheckOnSearchData() {
        try {
            AkkaScheduleUtil.scheduleAt(new Time.CronExpression("0 0 0 * * ?"), Duration.create(1, TimeUnit.DAYS), new Runnable() {
                @Override
                public void run() {
                    MizController.syncMizes();
                }
            });
        } catch (ParseException e) {
            Logger.error("Wrong cron expression");
        }
    }

}