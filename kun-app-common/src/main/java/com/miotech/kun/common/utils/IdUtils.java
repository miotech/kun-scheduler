package com.miotech.kun.common.utils;

/**
 * @author: Jie Chen
 * @created: 2020/7/24
 */

public class IdUtils {

    public static boolean equals(Long id1, Long id2) {
        return isNotEmpty(id1) && isNotEmpty(id2) && id1.equals(id2);
    }

    public static boolean isEmpty(Long id) {
        return id == null || id.equals(0L);
    }

    public static boolean isNotEmpty(Long id) {
        return !isEmpty(id);
    }

    public static <T extends Long> T firstNonEmpty(final T... values) {
        if (values != null) {
            for (final T val : values) {
                if (isNotEmpty(val)) {
                    return val;
                }
            }
        }
        return null;
    }
}
