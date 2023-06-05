package com.anubhavps.pdfsync.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputFormatter {

    public static boolean isValidMailId(String mailId) {
        final String PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return matcher(mailId.toLowerCase(Locale.ROOT), PATTERN);
    }

    public static boolean isValidUsername(String username) {
        final String PATTERN = "^(?=.{8,20}$)(?!.*[_]{2})[a-z0-9_]+";
        return matcher(username, PATTERN);
    }

    private static boolean matcher(String input, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(input.trim());
        return m.matches();
    }

}
