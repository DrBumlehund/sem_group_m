package m.group.sem.projectm;

/**
 * Created by Simon on 13-11-2017.
 */

public class Constants {

    public static final String BASE_URL = "http://51.254.127.173:8080/api";
    public static final String BASE_TEST_URL = "http://51.254.127.173:8181/api";
    // https://developer.android.com/studio/run/emulator-networking.html
    public static final String BASE_LOCALHOST_URL = "http://10.0.2.2:8080/api";
    public static final String REPORTS_ONLY_COORDINATES = "reportsOnlyCoordinates";
    private static final boolean USE_LOCAL_HOST = true;

    public static final String getBaseUrl () {
        if (Constants.USE_LOCAL_HOST) {
            return BASE_LOCALHOST_URL;
        } else if (BuildConfig.DEBUG) {
            return BASE_TEST_URL;
        } else {
            return BASE_URL;
        }
    }
}
