package org.veupathdb.service.eda.us.service;

import jakarta.ws.rs.BadRequestException;
import org.veupathdb.service.eda.generated.model.UserAnalysisMetricsResponse;
import org.veupathdb.service.eda.generated.resources.MetricsUserProjectIdAnalyses;
import org.veupathdb.service.eda.us.model.UserDataFactory;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class MetricsService implements MetricsUserProjectIdAnalyses {

    @Override
    public GetMetricsUserAnalysesByProjectIdResponse getMetricsUserAnalysesByProjectId( String projectId, String startDate, String endDate) {
        try {
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.of(1990, 1, 1);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.of(2090, 1, 1);
            UserAnalysisMetricsResponse metricsResponse = new UserDataFactory(projectId).readAnalysisMetrics(start, end);
            return GetMetricsUserAnalysesByProjectIdResponse.respond200WithApplicationJson(metricsResponse);
        } catch(DateTimeParseException e) {
            throw new BadRequestException("Can't parse startDate '" + startDate + "' or endDate '" + endDate + "'", e);
        }
    }
}
