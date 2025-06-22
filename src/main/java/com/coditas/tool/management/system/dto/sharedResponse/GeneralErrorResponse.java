package com.coditas.tool.management.system.dto.sharedResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GeneralErrorResponse {
    private String message;
    private LocalDateTime time;
    private int errorCode;
}
