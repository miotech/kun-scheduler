package com.miotech.kun.commons.utils;

import com.google.common.base.Preconditions;

import java.util.Objects;

/**
 * Common utility class for argument checkers
 * @author Josh Ouyang
 */
public class ArgumentCheckUtils {
    private ArgumentCheckUtils() {}

    public static Boolean parseBooleanQueryParameter(String parameter) {
        if (Objects.isNull(parameter) || parameter.isEmpty()) {
            return false;
        }
        String normalizedParam = parameter.toLowerCase().trim();
        switch (normalizedParam) {
            case "1":
            case "true":
                return true;
            case "0":
            case "false":
                return false;
            default:
                throw new IllegalArgumentException(String.format("Unknown argument for a boolean-type parameter: %s", parameter));
        }
    }

    public static boolean isValidSortOrder(String sortOrder) {
        return Objects.isNull(sortOrder) ||
                Objects.equals(sortOrder.toUpperCase(), "ASC") ||
                Objects.equals(sortOrder.toUpperCase(), "DESC");
    }

    /**
     * Check if a sort order field is valid (should be one of "ASC", "DESC" or null).
     * @throws IllegalArgumentException if {@code sortOrder} is not {@code "ASC"}, {@code "DESC"} or {@code null}.
     * @param sortOrder - sortOrder field value
     */
    public static void checkSortOrder(String sortOrder) {
        Preconditions.checkArgument(isValidSortOrder(sortOrder), "Invalid sort order: \"{}\"", sortOrder);
    }
}
