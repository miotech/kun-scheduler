package com.miotech.kun.workflow.operator;

import com.google.common.base.Preconditions;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.operator.spark.clients.LivyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class LivyBaseSparkOperator extends KunOperator {
    public static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^\\}]+)}");
    private final static Logger logger = LoggerFactory.getLogger(LivyBaseSparkOperator.class);

    protected LivyClient livyClient;

    @Override
    public void init() {
        OperatorContext context = getContext();
        String livyHost = SparkConfiguration.getString(context, SparkConfiguration.CONF_LIVY_HOST);
        Preconditions.checkNotNull(livyHost, "Livy host should not be null");
        String queue = SparkConfiguration.getString(context, SparkConfiguration.CONF_LIVY_YARN_QUEUE);
        String proxyUser = SparkConfiguration.getString(context, SparkConfiguration.CONF_LIVY_PROXY_USER);
        logger.info("Livy client connect to \"{}\" to queue \"{}\" as user \"{}\"", livyHost, queue, proxyUser);
        livyClient = new LivyClient(livyHost, queue, proxyUser);
    }

    protected String replaceWithVariable(String rawText) {
        final Matcher matcher = VARIABLE_PATTERN.matcher(rawText);

        String result = rawText;
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String key = matcher.group(i);
                String value = (String) SparkConfiguration.getVariable(getContext(), key);
                if (value != null) {
                    result = result.replace(String.format("${%s}", key), value);
                } else {
                    throw new IllegalArgumentException("Cannot resolve variable key `" + key + "`");
                }
            }
        }
        return result;
    }

    protected void waitFoSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            logger.error("Failed in wait for : {}s", seconds, e);
            Thread.currentThread().interrupt();
        }
    }
}
