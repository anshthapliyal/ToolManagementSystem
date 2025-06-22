package com.coditas.tool.management.system.dto.tool;

import com.coditas.tool.management.system.constant.ToolCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToolDTO {

    private long id;

    @NotBlank(message = "Please enter a name.")
    private String name;

    @PositiveOrZero(message = "Price should be greator than 0.")
    private Long price = 0L;

    @PositiveOrZero(message = "Price should be greator than 0.")
    private Long fineAmount = 0L;

    @Enumerated(value = EnumType.STRING)
    private ToolCategory category;

    private Boolean isPerishable;

    private Integer returnPeriod;

    private String toolImageUrl;

}