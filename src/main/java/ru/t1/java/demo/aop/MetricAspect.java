package ru.t1.java.demo.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Async
@Slf4j
@Aspect
@Component
public class MetricAspect {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "t1_demo_metrics";
    private static final String ERROR_TYPE = "METRICS";

    @Around("@annotation(metric)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint, Metric metric) throws Throwable {
        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;

        if (executionTime > metric.value()) {
            String message = String.format("Error Type: %s, Method: %s, Execution Time: %d ms, Args: %s",
                    ERROR_TYPE, joinPoint.getSignature().toString(), executionTime, joinPoint.getArgs());
            kafkaTemplate.send(TOPIC, message);
        }

        return proceed;
    }
}
