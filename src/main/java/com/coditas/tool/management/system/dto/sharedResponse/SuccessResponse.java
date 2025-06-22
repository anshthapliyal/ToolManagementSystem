package com.coditas.tool.management.system.dto.sharedResponse;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class SuccessResponse {
    private int status = HttpStatus.OK.value();
    private String message;
    private Object data;
    private LocalDateTime time = LocalDateTime.now();
}
