package com.coditas.tool.management.system.dto.premises;

import com.coditas.tool.management.system.entity.Workplace;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FacilityDTO {

    private Long id;

    @NotBlank(message = "Name Cannot Be Empty.")
    private String name;

    @NotBlank(message = "Address Cannot Be Empty.")
    private String address;

    private long facilityManagerId;

    private String facilityManagerEmail;

    private Long ownerId;

    private List<Workplace> workplaces;

}
