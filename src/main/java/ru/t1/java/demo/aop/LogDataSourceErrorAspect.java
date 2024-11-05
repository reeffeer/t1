package ru.t1.java.demo.aop;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.DataSourceErrorLog;
import ru.t1.java.demo.repository.DataSourceErrorLogRepository;

@Aspect
@Component
public class LogDataSourceErrorAspect {
    private DataSourceErrorLogRepository errorLogRepository;

    @AfterThrowing(pointcut = "@annotation(LogDataSourceError)", throwing = "ex")
    public void logDataSourceError(Exception ex) {
        String stackTrace = getStackTraceAsString(ex);
        String message = ex.getMessage();
        String methodSignature = ex.getStackTrace()[0].toString();

        DataSourceErrorLog errorLog = DataSourceErrorLog.builder()
                .stackTrace(stackTrace)
                .message(message)
                .methodSignature(methodSignature)
                .build();
        errorLogRepository.save(errorLog);
    }

    private String getStackTraceAsString(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
