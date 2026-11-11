package com.ceres.web.controller.system;

import com.ceres.common.annotation.Log;
import com.ceres.common.core.controller.BaseController;
import com.ceres.common.core.domain.AjaxResult;
import com.ceres.common.core.page.PageResult;
import com.ceres.system.domain.vo.req.InvitationLikeReq;
import com.ceres.system.domain.vo.req.InvitationReq;
import com.ceres.system.domain.vo.resp.InvitationResp;
import com.ceres.system.service.IInvitationService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/invitation")
public class InvitationController extends BaseController {

    @Resource
    private IInvitationService invitationService;

    @Log(title = "分页查询帖子")
    @GetMapping("/pageQuery")
    public AjaxResult pageQuery(@RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize){
        PageResult pageResult = invitationService.pageQuery(pageNum, pageSize);
        return success(pageResult);
    }

    @Log(title = "发布帖子")
    @PostMapping("/add")
    public AjaxResult add(@RequestBody InvitationReq invitationReq){
        invitationService.add(invitationReq);
        return success();
    }

    @Log(title = "删除帖子")
    @DeleteMapping("/delete/{id}")
    public AjaxResult delete(@PathVariable Long id){
        invitationService.delete(id);
        return success();
    }

    @Log(title = "根据id查看帖子")
    @GetMapping("/selectById/{id}")
    public AjaxResult selectById(@PathVariable Long id){
        InvitationResp invitationResp = invitationService.selectById(id);
        return success(invitationResp);
    }

    @Log(title = "点赞帖子")
    @PostMapping("/likes")
    public AjaxResult likes(@RequestBody InvitationLikeReq likeReq){
        invitationService.likes(likeReq);
        return success();
    }

}
