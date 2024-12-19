package io.hhplus.tdd.util;

import java.util.Collection;
import java.util.Map;

public class CommUtil {

    public static <T> boolean isNullOrEmpty(T obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof String) {
            return ((String) obj).trim().isEmpty();
        }
        if (obj instanceof Collection) {
            return ((Collection<?>) obj).isEmpty();
        }
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).isEmpty();
        }
        return false;
    }
}
