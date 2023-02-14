package org.veupathdb.service.eda.us.service;

import jakarta.ws.rs.BadRequestException;
import org.veupathdb.service.eda.generated.model.*;
import org.veupathdb.service.eda.generated.resources.MetricsUserProjectIdAnalyses;
import org.veupathdb.service.eda.us.model.UserDataFactory;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

public class MetricsService implements MetricsUserProjectIdAnalyses {

    @Override
    public GetMetricsUserAnalysesByProjectIdResponse getMetricsUserAnalysesByProjectId( String projectId, String startDate, String endDate,
                                                                                        MetricsUserProjectIdAnalysesGetStudyType studyType) {
        try {
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.of(1990, 1, 1);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.of(2090, 1, 1);
            UserDataFactory udf = new UserDataFactory(projectId);
            UserAnalysisMetricsResponse metricsResponse = readAnalysisMetrics(udf, studyType, start, end);
            return GetMetricsUserAnalysesByProjectIdResponse.respond200WithApplicationJson(metricsResponse);
        } catch(DateTimeParseException e) {
            throw new BadRequestException("Can't parse startDate '" + startDate + "' or endDate '" + endDate + "'. Correct format is: YYYY-MM-DD", e);
        }
    }
    public UserAnalysisMetricsResponse readAnalysisMetrics(UserDataFactory udf, MetricsUserProjectIdAnalysesGetStudyType studyType, LocalDate startDate, LocalDate endDate) {

        UserAnalysisMetricsResponseImpl response = new UserAnalysisMetricsResponseImpl();
        response.setStartDate(Date.from(startDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        response.setStartDate(Date.from(startDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        List<String> ignoreInMetricsUserIds = udf.getIgnoreInMetricsUserIds();
        String ignoreIdsString = String.join(", ", ignoreInMetricsUserIds);
        response.setCreatedOrModifiedCounts(getAnalysisCountsPerDateType(udf, studyType, startDate, endDate, UserDataFactory.DateColumn.CREATION_OR_MODIFICATION, ignoreIdsString));
        return response;
    }

    UserAnalysisCounts getAnalysisCountsPerDateType(UserDataFactory udf, MetricsUserProjectIdAnalysesGetStudyType studyType, LocalDate startDate, LocalDate endDate, UserDataFactory.DateColumn dateTypeColumn, String ignoreUserIds) {

        UserAnalysisCounts counts = new UserAnalysisCountsImpl();

        // All analyses in this time period
        List<StudyCount> allAnalyses =  udf.readAnalysisCountsByStudy(studyType, startDate, endDate, dateTypeColumn, ignoreUserIds, UserDataFactory.Imported.NO);
        counts.setAnalysesPerStudy(allAnalyses);
        int allCount = allAnalyses.stream().map(StudyCount::getCount).reduce(0, Integer::sum);
        counts.setAnalysesCount(allCount);

        // Imported analyses in this time period
        List<StudyCount> importedAnalyses =  udf.readAnalysisCountsByStudy(studyType, startDate, endDate, dateTypeColumn, ignoreUserIds, UserDataFactory.Imported.YES);
        counts.setImportedAnalysesPerStudy(importedAnalyses);
        int importedCount = importedAnalyses.stream().map(StudyCount::getCount).reduce(0, Integer::sum);
        counts.setImportedAnalysesCount(importedCount);

        // Registered user analyses in this time period
        List<UsersObjectsCount> registeredUsersAnalyses = udf.readObjectCountsByUserCounts(studyType, "count(analysis_id)", startDate, endDate, dateTypeColumn, ignoreUserIds, UserDataFactory.IsGuest.NO);
        counts.setRegisteredUsersAnalysesCounts(registeredUsersAnalyses);
        counts.setRegisteredUsersCount(registeredUsersAnalyses.stream().map(UsersObjectsCount::getUsersCount).reduce(0, Integer::sum));
        counts.setRegisteredAnalysesCount(registeredUsersAnalyses.stream().map(uoc->uoc.getObjectsCount() * uoc.getUsersCount()).reduce(0, Integer::sum));

        // Guest user analyses in this time period
        List<UsersObjectsCount> guestUsersAnalyses = udf.readObjectCountsByUserCounts(studyType, "count(analysis_id)", startDate, endDate, dateTypeColumn, ignoreUserIds, UserDataFactory.IsGuest.YES);
        counts.setGuestUsersAnalysesCounts(guestUsersAnalyses);
        counts.setGuestUsersCount(guestUsersAnalyses.stream().map(UsersObjectsCount::getUsersCount).reduce(0, Integer::sum));
        counts.setGuestAnalysesCount(guestUsersAnalyses.stream().map(uoc->uoc.getObjectsCount() * uoc.getUsersCount()).reduce(0, Integer::sum));

        // Registered user filters in this time period
        List<UsersObjectsCount> registeredUsersFilters = udf.readObjectCountsByUserCounts(studyType, "sum(num_filters)", startDate, endDate, dateTypeColumn, ignoreUserIds, UserDataFactory.IsGuest.NO);
        counts.setRegisteredUsersFiltersCounts(registeredUsersFilters);
        counts.setRegisteredFiltersCount(registeredUsersFilters.stream().map(uoc->uoc.getObjectsCount() * uoc.getUsersCount()).reduce(0, Integer::sum));

        // Guest user filters in this time period
        List<UsersObjectsCount> guestUsersFilters = udf.readObjectCountsByUserCounts(studyType, "sum(num_filters)", startDate, endDate, dateTypeColumn, ignoreUserIds, UserDataFactory.IsGuest.YES);
        counts.setGuestUsersFiltersCounts(guestUsersFilters);
        counts.setGuestFiltersCount(guestUsersFilters.stream().map(uoc->uoc.getObjectsCount() * uoc.getUsersCount()).reduce(0, Integer::sum));

        // Registered user visualizations in this time period
        List<UsersObjectsCount> registeredUsersVizs = udf.readObjectCountsByUserCounts(studyType, "sum(num_visualizations)", startDate, endDate, dateTypeColumn, ignoreUserIds, UserDataFactory.IsGuest.NO);
        counts.setRegisteredUsersVisualizationsCounts(registeredUsersVizs);
        counts.setRegisteredVisualizationsCount(registeredUsersVizs.stream().map(uoc->uoc.getObjectsCount() * uoc.getUsersCount()).reduce(0, Integer::sum));

        // Guest user filters in this time period
        List<UsersObjectsCount> guestUsersVisualizations = udf.readObjectCountsByUserCounts(studyType, "sum(num_visualizations)", startDate, endDate, dateTypeColumn, ignoreUserIds, UserDataFactory.IsGuest.YES);
        counts.setGuestUsersVisualizationsCounts(guestUsersVisualizations);
        counts.setGuestVisualizationsCount(guestUsersVisualizations.stream().map(uoc->uoc.getObjectsCount() * uoc.getUsersCount()).reduce(0, Integer::sum));

        return counts;
    }

}
