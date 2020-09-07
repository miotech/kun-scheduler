package com.miotech.kun.metadata.core.utils;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

/**
 * Forked from: https://gist.github.com/skeller88/5eb73dc0090d4ff1249a
 *
 * Current standard is RFC 3986, published in 2005: http://tools.ietf.org/html/rfc3986#page-50. Apache Commons has a
 * url validator, but it doesn't accept certain urls, probably because it's implementing RFC2396 from 1998. Also, the
 * Fitbit API validator has custom needs such as allowing unicode characters.
 * <p/>
 * REGEX_COMPILED is used by UrlTypeConverter and MultiUrlsTypeConverter to validate third party app urls.
 * <p/>
 * All regex parts are borrowed from dperini's "https://gist.github.com/dperini/729294" unless otherwise noted.
 * The dperini regex satisfies most of the test cases here: https://mathiasbynens.be/demo/url-regex, and has undergone
 * significant edits that deal with most of the edge cases.
 * <p/>
 * ------<br>
 * Regular Expression for URL validation
 * <p/>
 * Author: Diego Perini
 * Updated: 2010/12/05
 * License: MIT
 * <p/>
 * Copyright (c) 2010-2013 Diego Perini (http://www.iport.it)
 * <p/>
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p/>
 * ------
 * <p/>
 * dperini's regex does ignore certain RFC spec edge cases for the sake of simplicity. Some examples:
 * - domain labels may only start and with alphanumeric characters, and end with alphanumeric characters or ".".<br>
 * - paths can begin with "//" when the authority component is not present.<br>
 * - the "unspecified address" should not be an allowed IP address.<br>
 * <p/>
 * Also in contrast to the spec, dperini's regex allows unicode characters. This leniency suits our purposes because
 * developers will likely submit URIs that they would paste into a browser address bar, and browsers now automatically
 * convert unicode characters into a valid format using the IDNA mechanism or percent encoding.
 * <p/>
 * We don't have to be as strict as the spec is with uri validation of callback urls that developers submit, because
 * ultimately, it's their responsibility to choose valid URIs. It's better to be overly lenient.
 */
public final class URIValidator {
    private URIValidator() {
    }

    /**
     * Example: "http". Also called 'protocol'.
     * Scheme component is optional, even though the RFC doesn't make it optional. Since this regex is validating a
     * submitted callback url, which determines where the browser will navigate to after a successful authentication,
     * the browser will use http or https for the scheme by default.
     * Not borrowed from dperini in order to allow any scheme type.
     */
    private static final String REGEX_SCHEME = "[A-Za-z][+-.\\w^_]*:";

    // Example: "//".
    private static final String REGEX_AUTHORATIVE_DECLARATION = "/{2}";

    // Optional component. Example: "suzie:abc123@". The use of the format "user:password" is deprecated.
    private static final String REGEX_USERINFO = "(?:\\S+(?::\\S*)?@)?";

    // Examples: "fitbit.com", "22.231.113.64".
    private static final String REGEX_HOST = "(?:" +
            // @Author = http://www.regular-expressions.info/examples.html
            // IP address
            "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)" +
            "|" +
            // host name
            "(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)" +
            // domain name
            "(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*" +
            // TLD identifier must have >= 2 characters
            "(?:\\.(?:[a-z\\u00a1-\\uffff]{2,})))";

    // Example: ":8042".
    private static final String REGEX_PORT = "(?::\\d{2,5})?";

    //Example: "/user/heartrate?foo=bar#element1".
    private static final String REGEX_RESOURCE_PATH = "(?:/\\S*)?";

    private static final String REGEX_URL = "^(?:(?:" + REGEX_SCHEME + REGEX_AUTHORATIVE_DECLARATION + ")?" +
            REGEX_USERINFO + REGEX_HOST + REGEX_PORT + REGEX_RESOURCE_PATH + ")$";

    private static final Pattern REGEX_COMPILED = Pattern.compile(REGEX_URL);

    private static final Pattern URN_PATTERN_REGEX_COMPILED = Pattern.compile("^urn(?::\\S*)*$");

    public static boolean isValid(@Nonnull final String url) {
        return REGEX_COMPILED.matcher(url).matches() || URN_PATTERN_REGEX_COMPILED.matcher(url).matches();
    }
}