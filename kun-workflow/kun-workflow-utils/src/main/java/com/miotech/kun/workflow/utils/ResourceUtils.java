package com.miotech.kun.workflow.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Stream;

public class ResourceUtils {
    public static Stream<String> lines(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream)).lines();
    }
}
