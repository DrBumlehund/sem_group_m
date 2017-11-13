package m.group.sem.projectm;

/**
 * Created by Simon on 13-11-2017.
 */

public class Constants {

    public static final String BASE_URL = "http://51.254.127.173:8080/api";
    public static final String BASE_TEST_URL = "http://51.254.127.173:8181/api";
    public static final String REPORTS_ONLY_COORDINATES = "reportsOnlyCoordinates";

    public static final String getBaseUrl () {
        if (BuildConfig.DEBUG) {
            return BASE_TEST_URL;
        } else {
            return BASE_URL;
        }
    }
}
