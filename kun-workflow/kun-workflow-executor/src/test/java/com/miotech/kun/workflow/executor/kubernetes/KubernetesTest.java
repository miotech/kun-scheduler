package com.miotech.kun.workflow.executor.kubernetes;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Inject;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.workflow.common.operator.dao.OperatorDao;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.common.taskrun.dao.TaskRunDao;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.model.operator.Operator;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.worker.WorkerInstance;
import com.miotech.kun.workflow.core.publish.EventPublisher;
import com.miotech.kun.workflow.executor.CommonTestBase;
import com.miotech.kun.workflow.executor.WorkerMonitor;
import com.miotech.kun.workflow.testing.factory.MockOperatorFactory;
import com.miotech.kun.workflow.testing.factory.MockTaskAttemptFactory;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class KubernetesTest extends CommonTestBase {

    @Rule
    public KubernetesServer server = new KubernetesServer(true, true);

    @Inject
    private PodLifeCycleManager podLifeCycleManager;

    @Inject
    private TaskRunDao taskRunDao;

    @Inject
    private TaskDao taskDao;

    @Inject
    private OperatorDao operatorDao;

    private KubernetesClient client;

    @Override
    protected void configuration() {
        Props props = new Props();
        props.put("executor.env", "KUBERNETES");
        props.put("executor.env.version", "1.15");
        props.put("executor.env.logPath", "/server/lib/logs");
        bind(Props.class, props);
        client = server.getClient();
        bind(KubernetesClient.class, client);
        bind(WorkerMonitor.class, PodEventMonitor.class);
        bind(EventPublisher.class, new NopEventPublisher());
        bind(EventBus.class, new EventBus());

        super.configuration();
    }

    @Test
    public void testCreatePod(){
        TaskAttempt taskAttempt = prepareAttempt();
        WorkerInstance instance = podLifeCycleManager.start(taskAttempt);
//        client.pods().inNamespace(taskAttempt.getQueueName()).withName(KUN_WORKFLOW + "_" + taskAttempt.getId())
//                .patch(new PodBuilder().withNewMetadataLike(MockPodFactory.create(taskAttempt.getId(),taskAttempt.getQueueName()).getMetadata()).endMetadata().build());;
//
//        podLifeCycleManager.stop(taskAttempt);
        int a = 1+1;
        Uninterruptibles.sleepUninterruptibly(60, TimeUnit.SECONDS);

        assertThat(instance.getTaskAttemptId(),is(taskAttempt.getId()));
    }

    private TaskAttempt prepareAttempt() {
        TaskAttempt attempt = MockTaskAttemptFactory.createTaskAttempt();
        long operatorId = attempt.getTaskRun().getTask().getOperatorId();
        Operator op = MockOperatorFactory.createOperator()
                .cloneBuilder()
                .withId(operatorId)
                .withName("Operator_" + operatorId)
                .build();
        operatorDao.createWithId(op, operatorId);
        taskDao.create(attempt.getTaskRun().getTask());
        taskRunDao.createTaskRun(attempt.getTaskRun());
        taskRunDao.createAttempt(attempt);
        return attempt;
    }

    private static class NopEventPublisher implements EventPublisher {
        @Override
        public void publish(Event event) {
            // nop
        }
    }
}
