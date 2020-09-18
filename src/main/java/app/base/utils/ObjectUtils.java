package app.base.utils;

import java.util.UUID;

public final class ObjectUtils {

    public static boolean isNull(Object object) {
        return object == null;
    }

    public static boolean isBlank(String string) {
        return isNull(string) || string.trim().isEmpty();
    }

    public static UUID parseUuidOr(String strUuid, UUID def) {
        if (!isBlank(strUuid)) {
            try {
                return UUID.fromString(strUuid);
            } catch (Exception ex) {
                return def;
            }
        }
        return def;
    }

    public static UUID parseUuidOrNull(String strUuid) {
        return parseUuidOr(strUuid, null);
    }

    public static Long parseLongOr(String strLong, Long def) {
        if (!isBlank(strLong)) {
            try {
                return Long.parseLong(strLong);
            } catch (Exception ex) {
                return def;
            }
        }
        return def;
    }

    public static Long parseLongOrNull(String strLong) {
        return parseLongOr(strLong, null);
    }


    public static Integer parseIntegerOr(String strLong, Integer def) {
        if (!isBlank(strLong)) {
            try {
                return Integer.parseInt(strLong);
            } catch (Exception ex) {
                return def;
            }
        }
        return def;
    }

    public static Integer parseIntegerOrNull(String strInt) {
        return parseIntegerOr(strInt, null);
    }


    public static Double parseDoubleOr(String strDouble, Double def) {
        if (!isBlank(strDouble)) {
            try {
                return Double.parseDouble(strDouble);
            } catch (Exception ex) {
                return def;
            }
        }
        return def;
    }

    public static Double parseDoubleOrNull(String strInt) {
        return parseDoubleOr(strInt, null);
    }


    public static Boolean parseBooleanOr(String strBoolean, Boolean defValue) {
        if (!isBlank(strBoolean)) {
            try {
                return Boolean.parseBoolean(strBoolean);
            } catch (Exception ex) {
                return defValue;
            }
        }
        return defValue;
    }

    public static Boolean parseBooleanOrNull(String strBoolean) {
        return parseBooleanOr(strBoolean, null);
    }

    public static String toStringOrNull(Object object) {
        return !isNull(object) ? object.toString() : null;
    }
}
