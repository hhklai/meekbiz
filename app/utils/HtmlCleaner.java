package utils;


import org.owasp.validator.html.*;
import play.Logger;


public class HtmlCleaner {
    public static String clean(String input) {

        Policy policy = null;
        try {
            policy = Policy.getInstance("conf/antisamy-tinymce-1.4.4.xml");
        } catch (PolicyException e) {
            Logger.warn("OWASP policy not loaded");
        }

        AntiSamy as = new AntiSamy();
        CleanResults cr = null;
        try {
            cr = as.scan(input, policy);
        } catch (ScanException e) {
            Logger.warn("OWASP scan problem", e);
        } catch (PolicyException e) {
            Logger.warn("OWASP policy problem", e);
        }

        return cr.getCleanHTML();
    }
}
