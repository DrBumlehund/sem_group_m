package m.group.sem.projectm;

import android.content.SharedPreferences;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Simon on 13-11-2017.
 */

public class Utilities {
    public static Object fromString(String s) throws IOException, ClassNotFoundException {
        byte[] data = Base64.decode(s, Base64.DEFAULT);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        Object o = ois.readObject();
        ois.close();
        return o;
    }

    public static String toString(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    public static SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    public static double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }

    /**
     * these characters should be removed from urls
     * space    %20     | # 	%23
     * $	    %24     | % 	%25
     * &	    %26     | @ 	%40
     * `	    %60     | / 	%2F
     * :	    %3A     | ; 	%3B
     * <	    %3C     | = 	%3D
     * >	    %3E     | ? 	%3F
     * [	    %5B     | \ 	%5C
     * ]	    %5D     | ^ 	%5E
     * {	    %7B     | | 	%7C
     * }	    %7D     | ~ 	%7E
     * “	    %22     | ‘ 	%27
     * +	    %2B     | , 	%2C
     */
    public static String sanitizeRestParameterValue(String url) {
        return url
                .replaceAll("%", "%25")
                .replaceAll(" ", "%20")
                .replaceAll("#", "%23")
                .replaceAll("&", "%26")
//                .replaceAll("$", "%24")
                .replaceAll("`", "%60")
                .replaceAll(":", "%3A")
                .replaceAll("<", "%3C")
                .replaceAll(">", "%3E")
//                .replaceAll("[", "%5B")
                .replaceAll("]", "%5D")
//                .replaceAll("{", "%7B")
                .replaceAll("}", "%7D")
                .replaceAll("\"", "%22")
//                .replaceAll("+", "%2B")
                .replaceAll("@", "%40")
                .replaceAll("/", "%2F")
                .replaceAll(";", "%3B")
                .replaceAll("=", "%3D")
//                .replaceAll("?", "%3F")
//                .replaceAll("\\\\", "%5C")
//                .replaceAll("^", "%5E")
//                .replaceAll("|", "%7C")
                .replaceAll("~", "%7E")
                .replaceAll("‘", "%27")
                .replaceAll(",", "%2c");
    }
}
