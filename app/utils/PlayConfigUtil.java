package utils;

import play.Play;

public class PlayConfigUtil {
    public static String getConfig(String configEntry) {
        return Play.application().configuration().getString(configEntry);
    }
    public static Integer getConfigI(String configEntry) {
        return Play.application().configuration().getInt(configEntry);
    }
}
