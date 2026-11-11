package com.ceres.web.controller.system;

import com.ceres.common.annotation.Log;
import com.ceres.common.core.controller.BaseController;
import com.ceres.common.core.domain.AjaxResult;
import com.ceres.common.core.page.PageResult;
import com.ceres.system.domain.vo.req.InvitationReplyReq;
import com.ceres.system.domain.vo.resp.InvitationReplyResp;
import com.ceres.system.service.IInvitationReplyService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/invitationReply")
public class InvitationReplyController extends BaseController {

    @Resource
    private IInvitationReplyService replyService;

    @Log(title = "新增评论")
    @PostMapping("/add")
    public AjaxResult add(@RequestBody InvitationReplyReq replyReq){
        InvitationReplyResp invitationReplyResp = replyService.add(replyReq);
        return success(invitationReplyResp);
    }

    @Log(title = "分页查询评论")
    @GetMapping("/pageQuery")
    public AjaxResult pageQuery(@RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize,
                                @RequestParam("invitationId") Long invitationId){
        PageResult pageResult = replyService.pageQuery(pageNum, pageSize, invitationId);
        return success(pageResult);
    }

    @Log(title = "删除评论")
    @DeleteMapping("/delete/{id}")
    public AjaxResult delete(@PathVariable Long id){
        replyService.delete(id);
        return success();
    }

    @Log(title = "查看评论列表")
    @GetMapping("/listComment")
    public AjaxResult listComment(@RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize,
                                  @RequestParam("invitationReplyId") Long invitationReplyId){
        PageResult pageResult = replyService.listComment(pageNum, pageSize, invitationReplyId);
        return success(pageResult);
    }

}
