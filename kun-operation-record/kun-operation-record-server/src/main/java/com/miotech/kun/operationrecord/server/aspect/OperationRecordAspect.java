package com.miotech.kun.operationrecord.server.aspect;

import com.miotech.kun.commons.pubsub.publish.EventPublisher;
import com.miotech.kun.operationrecord.common.anno.OperationRecord;
import com.miotech.kun.operationrecord.common.event.BaseOperationEvent;
import com.miotech.kun.operationrecord.common.model.OperationStatus;
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

@Aspect
@Component
public class OperationRecordAspect {

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
        BaseOperationEvent baseOperationEvent = parseParam(pjp, operationRecord);
        baseOperationEvent.setStatus(OperationStatus.CREATED.name());
        eventPublisher.publish(baseOperationEvent);

        Object proceed = pjp.proceed();

        baseOperationEvent.setStatus(OperationStatus.SUCCESS.name());
        eventPublisher.publish(baseOperationEvent);

        return proceed;
    }

    @AfterThrowing(throwing = "t", pointcut = "@annotation(operationRecord)")
    public void recordOperationAfterException(JoinPoint jp, Throwable t, OperationRecord operationRecord) {
        BaseOperationEvent baseOperationEvent = parseParam(jp, operationRecord);
        baseOperationEvent.setStatus(OperationStatus.FAILED.name());
        eventPublisher.publish(baseOperationEvent);
    }

    private BaseOperationEvent parseParam(JoinPoint jp, OperationRecord operationRecord) {
        Object[] args = jp.getArgs();
        Method method = ((MethodSignature) jp.getSignature()).getMethod();
        String[] params = discoverer.getParameterNames(method);
        EvaluationContext context = new StandardEvaluationContext();
        for (int index = 0; index < params.length; index++) {
            context.setVariable(params[index], args[index]);
        }

        String key = operationRecord.event();
        Expression keyExpression = parser.parseExpression(key);
        return keyExpression.getValue(context, BaseOperationEvent.class);
    }

}
