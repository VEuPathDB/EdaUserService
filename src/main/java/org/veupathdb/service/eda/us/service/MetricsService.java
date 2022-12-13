package org.veupathdb.service.eda.us.service;

import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.veupathdb.service.eda.generated.model.UserAnalysisMetricsResponse;
import org.veupathdb.service.eda.generated.resources.MetricsUserProjectIdAnalyses;
import org.veupathdb.service.eda.us.model.UserDataFactory;
import java.util.Date;

public class MetricsService implements MetricsUserProjectIdAnalyses {

    @Override
    public MetricsUserProjectIdAnalyses.GetMetricsUserAnalysesByProjectIdResponse
    getMetricsUserAnalysesByProjectId(@PathParam("project-id") String projectId, @QueryParam("startDate") Date startDate,
                           @QueryParam("endDate") Date endDate){
        UserAnalysisMetricsResponse metricsResponse = new UserDataFactory(projectId).readAnalysisMetrics(startDate, endDate);
        return MetricsUserProjectIdAnalyses.GetMetricsUserAnalysesByProjectIdResponse.respond200WithApplicationJson(
                metricsResponse);
    }
}
