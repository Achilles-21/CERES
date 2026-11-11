package com.ceres.web.controller.openapi;

import com.ceres.system.domain.vo.resp.openApi.CaseReportResp;
import com.ceres.system.domain.vo.resp.openApi.ChatHistoryResp;
import com.ceres.system.domain.vo.resp.openApi.NotesResponse;
import com.ceres.system.domain.vo.resp.openApi.QueResponse;
import com.ceres.system.service.IOpenApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class OpenApiController {

    @Autowired
    private IOpenApiService openApiService;

    @GetMapping("/getChatHistory")
    public List<ChatHistoryResp> getChatHistory(@RequestParam("user_id") Long user_id){
        return openApiService.getChatHistory(user_id);
    }

    @GetMapping("/getExampleAnalysis")
    public List<CaseReportResp> getExampleAnalysis(@RequestParam("concept") String concept){
        return openApiService.getExampleAnalysis(concept);
    }

    @GetMapping("/getExercise")
    public List<QueResponse> getExercise(@RequestParam("user_id") Long user_id, @RequestParam(value = "course_id") Long course_id){
        return openApiService.getExercise(user_id, course_id);
    }

    @GetMapping("/getNotes")
    public List<NotesResponse> getNotes(@RequestParam("user_id") Long user_id, @RequestParam("course_id") Long course_id){
        return openApiService.getNotes(user_id, course_id);
    }


}
