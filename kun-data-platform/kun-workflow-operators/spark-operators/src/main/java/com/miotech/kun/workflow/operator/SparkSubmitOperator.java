package com.miotech.kun.workflow.operator;

import com.miotech.kun.commons.utils.StringUtils;
import com.miotech.kun.workflow.core.execution.*;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class SparkSubmitOperator extends KunOperator {

    private static final Logger logger = LoggerFactory.getLogger(SparkSubmitOperator.class);
    private Process process;
    private String applicationId;

    private static final String COMMAND = "command";
    private static final String VARIABLES = "variables";
    private static final String DISPLAY_COMMAND = "bash command";
    private static final Long FORCES_WAIT_SECONDS_DEFAULT_VALUE = 10l;

    @Override
    public void init() {
        OperatorContext context = getContext();
        logger.info("Recieved task config: {}", JSONUtils.toJsonString(context.getConfig()));

        //TODO: su to proxyUser to execute spark-submit
        String queue = SparkConfiguration.getString(context, SparkConfiguration.CONF_LIVY_YARN_QUEUE);
        String proxyUser = SparkConfiguration.getString(context, SparkConfiguration.CONF_LIVY_PROXY_USER);
        logger.info("submit spark application to queue \"{}\" as user \"{}\"", queue, proxyUser);
    }


    @Override
    public boolean run() {
        Config config = getContext().getConfig();
        Map<String, String> variables = JSONUtils.jsonStringToStringMap(config.getString(VARIABLES));
        String command = config.getString(COMMAND);
        command = StringUtils.resolveWithVariable(command, variables);

        logger.debug("Execute command:\n\n {}", command);

        try {
            File commandFile = File.createTempFile("spark-submit-operator-", ".sh");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(commandFile))) {
                writer.write(command);
            }
            ProcessExecutor processExecutor = new ProcessExecutor();
            StartedProcess startedProcess = processExecutor
                    .command("sh", commandFile.getPath())
                    .redirectOutput(Slf4jStream.of(logger).asInfo())
                    .start();
            process = startedProcess.getProcess();

            // wait for termination
            int exitCode = startedProcess.getFuture()
                    .get().getExitValue();
            logger.info("Bash exit with code {}", exitCode);

            //TODO: if cluster mode, parse application Id from output, track yarn app status


            return exitCode == 0;
        } catch (IOException | ExecutionException e) {
            logger.error("{}", e);
            return false;
        } catch (InterruptedException e) {
            logger.error("{}", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public void abort() {
        //TODO: Yarn/K8/Mesos kill application
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef()
                .define(COMMAND, ConfigDef.Type.STRING, true, "bash command", DISPLAY_COMMAND)
                .define("forceWaitSeconds", ConfigDef.Type.LONG, FORCES_WAIT_SECONDS_DEFAULT_VALUE, true, "force terminate wait seconds", "forceWaitSeconds")
                .define(VARIABLES, ConfigDef.Type.STRING, "{}", true, "bash variables", "variables");
    }

    @Override
    public Resolver getResolver() {
        // TODO: implement this
        return new NopResolver();
    }

}
