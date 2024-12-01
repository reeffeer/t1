package ru.t1.java.demo.aop;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.DataSourceErrorLog;
import ru.t1.java.demo.repository.DataSourceErrorLogRepository;

@Aspect
@Component
public class LogDataSourceErrorAspect {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "t1_demo_metrics";
    private static final String ERROR_TYPE = "DATA_SOURCE";

    @AfterThrowing(pointcut = "execution(* ru.t1.java.demo..*(..))", throwing = "exception")
    public void logDataSourceError(JoinPoint joinPoint, Throwable exception) {
        String message = String.format("Error Type: %s, Method: %s, Message: %s", ERROR_TYPE, joinPoint.getSignature().toString(), exception.getMessage());
        try {
            kafkaTemplate.send(TOPIC, message);
        } catch (Exception e) {
            DataSourceErrorLog errorLog = DataSourceErrorLog.builder()
                    .stackTrace(getStackTraceAsString(exception))
                    .message(exception.getMessage())
                    .methodSignature(joinPoint.getSignature().toString())
                    .build();

            entityManager.persist(errorLog);
        }
    }

    private String getStackTraceAsString(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
