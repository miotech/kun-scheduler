package com.miotech.kun.workflow.operator;

import com.miotech.kun.commons.testing.MockServerTestBase;
import com.miotech.kun.workflow.core.execution.KunOperator;
import com.miotech.kun.workflow.core.execution.TaskAttemptReport;
import com.miotech.kun.workflow.core.model.lineage.HiveTableStore;
import com.miotech.kun.workflow.testing.executor.OperatorRunner;
import org.junit.Before;
import org.junit.Test;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class SparkSQLOperatorTest extends MockServerTestBase {
    private static final String DATASTORE_URL = "/test";
    private OperatorRunner operatorRunner;

    @Before
    public void initSparkSqlOperator() {
        KunOperator operator = new SparkSQLOperator();
        operatorRunner = new OperatorRunner(operator);
        operatorRunner.setConfigKey(SparkConfiguration.CONF_LIVY_HOST, getAddress());
        operatorRunner.setConfigKey(SparkConfiguration.CONF_SPARK_DATASTORE_URL, DATASTORE_URL);
    }

    @Test
    public void run_sparksql_ok() {
        String sqlScript = "create table a as select * from b where bizdate = ${bizdate}";
        operatorRunner.setConfigKey(SparkConfiguration.CONF_SPARK_SQL, sqlScript);
        operatorRunner.setConfigKey(SparkConfiguration.CONF_VARIABLES,  "{\"bizdate\":\"'2020'\"}");

        // 1. create session
        mockPost("/sessions",  "{\"proxyUser\":\"hadoop\",\"queue\":\"default\"}","{\"id\":0,\"name\":null,\"appId\":null,\"owner\":null,\"proxyUser\":null,\"state\":\"starting\",\"kind\":\"shared\",\"appInfo\":{\"driverLogUrl\":null,\"sparkUiUrl\":null},\"log\":[\"stdout: \",\"\\nstderr: \",\"\\nYARN Diagnostics: \"]}");
        // 2. query session state
        mockGet("/sessions/0/state", "{\"state\":\"starting\"}");
        mockGet("/sessions/0/state", "{\"state\":\"idle\"}");

        // 3. create session statement
        String evaluatedSQL = "create table a as select * from b where bizdate = '2020'";
        String response = String.format(" {\"id\":0,\"code\":\"%s\",\"state\":\"available\",\"progress\":0.0}", evaluatedSQL);
        mockPost("/sessions/0/statements", String.format("{\"code\":\"%s\",\"kind\":\"sql\"}", evaluatedSQL), response);

        // 4. query statement state
        response = " {\"id\":0,\"code\":\"select 1\",\"state\":\"available\",\"output\":{\"status\":\"ok\",\"execution_count\":0,\"data\":{\"application/json\":{\"schema\":{\"type\":\"struct\",\"fields\":[{\"name\":\"1\",\"type\":\"integer\",\"nullable\":false,\"metadata\":{}}]},\"data\":[[1]]}}},\"progress\":1.0}";
        mockGet("/sessions/0/statements/0", response);

        // 5. delete session
        mockDelete("/sessions/0", "{\"msg\": \"deleted\"}");

        boolean isSuccess = operatorRunner.run();
        assertTrue(isSuccess);
        TaskAttemptReport report = operatorRunner.getReport().get();
        assertThat(report.getOutlets().get(0),
                sameBeanAs(new HiveTableStore(DATASTORE_URL + "/default/a", "default", "a")));
        assertThat(report.getInlets().get(0),
                sameBeanAs(new HiveTableStore(DATASTORE_URL + "/default/b","default", "b")));
    }

    @Test
    public void run_sparksql_failed() {
        String sqlScript = "select xxxx";
        operatorRunner.setConfigKey(SparkConfiguration.CONF_SPARK_SQL, sqlScript);

        // 1. create session
        mockPost("/sessions",  "{\"proxyUser\":\"hadoop\",\"queue\":\"default\"}","{\"id\":0,\"name\":null,\"appId\":null,\"owner\":null,\"proxyUser\":null,\"state\":\"starting\",\"kind\":\"shared\",\"appInfo\":{\"driverLogUrl\":null,\"sparkUiUrl\":null},\"log\":[\"stdout: \",\"\\nstderr: \",\"\\nYARN Diagnostics: \"]}");
        // 2. query session state
        mockGet("/sessions/0/state", "{\"state\":\"starting\"}");
        mockGet("/sessions/0/state", "{\"state\":\"idle\"}");

        // 3. create session statement
        String response = String.format(" {\"id\":0,\"code\":\"%s\",\"state\":\"available\",\"progress\":0.0}", sqlScript);
        String request = String.format( "{\"code\":\"%s\",\"kind\":\"sql\"}", sqlScript);
        mockPost("/sessions/0/statements", request, response);

        // 4. query statement state with error traceback
        response = String.format(" {\"id\":0,\"code\":\"%s\",\"state\":\"available\",\"output\":{\"status\":\"error\",\"execution_count\":0,\"ename\":\"Error\",\"evalue\":\"cannot resolve '`xxxx`' given input columns: []; line 1 pos 7;\\n'Project ['xxxx]\\n+- OneRowRelation\\n\",\"traceback\":[\"org.apache.spark.sql.catalyst.analysis.package$AnalysisErrorAt.failAnalysis(package.scala:42)\",\"org.apache.spark.sql.catalyst.analysis.CheckAnalysis$$anonfun$checkAnalysis$1$$anonfun$apply$3.applyOrElse(CheckAnalysis.scala:110)\",\"org.apache.spark.sql.catalyst.analysis.CheckAnalysis$$anonfun$checkAnalysis$1$$anonfun$apply$3.applyOrElse(CheckAnalysis.scala:107)\",\"org.apache.spark.sql.catalyst.trees.TreeNode$$anonfun$transformUp$1.apply(TreeNode.scala:278)\",\"org.apache.spark.sql.catalyst.trees.TreeNode$$anonfun$transformUp$1.apply(TreeNode.scala:278)\",\"org.apache.spark.sql.catalyst.trees.CurrentOrigin$.withOrigin(TreeNode.scala:70)\",\"org.apache.spark.sql.catalyst.trees.TreeNode.transformUp(TreeNode.scala:277)\",\"org.apache.spark.sql.catalyst.plans.QueryPlan$$anonfun$transformExpressionsUp$1.apply(QueryPlan.scala:93)\",\"org.apache.spark.sql.catalyst.plans.QueryPlan$$anonfun$transformExpressionsUp$1.apply(QueryPlan.scala:93)\",\"org.apache.spark.sql.catalyst.plans.QueryPlan$$anonfun$1.apply(QueryPlan.scala:105)\",\"org.apache.spark.sql.catalyst.plans.QueryPlan$$anonfun$1.apply(QueryPlan.scala:105)\",\"org.apache.spark.sql.catalyst.trees.CurrentOrigin$.withOrigin(TreeNode.scala:70)\",\"org.apache.spark.sql.catalyst.plans.QueryPlan.transformExpression$1(QueryPlan.scala:104)\",\"org.apache.spark.sql.catalyst.plans.QueryPlan.org$apache$spark$sql$catalyst$plans$QueryPlan$$recursiveTransform$1(QueryPlan.scala:116)\",\"org.apache.spark.sql.catalyst.plans.QueryPlan$$anonfun$org$apache$spark$sql$catalyst$plans$QueryPlan$$recursiveTransform$1$2.apply(QueryPlan.scala:121)\",\"scala.collection.TraversableLike$$anonfun$map$1.apply(TraversableLike.scala:234)\",\"scala.collection.TraversableLike$$anonfun$map$1.apply(TraversableLike.scala:234)\",\"scala.collection.immutable.List.foreach(List.scala:392)\",\"scala.collection.TraversableLike$class.map(TraversableLike.scala:234)\",\"scala.collection.immutable.List.map(List.scala:296)\",\"org.apache.spark.sql.catalyst.plans.QueryPlan.org$apache$spark$sql$catalyst$plans$QueryPlan$$recursiveTransform$1(QueryPlan.scala:121)\",\"org.apache.spark.sql.catalyst.plans.QueryPlan$$anonfun$2.apply(QueryPlan.scala:126)\",\"org.apache.spark.sql.catalyst.trees.TreeNode.mapProductIterator(TreeNode.scala:187)\",\"org.apache.spark.sql.catalyst.plans.QueryPlan.mapExpressions(QueryPlan.scala:126)\",\"org.apache.spark.sql.catalyst.plans.QueryPlan.transformExpressionsUp(QueryPlan.scala:93)\",\"org.apache.spark.sql.catalyst.analysis.CheckAnalysis$$anonfun$checkAnalysis$1.apply(CheckAnalysis.scala:107)\",\"org.apache.spark.sql.catalyst.analysis.CheckAnalysis$$anonfun$checkAnalysis$1.apply(CheckAnalysis.scala:85)\",\"org.apache.spark.sql.catalyst.trees.TreeNode.foreachUp(TreeNode.scala:127)\",\"org.apache.spark.sql.catalyst.analysis.CheckAnalysis$class.checkAnalysis(CheckAnalysis.scala:85)\",\"org.apache.spark.sql.catalyst.analysis.Analyzer.checkAnalysis(Analyzer.scala:95)\",\"org.apache.spark.sql.catalyst.analysis.Analyzer$$anonfun$executeAndCheck$1.apply(Analyzer.scala:108)\",\"org.apache.spark.sql.catalyst.analysis.Analyzer$$anonfun$executeAndCheck$1.apply(Analyzer.scala:105)\",\"org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper$.markInAnalyzer(AnalysisHelper.scala:201)\",\"org.apache.spark.sql.catalyst.analysis.Analyzer.executeAndCheck(Analyzer.scala:105)\",\"org.apache.spark.sql.execution.QueryExecution.analyzed$lzycompute(QueryExecution.scala:57)\",\"org.apache.spark.sql.execution.QueryExecution.analyzed(QueryExecution.scala:55)\",\"org.apache.spark.sql.execution.QueryExecution.assertAnalyzed(QueryExecution.scala:47)\",\"org.apache.spark.sql.Dataset$.ofRows(Dataset.scala:79)\",\"org.apache.spark.sql.SparkSession.sql(SparkSession.scala:643)\",\"org.apache.livy.repl.SQLInterpreter.execute(SQLInterpreter.scala:84)\",\"org.apache.livy.repl.Session$$anonfun$7.apply(Session.scala:274)\",\"org.apache.livy.repl.Session$$anonfun$7.apply(Session.scala:272)\",\"scala.Option.map(Option.scala:146)\",\"org.apache.livy.repl.Session.org$apache$livy$repl$Session$$executeCode(Session.scala:272)\",\"org.apache.livy.repl.Session$$anonfun$execute$1.apply$mcV$sp(Session.scala:168)\",\"org.apache.livy.repl.Session$$anonfun$execute$1.apply(Session.scala:163)\",\"org.apache.livy.repl.Session$$anonfun$execute$1.apply(Session.scala:163)\",\"scala.concurrent.impl.Future$PromiseCompletingRunnable.liftedTree1$1(Future.scala:24)\",\"scala.concurrent.impl.Future$PromiseCompletingRunnable.run(Future.scala:24)\",\"java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)\",\"java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)\",\"java.lang.Thread.run(Thread.java:748)\"]},\"progress\":1.0}", sqlScript);
        mockGet("/sessions/0/statements/0", response);

        // 5. delete session
        mockDelete("/sessions/0", "{\"msg\": \"deleted\"}");
        boolean isSuccess = operatorRunner.run();
        assertFalse(isSuccess);
    }

    @Test
    public void run_sparksession_failed() {
        String sqlScript = "select 1";

        operatorRunner.setConfigKey(SparkConfiguration.CONF_SPARK_SQL, sqlScript);

        // 1. create session
        mockPost("/sessions",  "{\"proxyUser\":\"hadoop\",\"queue\":\"default\"}","{\"id\":0,\"name\":null,\"appId\":null,\"owner\":null,\"proxyUser\":null,\"state\":\"starting\",\"kind\":\"shared\",\"appInfo\":{\"driverLogUrl\":null,\"sparkUiUrl\":null},\"log\":[\"stdout: \",\"\\nstderr: \",\"\\nYARN Diagnostics: \"]}");
        // 2. query session state, response error due to some cause
        mockGet("/sessions/0/state", "{\"state\":\"starting\"}");
        mockGet("/sessions/0/state", "{\"state\":\"error\"}");
        mockDelete("/sessions/0", "{\"msg\": \"deleted\"}");

        try {
            operatorRunner.run();
        } catch (Exception e) {
            assertThat(IllegalStateException.class, is(e.getClass()));
            assertThat("Session 0 is finished, current state: ERROR", is(e.getMessage()));
        }
    }

    @Test
    public void run_sparksession_abort() {
        String sqlScript = "select 1";

        operatorRunner.setConfigKey(SparkConfiguration.CONF_SPARK_SQL, sqlScript);

        // 1. create session
        mockPost("/sessions",  "{\"proxyUser\":\"hadoop\",\"queue\":\"default\"}","{\"id\":0,\"name\":null,\"appId\":null,\"owner\":null,\"proxyUser\":null,\"state\":\"starting\",\"kind\":\"shared\",\"appInfo\":{\"driverLogUrl\":null,\"sparkUiUrl\":null},\"log\":[\"stdout: \",\"\\nstderr: \",\"\\nYARN Diagnostics: \"]}");
        // 2. query session state, response error due to some cause
        mockGet("/sessions/0/state", "{\"state\":\"starting\"}");
        mockGet("/sessions/0/state", "{\"state\":\"starting\"}");
        mockGet("/sessions/0/state", "{\"state\":\"starting\"}");

        try {
            operatorRunner.abortAfter(2, (context) -> {
                mockDelete("/sessions/0", "{\"msg\": \"deleted\"}");
                mockGet("/sessions/0/state", "{\"msg\": \"Session '0' not found\"}");
            });
            operatorRunner.run();
        } catch (Exception e) {
            assertThat(IllegalStateException.class, is(e.getClass()));
            assertThat("Cannot find session: 0 . Maybe killed by user termination.", is(e.getMessage()));
        }
    }
}