package com.hrbank.hrbank.changelog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "change_log_details")
// 변경 이력 상세
public class ChangeLogDiff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "change_log_id")
    private ChangeLog changeLog;

    @Column(nullable = false, length = 50)
    private String propertyName;        // 예: "직함, 부서, 이메일 등"

    @Column(length = 500)
    private String beforeValue;         // 추가 시 null

    @Column(length = 500)
    private String afterValue;          // 삭제 시 null

    public ChangeLogDiff(ChangeLog changeLog, String propertyName, String before, String after) {
        this.changeLog = changeLog;
        this.propertyName = propertyName;
        this.beforeValue = before;
        this.afterValue = after;
    }
}
