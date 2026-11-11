package com.ceres.system.service;

import com.ceres.system.domain.vo.resp.openApi.*;

import java.util.List;

public interface IOpenApiService {

    List<ChatHistoryResp> getChatHistory(Long user_id);

    List<CaseReportResp> getExampleAnalysis(String concept);

    List<QueResponse> getExercise(Long user_id, Long course_id);

    List<NotesResponse> getNotes(Long user_id, Long course_id);
}
