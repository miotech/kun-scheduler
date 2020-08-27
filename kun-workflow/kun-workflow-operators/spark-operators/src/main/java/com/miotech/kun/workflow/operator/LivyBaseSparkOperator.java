package com.miotech.kun.workflow.operator;

import com.google.common.base.Preconditions;
import com.miotech.kun.commons.utils.StringUtils;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.OperatorContext;
import com.miotech.kun.workflow.operator.spark.clients.LivyClient;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class LivyBaseSparkOperator extends KunOperator {
    private static final Logger logger = LoggerFactory.getLogger(LivyBaseSparkOperator.class);

    protected LivyClient livyClient;

    @Override
    public void init() {
        OperatorContext context = getContext();
        logger.info("Recieved task config: {}", JSONUtils.toJsonString(context.getConfig()));

        String livyHost = SparkConfiguration.getString(context, SparkConfiguration.CONF_LIVY_HOST);
        Preconditions.checkNotNull(livyHost, "Livy host should not be null");
        String queue = SparkConfiguration.getString(context, SparkConfiguration.CONF_LIVY_YARN_QUEUE);
        String proxyUser = SparkConfiguration.getString(context, SparkConfiguration.CONF_LIVY_PROXY_USER);
        logger.info("Livy client connect to \"{}\" to queue \"{}\" as user \"{}\"", livyHost, queue, proxyUser);
        livyClient = new LivyClient(livyHost, queue, proxyUser);
    }

    protected String replaceWithVariable(String rawText) {
        return StringUtils.resolveWithVariable(rawText, SparkConfiguration.getVariables(getContext()));
    }

    protected void waitForSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            logger.error("Failed in wait for : {}s", seconds, e);
            Thread.currentThread().interrupt();
        }
    }
}
