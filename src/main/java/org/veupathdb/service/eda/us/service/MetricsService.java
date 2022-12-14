package org.veupathdb.service.eda.us.service;

import org.veupathdb.service.eda.generated.model.UserAnalysisMetricsResponse;
import org.veupathdb.service.eda.generated.resources.MetricsUserProjectIdAnalyses;
import org.veupathdb.service.eda.us.model.UserDataFactory;
import java.util.Date;

public class MetricsService implements MetricsUserProjectIdAnalyses {

    @Override
    public GetMetricsUserAnalysesByProjectIdResponse getMetricsUserAnalysesByProjectId( String projectId, Date startDate, Date endDate) {
        UserAnalysisMetricsResponse metricsResponse = new UserDataFactory(projectId).readAnalysisMetrics(startDate, endDate);
        return GetMetricsUserAnalysesByProjectIdResponse.respond200WithApplicationJson(metricsResponse);
    }
}
