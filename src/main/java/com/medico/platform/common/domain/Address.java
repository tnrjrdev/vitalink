package com.medico.platform.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * endereco reutilizavel, mapeado como tipo embutido (Value Object).
 * compartilhado por organizacoes e pacientes.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Address implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "address_street", length = 180)
    private String street;

    @Column(name = "address_number", length = 20)
    private String number;

    @Column(name = "address_complement", length = 120)
    private String complement;

    @Column(name = "address_district", length = 120)
    private String district;

    @Column(name = "address_city", length = 120)
    private String city;

    @Column(name = "address_state", length = 2)
    private String state;

    @Column(name = "address_zip_code", length = 9)
    private String zipCode;

    @Column(name = "address_country", length = 60)
    private String country;
}
