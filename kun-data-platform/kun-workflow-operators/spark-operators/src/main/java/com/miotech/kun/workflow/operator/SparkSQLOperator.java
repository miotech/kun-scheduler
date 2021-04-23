package com.miotech.kun.workflow.operator;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.workflow.core.execution.*;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import com.miotech.kun.workflow.operator.resolver.SparkSqlResolver;
import com.miotech.kun.workflow.operator.spark.models.SparkApp;
import com.miotech.kun.workflow.operator.spark.models.SparkJob;
import com.miotech.kun.workflow.operator.spark.models.StateInfo;
import com.miotech.kun.workflow.operator.spark.models.Statement;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.miotech.kun.workflow.operator.SparkConfiguration.*;

public class SparkSQLOperator extends LivyBaseSparkOperator {

    private static final Logger logger = LoggerFactory.getLogger(SparkSQLOperator.class);

    private AtomicInteger currentActiveSessionId = new AtomicInteger(-1);
    private boolean isSharedSession;
    private String currentActiveStatementId;

    /**
     * init a livy rest client for later api calls
     */
    @Override
    public void init() {
        OperatorContext context = getContext();
        super.init();
        isSharedSession = SparkConfiguration.getBoolean(context, SparkConfiguration.CONF_LIVY_SHARED_SESSION);
        logger.info("Initialize livy rest client using shared mode: {}", isSharedSession);
    }

    /**
     * Execute user provided sql
     *
     * @return true if sql execution is success
     */
    @Override
    public boolean run() {
        try {
            return execute();
        } finally {
            this.cleanup();
        }
    }

    @Override
    public void abort() {
        this.cleanup();
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef()
                .define(CONF_LIVY_HOST, ConfigDef.Type.STRING, true, "Livy host to submit application, in the format `ip:port`", CONF_LIVY_HOST)
                .define(CONF_LIVY_YARN_QUEUE, ConfigDef.Type.STRING, CONF_LIVY_YARN_QUEUE_DEFAULT, true, "yarn queue name, default is `default`", CONF_LIVY_YARN_QUEUE)
                .define(CONF_LIVY_PROXY_USER, ConfigDef.Type.STRING, CONF_LIVY_PROXY_DEFAULT, true, "proxy use for livy", CONF_LIVY_PROXY_USER)
                .define(CONF_LIVY_BATCH_JARS, ConfigDef.Type.STRING, "", true, "Java application jar files", CONF_LIVY_BATCH_JARS)
                .define(CONF_LIVY_BATCH_FILES, ConfigDef.Type.STRING, "", true, "files to use, seperated with `,`, the first file would be used as main entry", CONF_LIVY_BATCH_FILES)
                .define(CONF_LIVY_SHARED_SESSION, ConfigDef.Type.BOOLEAN, false,true, "whether to use shared session in spark", CONF_LIVY_SHARED_SESSION)
                .define(CONF_LIVY_SHARED_SESSION_NAME, ConfigDef.Type.STRING, "",true, " shared session name if shared session enabled", CONF_LIVY_SHARED_SESSION_NAME)
                .define(CONF_SPARK_SQL, ConfigDef.Type.STRING, true, "SQL script", CONF_SPARK_SQL)
                .define(CONF_SPARK_DEFAULT_DB, ConfigDef.Type.STRING, CONF_SPARK_DEFAULT_DB_DEFAULT,true, "Default database name for a sql execution", CONF_SPARK_DEFAULT_DB)
                .define(CONF_LIVY_BATCH_CONF, ConfigDef.Type.STRING, "{}", true, "Extra spark configuration , in the format `{\"key\": \"value\"}`", CONF_LIVY_BATCH_CONF)
                .define(CONF_VARIABLES, ConfigDef.Type.STRING, "{}", true, "SQL variables, use like `select ${a}`, supply with {\"a\": \"b\"}", CONF_VARIABLES)
                ;
    }

    @Override
    public Resolver getResolver() {
        return new NopResolver();
    }

    public boolean execute() {

        SparkJob job = new SparkJob();
        OperatorContext context = getContext();
        String jars = SparkConfiguration.getString(context, CONF_LIVY_BATCH_JARS);
        String sparkConf = SparkConfiguration.getString(context, CONF_LIVY_BATCH_CONF);
        String sessionName = SparkConfiguration.getString(context, CONF_LIVY_SHARED_SESSION_NAME);
        Long taskRunId = context.getTaskRunId();

        if (Strings.isNullOrEmpty(sessionName)) {
            sessionName = "Spark Job: " + IdGenerator.getInstance().nextId();
        } else {
            sessionName = sessionName + " - " + IdGenerator.getInstance().nextId();
        }
        if (!Strings.isNullOrEmpty(sessionName)) {
            job.setName(sessionName);
        }
        if (!Strings.isNullOrEmpty(jars)) {
            job.setJars(Arrays.asList(jars.split(",")));
        }
        if (!Strings.isNullOrEmpty(sparkConf)) {
            job.setConf(JSONUtils.jsonStringToStringMap(replaceWithVariable(sparkConf)));
        }
        job.addConf("spark.hadoop.taskRunId", taskRunId.toString());
        if (!job.getConf().containsKey("spark.driver.memory")) {
            job.addConf("spark.driver.memory", "2g");
        }
        logger.info("Submit spark session: {}", JSONUtils.toJsonString(job));
        SparkApp app = livyClient.runSparkSession(job);
        Integer sessionId = app.getId();
        currentActiveSessionId.set(sessionId);
        logger.info("Running spark session in id: {}", currentActiveSessionId);

        // wait for session available
        StateInfo.State sessionState;
        do {
            sessionState = livyClient.getSparkSessionState(sessionId).getState();
            if (sessionState == null) {
                throw new IllegalStateException("Cannot find session: " + sessionId + " . Maybe killed by user termination.");
            }
            if (sessionState.isFinished()) {
                throw new IllegalStateException(String.format("Session %d is finished, current state: %s", sessionId, sessionState));
            }
            waitForSeconds(3);
        } while (!sessionState.isAvailable());

        // launch sql
        String sql = SparkConfiguration.getString(context, SparkConfiguration.CONF_SPARK_SQL);
        sql = replaceWithVariable(sql);
        logger.info("submit user provided sql: {}", sql);
        List<String> statements = Arrays.asList(sql.split(";"))
                .stream()
                .filter(StringUtils::isNoneBlank)
                .collect(Collectors.toList());
        for (String s: statements) {
            Statement stat = livyClient.runSparkSQL(sessionId, s);

            currentActiveStatementId = buildStatementId(sessionId, stat.getId());
            // wait for statement ended
            do {
                stat = livyClient.getStatement(sessionId, stat.getId());
                logger.debug("Statement: {}", JSONUtils.toJsonString(stat));
                if (stat == null) {
                    throw new IllegalStateException("Cannot find statement: " + currentActiveStatementId + " . Maybe killed by user termination.");
                }
                if (stat.getState().isFailed()) {
                    throw new IllegalStateException(String.format("statement %s is failed, current state: %s", currentActiveStatementId, sessionState));
                }
                waitForSeconds(3);
            } while (!stat.getState().isSuccess());
            Statement.StatementOutput output = stat.getOutput();
            logger.info("Output: \n {}", JSONUtils.toJsonString(output));
            boolean isSuccess = output.getStatus().equals("ok");
            if (!isSuccess) {
                return false;
            }
        }

        try {
            TaskAttemptReport taskAttemptReport = SparkQueryPlanLineageAnalyzer.lineageAnalysis(context.getConfig(), context.getTaskRunId());
            report(taskAttemptReport);
        } catch (Exception e) {
            logger.error("Failed to parse lineage: {}", e);
        }
        return true;
    }

    /**
     * When task is terminated,
     * should terminate the current sql statement in shared session mode
     * or close the current active session if not in shared session mode
     */
    public void cleanup() {
        if (isSharedSession && StringUtils.isNotBlank(currentActiveStatementId)) {
            String[] sessionAndStat = currentActiveStatementId.split("-");
            logger.info("Cancel current active statement {}", currentActiveStatementId);
            livyClient.cancelSessionStatement(
                    Integer.parseInt(sessionAndStat[0]),
                    Integer.parseInt(sessionAndStat[1]));
            currentActiveStatementId = null;
        } else {
            // current session may not be initialized
            int sessionId =  currentActiveSessionId.get();
            if (sessionId >= 0) {
                logger.info("Cancel current active session {}", sessionId);
                livyClient.deleteSession(sessionId);
                currentActiveSessionId.compareAndSet(sessionId, -1);
            }
        }
    }

    private String buildStatementId(int sessionId, int statementId) {
        return sessionId + "-" + statementId;
    }

}
