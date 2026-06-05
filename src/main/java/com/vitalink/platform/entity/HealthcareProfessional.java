package com.vitalink.platform.entity;

import com.vitalink.platform.common.domain.BaseEntity;
import com.vitalink.platform.entity.enums.CouncilType;
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
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "healthcare_professionals")
public class HealthcareProfessional extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Enumerated(EnumType.STRING)
    @Column(name = "council_type", nullable = false, length = 10)
    private CouncilType councilType;

    @Column(name = "council_number", nullable = false, length = 20)
    private String councilNumber;

    @Column(name = "council_state", nullable = false, length = 2)
    private String councilState;

    @Column(length = 120)
    private String specialty;

    @Column(length = 180)
    private String email;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RecordStatus status = RecordStatus.ACTIVE;
}
