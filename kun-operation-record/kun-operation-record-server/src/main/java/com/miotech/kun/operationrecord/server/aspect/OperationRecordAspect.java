package com.miotech.kun.operationrecord.server.aspect;

import com.google.common.collect.Maps;
import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.operationrecord.common.anno.OperationRecord;
import com.miotech.kun.operationrecord.common.event.OperationRecordEvent;
import com.miotech.kun.operationrecord.common.model.OperationRecordStatus;
import com.miotech.kun.operationrecord.common.model.OperationRecordType;
import com.miotech.kun.security.service.BaseSecurityService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

@Aspect
@Component
public class OperationRecordAspect extends BaseSecurityService {

    @Autowired
    @Qualifier("operationRecord-publisher")
    private EventPublisher eventPublisher;

    @Autowired
    private ExpressionParser parser;

    @Autowired
    @Qualifier("localVariableTableParameterNameDiscoverer")
    private ParameterNameDiscoverer discoverer;

    @Around("@annotation(operationRecord)")
    public Object recordOperation(ProceedingJoinPoint pjp, OperationRecord operationRecord) throws Throwable {
        OperationRecordEvent operationRecordEvent = parseParam(pjp, operationRecord);
        operationRecordEvent.setStatus(OperationRecordStatus.CREATED.name());
        eventPublisher.publish(operationRecordEvent);

        Object proceed = pjp.proceed();

        operationRecordEvent.setStatus(OperationRecordStatus.SUCCESS.name());
        eventPublisher.publish(operationRecordEvent);

        return proceed;
    }

    @AfterThrowing(throwing = "t", pointcut = "@annotation(operationRecord)")
    public void recordOperationAfterException(JoinPoint jp, Throwable t, OperationRecord operationRecord) {
        OperationRecordEvent operationRecordEvent = parseParam(jp, operationRecord);
        operationRecordEvent.setStatus(OperationRecordStatus.FAILED.name());
        eventPublisher.publish(operationRecordEvent);
    }

    private OperationRecordEvent parseParam(JoinPoint jp, OperationRecord operationRecord) {
        String operator = getCurrentUsername();
        OperationRecordType type = operationRecord.type();

        Object[] args = jp.getArgs();
        Method method = ((MethodSignature) jp.getSignature()).getMethod();
        String[] params = discoverer.getParameterNames(method);
        EvaluationContext context = new StandardEvaluationContext();
        for (int index = 0; index < params.length; index++) {
            context.setVariable(params[index], args[index]);
        }

        String[] annotationArgs = operationRecord.args();
        Map<String, Object> record = Maps.newHashMap();
        for (String annotationArg : annotationArgs) {
            Expression keyExpression = parser.parseExpression(annotationArg);
            Object annotationValue = keyExpression.getValue(context, Object.class);
            String recordKey = parseRecordKey(annotationArg);
            record.put(recordKey, annotationValue);
        }

        return new OperationRecordEvent(operator, type, record);
    }

    private String parseRecordKey(String annotationArg) {
        if (annotationArg.startsWith("#")) {
            return annotationArg.substring(1);
        }

        return annotationArg;
    }

}
