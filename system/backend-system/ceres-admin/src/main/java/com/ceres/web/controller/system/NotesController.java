package com.ceres.web.controller.system;

import com.ceres.common.annotation.Log;
import com.ceres.common.core.domain.AjaxResult;
import com.ceres.system.domain.vo.req.NotesReq;
import com.ceres.system.service.INotesService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("notes")
public class NotesController {

    @Resource
    private INotesService notesService;

    @Log(title = "查询笔记")
    @GetMapping("getNotesByVideoId")
    public AjaxResult getNotesByVideoId(@RequestParam("videoId") Long videoId) {
        return AjaxResult.success(notesService.getNotesByVideoId(videoId));
    }

    @Log(title = "新增笔记")
    @PostMapping("addNotes")
    public AjaxResult addNotes(@RequestBody NotesReq notesReq) {
        notesService.addNotes(notesReq);
        return AjaxResult.success();
    }

    @Log(title = "修改笔记")
    @PostMapping("updateNotes")
    public AjaxResult updateNotes(@RequestBody NotesReq notesReq) {
        notesService.updateNotes(notesReq);
        return AjaxResult.success();
    }

    @Log(title = "新增或修改笔记")
    @PostMapping("/addOrUpdateNotes")
    public AjaxResult addOrUpdateNotes(@RequestBody NotesReq notesReq){
        notesService.addOrUpdateNotes(notesReq);
        return AjaxResult.success();
    }

}
