package utils;

import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;
import api.entity.User;
import play.Play;
import play.libs.F;
import play.mvc.Call;

import static play.mvc.Controller.request;

public class MailUtil {
    public static String absoluteUrl(Call route) {
        String origin = Play.application().configuration().getString("application.allow.origin");
        if (origin == null || origin.equals("*")) {
            return route.absoluteURL(request());
        } else {
            return origin + route.url();
        }
    }
    public static MailerAPI getInstance() {
        return play.Play.application().plugin(MailerPlugin.class).email();
    }

    public static F.Promise<Boolean> sendWelcomeEmail(final User user) {
        return F.Promise.promise(
                new F.Function0<Boolean>() {
                    public Boolean apply() {
                        MailerAPI mail = MailUtil.getInstance();
                        mail.setSubject("Welcome to Meekbiz");
                        mail.setRecipient(user.getEmail());
                        mail.setFrom(Play.application().configuration().getString("email.notification.address"));
                        String html = view.views.html.emails.welcome.index.render(user).body();
                        String text = view.views.html.emails.welcome.plain.render(user).body();
                        mail.send(text, html);
                        return true;
                    }
                }
        );
    }
}

