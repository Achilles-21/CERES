package com.ceres.web.controller.system;

import com.ceres.common.annotation.Log;
import com.ceres.common.core.domain.AjaxResult;
import com.ceres.system.domain.vo.req.ExamRecordListReq;
import com.ceres.system.domain.vo.req.ExamReq;
import com.ceres.system.domain.vo.req.ExamCateReq;
import com.ceres.system.domain.vo.req.PageReq;
import com.ceres.system.domain.vo.resp.ExamResp;
import com.ceres.system.service.IExamService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("exam")
public class ExamController {

    @Resource
    private IExamService examService;

    @Log(title = "新增考试")
    @PostMapping("addExam")
    public AjaxResult addExam(@RequestBody ExamReq req) {
        examService.addExam(req);
        return AjaxResult.success();
    }

    @Log(title = "删除考试")
    @GetMapping("delete")
    public AjaxResult deleteExam(Long id) {
        examService.deleteExam(id);
        return AjaxResult.success();
    }

    @Log(title = "获取考试列表")
    @GetMapping("getExamList")
    public AjaxResult getExamList(Integer pageNum, Integer pageSize, String type, String content, String cateId) {
        return AjaxResult.success(examService.getExamList(pageNum, pageSize, type, content, cateId));
    }

    @Log(title = "修改考试")
    @PostMapping("update")
    public AjaxResult updateExam(@RequestBody ExamReq req) {
        examService.updateExam(req);
        return AjaxResult.success();
    }

    @Log(title = "根据id获取考试")
    @GetMapping("getExamById")
    public AjaxResult getExamById(@RequestParam("id") Long id) {
        ExamResp exam = examService.getExamById(id);
        return AjaxResult.success(exam);
    }

    @Log(title = "考试")
    @PostMapping("practice")
    public AjaxResult practice(@RequestParam("cateId") String cateId, @RequestBody PageReq req) {
        return AjaxResult.success(examService.getRecommendExamList(req, cateId));
    }

    @Log(title = "保存考试列表")
    @PostMapping("saveExamRecordList")
    public AjaxResult saveExamRecordList(@RequestBody ExamRecordListReq req) {
        examService.saveExamRecordList(req);
        return AjaxResult.success();
    }


    @Log(title = "查询所有题目")
    @GetMapping("/listAll")
    public AjaxResult listAll(String value){
        List<Map<String, String>> list = examService.listAll(value);
        return AjaxResult.success(list);
    }


    @Log(title = "新增考试类别")
    @PostMapping("addExamCate")
    public AjaxResult addExamCate(@RequestBody ExamCateReq examCateReq) {
        examService.addExamCate(examCateReq);
        return AjaxResult.success();
    }

    @Log(title = "删除考试类别")
    @GetMapping("deleteCate")
    public AjaxResult deleteExamCate(Long id) {
        examService.deleteExamCate(id);
        return AjaxResult.success();
    }

    @Log(title = "后台查询所有考试类别")
    @GetMapping("getExamCateList")
    public AjaxResult getExamCateList() {
        return AjaxResult.success(examService.getExamCateList());
    }

    @Log(title = "前台查询考试类别")
    @GetMapping("getFrontExamCateList")
    public AjaxResult getFrontExamCateList() {
        return AjaxResult.success(examService.getFrontExamCateList());
    }

    @Log(title = "修改开始类别")
    @PostMapping("updateCate")
    public AjaxResult updateExamCate(@RequestBody ExamCateReq examCateReq) {
        examService.updateExamCate(examCateReq);
        return AjaxResult.success();
    }

    @Log(title = "查询题目ID集")
    @GetMapping("getExamIdList")
    public AjaxResult getExamIdList(@RequestParam("cateId") Long cateId) {
        return AjaxResult.success(examService.examIdList(cateId));
    }
}
