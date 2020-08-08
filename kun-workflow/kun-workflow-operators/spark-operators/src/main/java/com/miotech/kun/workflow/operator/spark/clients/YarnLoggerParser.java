package com.miotech.kun.workflow.operator.spark.clients;


import com.miotech.kun.commons.utils.ExceptionUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YarnLoggerParser {

    private static final Logger logger = LoggerFactory.getLogger(YarnLoggerParser.class);
    private static final String STDERR_REGEX = "^(\\w+) : Total file length is (\\d+) bytes.";
    private static final Pattern STDERR_PATTERN = Pattern.compile(STDERR_REGEX, Pattern.MULTILINE);
    private static final String AMLOG_REGEX = "Log Type: ([\\w|\\.]+) \n"
            + " Log Upload Time: .* \n"
            + " Log Length: (\\d+)";
    private static final Pattern AMLOG_PATTERN = Pattern.compile(AMLOG_REGEX, Pattern.MULTILINE);

    private static final long MAX_FETCH_SIZE = 1000000;

    private final OkHttpClient restClient = new OkHttpClient();

    public String getYarnLogs(String amContainerLogUrl) {
        logger.debug("fetch log from {}", amContainerLogUrl);
        if (StringUtils.isNoneEmpty(amContainerLogUrl)) {
            Response responseEntity = getRequest(amContainerLogUrl);
            String finalUrl = responseEntity.request().url().toString();
            // if log aggregation ends, will redirect to am log
            if (!amContainerLogUrl.equals(finalUrl)) {
                logger.debug("Redirect to location: {}", finalUrl);
                responseEntity = getRequest(finalUrl);
                return getAmContainerLog(finalUrl, responseEntity.body());
            } else {
                return getAmContainerLog(amContainerLogUrl, responseEntity.body());
            }
        }
        return null;
    }

    /**
     * for test convenience, do not use Jsou.connect
     * @param url
     * @return
     */
    private Document connect(String url) {
        try {
            return Jsoup.parse(getRequest(url).body().string());
        } catch (IOException e) {
            logger.error("{}", e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    public String getAmContainerLog(String url, ResponseBody amContainerLogPage) {
        String logTypes = null;
        try {
            logTypes = Jsoup.parse(amContainerLogPage.string())
                    .body()
                    .selectFirst("table")
                    .selectFirst("tbody")
                    .getElementsByClass("content")
                    .get(0)
                    .select("p")
                    .append("\\n")
                    .text()
                    .replaceAll("\\\\n", "\n");
        } catch (IOException e) {
            logger.error("error to get response body {}", e);
        }

        StringBuilder stringBuilder = new StringBuilder();
        final Matcher matcher = AMLOG_PATTERN.matcher(logTypes);

        while (matcher.find()) {
            for (int i = 1; i < matcher.groupCount(); i = i + 2) {
                String logType = matcher.group(i);
                long logSiz = Long.parseLong( matcher.group(i+1));
                long startSize = logSiz < MAX_FETCH_SIZE ? 0: (-1 * MAX_FETCH_SIZE);
                stringBuilder.append("\n\n" + logType + ": \n\n");
                stringBuilder.append(getLogFile(url , logType.toLowerCase(), startSize));
                stringBuilder.append("\n\n");
            }
        }

        if (stringBuilder.toString().isEmpty()) {
            return getNodeContainerLog(url, logTypes);
        }
        return stringBuilder.toString();
    }

    private String getNodeContainerLog(String url, String content) {

        StringBuilder stringBuilder = new StringBuilder();
        final Matcher matcher = STDERR_PATTERN.matcher(content);

        while (matcher.find()) {
            for (int i = 1; i < matcher.groupCount(); i = i + 2) {
                String logType = matcher.group(i);
                long logSiz = Long.parseLong( matcher.group(i+1));
                long startSize = logSiz < MAX_FETCH_SIZE ? 0: (-1 * MAX_FETCH_SIZE);
                stringBuilder.append(logType + ": \n");
                stringBuilder.append(getLogFile(url , logType.toLowerCase(), startSize));
                stringBuilder.append("\n");
            }
        }

        return stringBuilder.toString();
    }

    private String getLogFile(String url, String type, long start) {
        String fetchLogUrl = String.format("%s/%s/?start=%s", url, type, start);
        try {
            Document page = connect(fetchLogUrl);
            return getLogContent(page);
        } catch (Exception e) {
            logger.error("Failed to fetch log from, {}", fetchLogUrl, e);
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    private String getLogContent(Document doc) {
        return doc.body()
                .selectFirst("table")
                .selectFirst("tbody")
                .getElementsByClass("content")
                .text();
    }

    private Response getRequest(String url) {
        Request request = new Request.Builder().url(url)
                .build();
        try {
            return restClient.newCall(request).execute();
        } catch (IOException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }
}
