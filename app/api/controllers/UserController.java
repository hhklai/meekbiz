package api.controllers;

import api.exceptions.ValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import api.entity.User;
import api.entity.UserSession;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import security.Authenticated;
import utils.MailUtil;

import java.util.Map;

import static play.data.Form.form;

public class UserController extends Controller {

    @Transactional
    public static Result registerUser() throws Throwable {
        Form<User> form = form(User.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest();
        }

        User user = form.get();
        if (user.getName() != null && User.getUserByName(user.getName()) != null) {
            return badRequest("User name already exists");
        }
        if (user.getEmail() == null) {
            return badRequest("Email is required");
        }
        if (User.getUserByEmail(user.getEmail()) != null) {
            return badRequest("Email already exists");
        }
        user.save();
        ObjectNode result = UserSession.createInst(user, request().remoteAddress());
        result.put("username", user.getName());
        MailUtil.sendWelcomeEmail(user); //send off async email
        return created(result);
    }

    @Transactional(readOnly = true)
    public static Result login() {
        JsonNode json = request().body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        }

        String emailOrName = json.findPath("emailplus").asText();
        String password = json.findPath("password").asText();
        boolean rememberMe = json.findPath("rememberMe").asBoolean();

        User userInDB = User.getUserByNameOrEmail(emailOrName);
        if (userInDB == null || !userInDB.checkPassword(password)) {
            return unauthorized();
        }
        ObjectNode result = UserSession.createInst(userInDB, request().remoteAddress(), rememberMe);
        result.put("username", userInDB.getName());
        return ok(result);
    }

    @Transactional(readOnly = true)
    public static Result checkIfUserExists(String email, String name) {
        boolean found = false;
        if (email != null) {
            User userInDB = User.getUserByEmail(email);
            found = (userInDB != null);
        }

        if (!found) {
            User userInDB = User.getUserByName(name);
            found = (userInDB != null);
        }
        ObjectNode result = Json.newObject()
                .put("found", found);
        return ok(result);
    }

    @Security.Authenticated(Authenticated.class)
    public static Result logout() {
        Map<String, String[]> headers = request().headers();
        String[] tokens = headers.get("X-XSRF-TOKEN");
        if (tokens != null && tokens.length != 0) {
            UserSession.deleteInstance(tokens[0]);
        }
        return ok();
    }

    @Security.Authenticated(Authenticated.class)
    @Transactional(readOnly = true)
    public static Result viewPrivateProfile(String fields) {
        ObjectNode result = Json.newObject();

        //optimized case for login check
        if (fields.equals("name")) {
            UserSession session = UserSession.retrieveFromRequestHeaders(request().headers());
            if (session != null) {
                ObjectNode user = Json.newObject();
                user.put("name", session.getUserName());
                result.put("user", user);
                return ok(result);
            }
        }

        //return all
        if (StringUtils.isBlank(fields) || fields.equals("all")) {
            User user = User.getById(request().username());
            result.put("user", user.asJSONObject());
            return ok(result);
        }

        //return filtered
        User user = User.getById(request().username());
        JsonNode userNode = user.asJSONObject();
        ObjectNode filteredNode = Json.newObject();
        String[] items = fields.split(",");
        for (String item : items) {
            JsonNode itemNode = userNode.get(item);
            if (itemNode != null) {
                filteredNode.put(item, itemNode);
            }
        }
        result.put("user", filteredNode);
        return ok(result);
    }

    @Security.Authenticated(Authenticated.class)
    @Transactional
    public static Result updatePrivateProfile() {
        Form<User> form = form(User.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest();
        }

        User requestUser = form.get();
        User user = User.getById(request().username());
        try {
            user.patch(requestUser);
        } catch (ValidationException e) {
            return badRequest(e.getMessage());
        }

        if (requestUser.getName() != null) {
            Map<String, String[]> headers = request().headers();
            String[] tokens = headers.get("X-XSRF-TOKEN");
            if (tokens != null && tokens.length != 0) {
                UserSession.updateUserName(tokens[0], requestUser.getName());
                //update search user names so that they are searchable
            }
        }
        return ok(user.asJSONObject());
    }
}
