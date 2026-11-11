package com.ceres.web.controller.system;

import com.ceres.common.annotation.Log;
import com.ceres.common.core.controller.BaseController;
import com.ceres.common.core.domain.AjaxResult;
import com.ceres.common.core.page.PageResult;
import com.ceres.system.domain.vo.req.CaseReq;
import com.ceres.system.domain.vo.req.EvaluateReq;
import com.ceres.system.domain.vo.resp.CaseAnalysisResp;
import com.ceres.system.domain.CaseQueBack;
import com.ceres.system.domain.vo.resp.CaseResp;
import com.ceres.system.service.ICaseService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/case")
public class CaseController extends BaseController {

    @Resource
    private ICaseService caseService;

    @Log(title = "案例分析")
    @PostMapping("/analysis")
    public AjaxResult analysis(@RequestBody Map<String, String> map){
        List<CaseAnalysisResp> chatRespList = caseService.analysis(map);
        return success(chatRespList);
    }

    @Log(title = "新增案例")
    @PostMapping("/add")
    public AjaxResult addCase(@RequestBody CaseReq caseReq){
        caseService.addCase(caseReq);
        return success();
    }

    @Log(title = "修改案例")
    @PutMapping("/update")
    public AjaxResult updateCase(@RequestBody CaseReq caseReq){
        caseService.updateCase(caseReq);
        return success();
    }

    @Log(title = "删除案例")
    @DeleteMapping("/del/{id}")
    public AjaxResult deleteCase(@PathVariable Long id){
        caseService.deleteCase(id);
        return success();
    }

    @Log(title = "根据id查询案例")
    @GetMapping("/{id}")
    public AjaxResult getById(@PathVariable Long id){
        CaseResp caseResp = caseService.getById(id);
        return success(caseResp);
    }

    @Log(title = "分页查询案例")
    @GetMapping("/page")
    public AjaxResult pageQuery(@RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize,
                                @RequestParam(name = "patient", required = false) String patient){
        PageResult pageResult = caseService.pageQuery(pageNum, pageSize, patient);
        return success(pageResult);
    }

    @Log(title = "获取模型生成的问题")
    @PostMapping("/getQue")
    public AjaxResult getQue(@RequestBody CaseQueBack caseQueBack){
        CaseAnalysisResp caseAnalysisRespList = caseService.getQue(caseQueBack);
        return success(caseAnalysisRespList);
    }

    @Log(title = "医生评判学生回答")
    @PostMapping("/evaluate")
    public AjaxResult evaluate(@RequestBody EvaluateReq evaluateReq){
        List<CaseAnalysisResp> caseAnalysisRespList = caseService.evaluate(evaluateReq);
        return success(caseAnalysisRespList);
    }

    @Log(title = "学生回答后显示答案")
    @GetMapping("/showAnswers")
    public AjaxResult showAnswers(@RequestParam("id") Long id, @RequestParam String question){
        CaseAnalysisResp caseAnalysisResp = caseService.showAnswers(id, question);
        return success(caseAnalysisResp);
    }
}
