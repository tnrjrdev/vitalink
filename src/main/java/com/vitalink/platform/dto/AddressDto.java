package com.vitalink.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    @Size(max = 180)
    private String street;

    @Size(max = 20)
    private String number;

    @Size(max = 120)
    private String complement;

    @Size(max = 120)
    private String district;

    @Size(max = 120)
    private String city;

    @Size(min = 2, max = 2, message = "UF deve ter 2 caracteres")
    private String state;

    @Size(max = 9)
    private String zipCode;

    @Size(max = 60)
    private String country;
}
