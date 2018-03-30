package ua.rsa.gd;

import ua.rsa.gd.org.apache.commons.net.util.Base64;

/**
 * Created by Ромка on 19.03.2016.
 */
public class Utils {

    public static String decode(String code) {
        return new String(Base64.decodeBase64(code));
    }
}
