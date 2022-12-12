package org.veupathdb.service.eda.us.service;

import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import org.glassfish.jersey.server.ContainerRequest;
import org.veupathdb.service.eda.generated.model.UserAnalysisMetricsResponse;
import org.veupathdb.service.eda.generated.resources.MetricsUserAnalyses;

import java.util.Date;

public class MetricsService implements MetricsUserAnalyses {

    @Context
    private ContainerRequest _request;

    @Override
    public MetricsUserAnalyses.GetMetricsUserAnalysesResponse getMetricsUserAnalyses(@QueryParam("startDate") Date startDate,
                                                                              @QueryParam("endDate") Date endDate){
        UserAnalysisMetricsResponse metrics = getMetrics(startDate, endDate);
        return MetricsUserAnalyses.GetMetricsUserAnalysesResponse.respond200WithApplicationJson(
                metrics);
    }

    public static UserAnalysisMetricsResponse getMetrics( Date startDate, Date endDate) {
        return null;
    }

    static void sql() {
        // histogram of user count per number of analyses
        String s = """
select count(user_id) as user_cnt, analyses
from (
  select count(analysis_id) as analyses, user_id
  from EDAUSERCE.analysis
  where creation_time > '05-SEP-22' and creation_time <= '05-DEC-22'
  group by user_id
)
group by analyses
order by analyses desc
                """;

String s2 = """
select count(analysis_id) as cnt, study_id
from EDAUSERCE.analysis
  where creation_time > '05-SEP-22' and creation_time <= '05-DEC-22'
  group by study_id
  order by cnt desc        
        """;

String s3 = """
select count(analysis_id) as cnt, study_id
from EDAUSERCE.analysis
  where creation_time > '05-SEP-21' and creation_time <= '05-DEC-22'
  and provenance is not null
  group by study_id
  order by cnt desc        
        """;
    }
}
