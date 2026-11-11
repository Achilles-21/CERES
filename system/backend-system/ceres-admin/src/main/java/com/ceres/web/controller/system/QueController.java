package com.ceres.web.controller.system;

import com.ceres.common.annotation.Log;
import com.ceres.common.core.domain.AjaxResult;
import com.ceres.common.core.page.PageResult;
import com.ceres.system.domain.vo.req.QueReq;
import com.ceres.system.domain.vo.resp.QueResp;
import com.ceres.system.domain.vo.resp.RecommendQueResp;
import com.ceres.system.service.IQueService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("que")
public class QueController{

    @Resource
    private IQueService queService;

    @Log(title = "新增题目")
    @PostMapping("addQue")
    public AjaxResult addQue(@RequestBody QueReq req) {
        queService.addQue(req);
        return AjaxResult.success();
    }

    @Log(title = "删除题目")
    @GetMapping("delete")
    public AjaxResult deleteQue(Long id) {
        queService.deleteQue(id);
        return AjaxResult.success();
    }

    @Log(title = "获取题目列表")
    @GetMapping("getQueList")
    public AjaxResult getQueList(Integer pageNum, Integer pageSize, String type, String content) {
        return AjaxResult.success(queService.getQueList(pageNum, pageSize, type, content));
    }

    @Log(title = "修改题目")
    @PostMapping("update")
    public AjaxResult updateQue(@RequestBody QueReq req) {
        queService.updateQue(req);
        return AjaxResult.success();
    }

    @Log(title = "根据id查询题目")
    @GetMapping("getQueById")
    public AjaxResult getQueById(@RequestParam("id") Long id) {
        QueResp que = queService.getQueById(id);
        return AjaxResult.success(que);
    }

    @Log(title = "练习题目")
    @GetMapping("practice")
    public AjaxResult practice() {
        return AjaxResult.success(queService.getRecommendQueList());
    }

    @Log(title = "保存答题")
    @PostMapping("saveQueRecord")
    public AjaxResult saveQueRecord(@RequestBody QueReq req) {
        queService.saveQueRecord(req);
        return AjaxResult.success();
    }

    @Log(title = "查看每个用户的答题情况")
    @GetMapping("/answer")
    public AjaxResult getAnswer(@RequestParam Integer pageNum, @RequestParam Integer pageSize){
        PageResult pageResult = queService.getAnswer(pageNum, pageSize);
        return AjaxResult.success(pageResult);
    }

    @Log(title = "查看用户答题详情")
    @GetMapping("/answerDetails/{userId}")
    public AjaxResult getAnswerDetails(@PathVariable Long userId){
        List<RecommendQueResp> recommendQueRespList = queService.getAnswerDetails(userId);
        return AjaxResult.success(recommendQueRespList);
    }

    @Log(title = "查询所有题目")
    @GetMapping("/listAll")
    public AjaxResult listAll(String value){
        List<Map<String, String>> list = queService.listAll(value);
        return AjaxResult.success(list);
    }

    /** A model result of 1 means the answer is correct and the remedial video should not open. */
    @Log(title = "模型根据答题内容判断正误")
    @PostMapping("/send")
    public AjaxResult send(@RequestBody Map<String, String> map){
        Map<String, String> mapResp = queService.send(map);
        return AjaxResult.success(mapResp);
    }

}
