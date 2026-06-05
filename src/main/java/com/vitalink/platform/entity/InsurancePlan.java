package com.vitalink.platform.entity;

import com.vitalink.platform.common.domain.BaseEntity;
import com.vitalink.platform.entity.enums.CoverageType;
import com.vitalink.platform.entity.enums.RecordStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "insurance_plans")
public class InsurancePlan extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operator_id", nullable = false)
    private Organization operator;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "ans_code", nullable = false, unique = true, length = 20)
    private String ansCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "coverage_type", nullable = false, length = 30)
    private CoverageType coverageType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RecordStatus status = RecordStatus.ACTIVE;
}
