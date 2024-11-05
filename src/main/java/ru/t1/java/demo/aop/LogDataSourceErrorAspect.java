package ru.t1.java.demo.aop;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.DataSourceErrorLog;

@Aspect
@Component
public class LogDataSourceErrorAspect {
    @PersistenceContext
    private EntityManager entityManager;

    @AfterThrowing(pointcut = "execution(* com.example.bankapp..*(..))", throwing = "exception")
    public void logDataSourceError(JoinPoint joinPoint, Throwable exception) {
        DataSourceErrorLog errorLog = DataSourceErrorLog.builder()
                .stackTrace(getStackTraceAsString(exception))
                .message(exception.getMessage())
                .methodSignature(joinPoint.getSignature().toString())
                .build();

        entityManager.persist(errorLog);
    }

    private String getStackTraceAsString(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
