package ru.t1.java.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "data_source_error_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataSourceErrorLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exception_stacktrace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(name = "error_message", nullable = false)
    private String message;

    @Column(name = "method_signature", nullable = false)
    private String methodSignature;
}