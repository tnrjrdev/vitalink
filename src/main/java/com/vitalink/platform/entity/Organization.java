package com.vitalink.platform.entity;

import com.vitalink.platform.common.domain.Address;
import com.vitalink.platform.common.domain.BaseEntity;
import com.vitalink.platform.entity.enums.OrganizationType;
import com.vitalink.platform.entity.enums.RecordStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "organizations")
public class Organization extends BaseEntity {
    @Column(name = "legal_name", nullable = false, length = 180)
    private String legalName;

    @Column(name = "trade_name", length = 180)
    private String tradeName;

    @Column(nullable = false, unique = true, length = 14)
    private String cnpj;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrganizationType type;

    @Column(length = 180)
    private String email;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RecordStatus status = RecordStatus.ACTIVE;

    @Embedded
    private Address address;

    public boolean isInsurer() {
        return this.type == OrganizationType.INSURER;
    }
}
