package com.coditas.tool.management.system.service;

import com.coditas.tool.management.system.dto.sharedResponse.SuccessResponse;
import com.coditas.tool.management.system.dto.tool.TopToolReportDTO;
import java.util.List;

public interface ReportService {
    SuccessResponse getTopDemandedTools();

    SuccessResponse getMostBrokenTools();

    SuccessResponse getTopPricedTools();
}
