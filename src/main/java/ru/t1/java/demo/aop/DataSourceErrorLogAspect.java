package ru.t1.java.demo.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class DataSourceErrorLogAspect {
    @AfterThrowing(pointcut = "@annotation(dataSourceErrorLogged)", throwing = "ex")
    public void logDataSourceError(DataSourceErrorLogged dataSourceErrorLogged, Throwable ex) {
        log.error("Data source error occurred: {}", ex.getMessage(), ex);
    }
}
