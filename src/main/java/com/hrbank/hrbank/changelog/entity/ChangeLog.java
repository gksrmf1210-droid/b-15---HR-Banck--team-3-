package com.hrbank.hrbank.changelog.entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
//import java.util.Map;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "change_logs")
public class ChangeLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ChangeType type;

    public enum ChangeType { CREATED, UPDATED, DELETED }

    private String employeeNumber;
    private String memo;              // 선택값 (null 가능)
    private String ipAddress;
    private Instant createdAt;

    @OneToMany(mappedBy = "changeLog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChangeLogDiff> details = new ArrayList<>();

    public ChangeLog(ChangeType type, String employeeNumber, String memo, String ipAddress) {
        this.type = type;
        this.employeeNumber = employeeNumber;
        this.memo = memo;
        this.ipAddress = ipAddress;
    }

    public void addDetail(String propertyName, String before, String after) {
        this.details.add(new ChangeLogDiff(this, propertyName, before, after));
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }
}