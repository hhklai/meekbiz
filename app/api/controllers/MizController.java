package api.controllers;

import api.entity.*;
import api.exceptions.ValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import play.Logger;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.F;
import play.libs.Json;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import security.Authenticated;
import utils.SearchUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static play.data.Form.form;

public class MizController extends Controller {

    @Security.Authenticated(Authenticated.class)
    @Transactional
    public static Result createMiz() throws Throwable {

        JsonNode json = request().body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        }

        String title = json.findPath("title").asText();
        String region = json.findPath("locationRegion").asText();
        String summary = json.findPath("summary").asText();

        if (StringUtils.isEmpty(title)) {
            badRequest("title cannot be empty");
        }

        if (title.length() > Miz.MAX_TITLE_LENGTH) {
            return badRequest("title cannot be greater than " + Miz.MAX_TITLE_LENGTH + " characters");
        }

        if (summary != null && summary.length() > MizContent.MAX_SUMMARY_LENGTH) {
            return badRequest("summary cannot be greater than " + MizContent.MAX_SUMMARY_LENGTH + " characters");
        }

        if (Miz.getByUserIdAndTitle(request().username(), title) != null) {
            return badRequest("Another service with title: \"" + title + "\" is already used.");
        }

        Miz miz = Miz.create(title, region, request().username());
        MizContent mizContent = MizContent.create(title, summary, miz.id);

        User user = User.getById(miz.ownerId);

        JPA.em().flush(); //Make sure miz id is persisted so that save to search callback won't return null
        saveMizToSearch(user.getName(), miz, mizContent);

        return created(miz.urlTitle);
    }

    @Security.Authenticated(Authenticated.class)
    @Transactional(readOnly = true)
    public static Result getPrivateMiz(String username, String title) {
        UserSession userSession = UserSession.retrieveFromRequestHeaders(request().headers());
        if (userSession == null || !username.equals(userSession.getUserName())) {
            return forbidden();
        }
        Miz miz = Miz.getByUserNameAndTitle(username, title);
        if (miz == null) {
            return notFound();
        }
        MizContent mizContent = MizContent.getByMizId(miz.id);
        ObjectNode result = Json.newObject();
        result.putAll(miz.asJSONObject());
        result.putAll(mizContent.asJSONObject());
        result.put("descriptionUnusedS3Ids", Json.toJson(MizToS3File.getAllS3IdsByMizId(miz.id, MizToS3File.State.UNUSED)));
        return ok(result);
    }

    public static MizContent getPubicMiz(String username, String title) {
        Miz miz = Miz.getByUserNameAndTitle(username, title);
        if (miz != null && miz.isPublic) {
            MizContent mizContent = MizContent.getByMizId(miz.id);
            return mizContent;
        } else {
            return null;
        }
    }

    @Security.Authenticated(Authenticated.class)
    @Transactional(readOnly = true)
    public static Result getPendingMizes() {
        List<Miz> mizes = Miz.getPendingMizesByOwnerId(request().username());
        ArrayNode nodes = Json.newObject().arrayNode();

        for (Miz miz : mizes) {
            MizContent mizContent = MizContent.getByMizId(miz.id);
            ObjectNode node = Json.newObject();
            node.putAll(miz.asJSONObject());
            node.putAll(mizContent.asJSONObject());
            nodes.add(node);
        }

        return ok(nodes);
    }

    @Security.Authenticated(Authenticated.class)
    @Transactional(readOnly = true)
    public static Result checkIfMizExists(String title) {
        Miz mizInDb = Miz.getByUserIdAndTitle(request().username(), title);
        boolean found = (mizInDb != null);

        ObjectNode result = Json.newObject()
                .put("found", found);
        return ok(result);
    }

    public static List<MizContent> getPublicMizesOwnedByUser(String userId) {
        List<MizContent> mizes = new ArrayList<>();
        for (Miz miz : Miz.getPublicMizesByOwnerId(userId)) {
            mizes.add(MizContent.getByMizId(miz.id));
        }
        return mizes;
    }

    @Security.Authenticated(Authenticated.class)
    @Transactional
    public static Result updateMiz(String title) {
        UserSession userSession = UserSession.retrieveFromRequestHeaders(request().headers());
        Miz mizInDb = Miz.getByUserIdAndTitle(request().username(), title);
        if (userSession == null || mizInDb == null) {
            return unauthorized();
        }

        Form<MizContent> form = form(MizContent.class).bindFromRequest();
        Form<Miz> form1 = form(Miz.class).bindFromRequest();
        if (form.hasErrors() || form1.hasErrors()) {
            return badRequest();
        }

        Miz mizPatch = form1.get();
        MizContent mizContentPatch = form.get();

        MizContent mizContentInDb = MizContent.getByMizId(mizInDb.id);

        boolean mizContentChanged;
        try {
            mizContentChanged = mizContentInDb.patch(mizContentPatch, request().username());
        } catch (ValidationException e) {
            return badRequest(e.getMessage());
        }

        if (mizContentChanged) {
            mizPatch.searchSynched = false;
        }
        mizPatch.urlTitle = mizContentPatch.title;
        try {
            mizInDb.patch(mizPatch);
        } catch (ValidationException e) {
            return badRequest(e.getMessage());
        }

        if (!mizInDb.searchSynched) {
            JPA.em().flush(); //Make sure miz is persisted so that save to search callback will not result in a transaction lock conflict
            saveMizToSearch(userSession.getUserName(), mizInDb, mizContentInDb);
        }

        ObjectNode result = Json.newObject();
        result.putAll(mizInDb.asJSONObject());
        result.putAll(mizContentInDb.asJSONObject());
        return ok(result);
    }

    @Security.Authenticated(Authenticated.class)
    @Transactional
    public static Result requestS3Ids(String title) {
        JsonNode json = request().body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        }

        Integer numIds = json.findPath("numIds").asInt();
        if (numIds == null) {
            return badRequest("Expecting numIds in body");
        }

        Miz mizInDb = Miz.getByUserIdAndTitle(request().username(), title);
        ArrayNode result = Json.newObject().arrayNode();

        //use unused s3ids
        List<String> unused = MizToS3File.getAllS3IdsByMizId(mizInDb.id, MizToS3File.State.UNUSED);
        for (int i=0; i < unused.size() && numIds > 0; numIds--, i++ ) {
            S3File s3File = S3File.getByPublicId(unused.get(i));
            MizToS3File.updateS3File(mizInDb.id, s3File.getPublicId(), MizToS3File.State.USED);
            result.add(s3File.createPolicyResponseNode());
        }

        //create new s3ids
        for (int i=0; i < numIds; i++) {
            S3File s3File = new S3File();
            s3File.save(request().username());
            MizToS3File.linkS3File(mizInDb.id, s3File.getPublicId());
            result.add(s3File.createPolicyResponseNode());
        }
        return ok(result);
    }

    public static void saveMizToSearch(String username, Miz miz, MizContent mizContent) {
        final String theId = miz.id;

        ObjectNode node = Json.newObject();
        node.putAll(miz.asJSONObject());
        node.putAll(mizContent.asJSONObject());
        node.put(SearchUtil.MIZ_OWNER_NAME, username);
        node.remove("contentBody");  //don't want the content details in search

        SearchUtil.httpMiz(miz.id).put(node).onRedeem(new F.Callback<WS.Response>() {
            @Override
            public void invoke(WS.Response response) throws Throwable {
                if (response.getStatus() == 201 || response.getStatus() == 200) {
                    JPA.withTransaction(new F.Callback0() {
                        @Override
                        public void invoke() throws Throwable {
                            Miz miz = Miz.get(theId);
                            if (miz != null) {
                                miz.searchSynched = true;
                            } else {
                                Logger.error("Miz not found with Id " + theId + ".  Unable to update searchSynched.");
                            }

                        }
                    });
                }
            }
        });
    }

    public static void syncMizes() {
        if (SearchUtil.isServerRunning()) {
            JPA.withTransaction(
                    new F.Callback0() {
                        @Override
                        public void invoke() throws Throwable {
                            List<Miz> nonsynchedMizes;
                            int offset = 0;
                            do {
                                //TODO LIMIT getNonSynchedMizes to a batch size, we might run into memory problems when there's lots of mizes
                                nonsynchedMizes = Miz.getNonSynchedMizes(offset);
                                offset += nonsynchedMizes.size();
                                for (Miz miz : nonsynchedMizes) {
                                    MizContent mizContent = MizContent.getByMizId(miz.id);
                                    User user = User.getById(miz.ownerId);
                                    saveMizToSearch(user.getName(), miz, mizContent);
                                }
                            } while (!nonsynchedMizes.isEmpty());
                        }
                    });

        }
    }

    public static F.Promise<List<Pair<String,MizContent>>> getMizByQuery(String query) {
        if (!SearchUtil.isServerRunning()) {
            return F.Promise.pure(Collections.<Pair<String,MizContent>>emptyList());
        }

        return SearchUtil.httpSearchMiz(query).get().map(
                new F.Function<WS.Response, List<Pair<String,MizContent>>>() {
                    @Override
                    public List<Pair<String,MizContent>> apply(WS.Response response) throws Throwable {
                        List<Pair<String,MizContent>> content = new ArrayList<>();
                        try {
                            JsonNode hits = response.asJson().get("hits").get("hits");
                            if (hits.isArray()) {
                                for (JsonNode element : hits) {
                                    JsonNode node = element.get("_source");
                                    Pair<String, MizContent> pair = Pair.of(node.get(SearchUtil.MIZ_OWNER_NAME).asText(),
                                            Json.fromJson(node, MizContent.class));
                                    content.add(pair);
                                }
                            }
                        } catch (NullPointerException e) {
                            //don't do anything and return empty content
                        }

                        return content;
                    }
                }
        );
    }

    @Security.Authenticated(Authenticated.class)
    @Transactional
    public static Result deleteMizThatHasNeverBeenActivated(String username, String title) throws Throwable {
        UserSession userSession = UserSession.retrieveFromRequestHeaders(request().headers());
        if (userSession == null || !username.equals(userSession.getUserName())) {
            return forbidden();
        }
        Miz miz = Miz.getByUserNameAndTitle(username, title);
        List<MizContent> mizContent = MizContent.getMizHistory(miz.id);

        if (mizContent.size() > 1 ||  mizContent.get(0).moderatedBy != null) {  //has been activated in the pass
            return forbidden();
        }

        for (MizContent item : mizContent) {
            MizContent.delete(item);
        }

        MizToS3File.deleteS3FileLinkForMiz(miz.id);
        SearchUtil.httpMiz(miz.id).delete(); //delete from search
        Miz.delete(miz);

        return noContent();
    }

}
