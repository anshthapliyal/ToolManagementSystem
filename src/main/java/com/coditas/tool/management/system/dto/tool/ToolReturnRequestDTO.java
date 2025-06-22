package com.coditas.tool.management.system.dto.tool;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ToolReturnRequestDTO {
    private Long requestItemId;
    private Long returnQuantity;
    private LocalDateTime actualReturnDate;
}