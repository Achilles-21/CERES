package com.ceres.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ceres.common.exception.ServiceException;
import com.ceres.common.utils.MessageUtils;
import com.ceres.common.utils.SecurityUtils;
import com.ceres.common.utils.StringUtils;
import com.ceres.common.utils.bean.BeanUtils;
import com.ceres.system.domain.Notes;
import com.ceres.system.domain.vo.req.NotesReq;
import com.ceres.system.domain.vo.resp.NotesResp;
import com.ceres.system.mapper.NotesMapper;
import com.ceres.system.service.INotesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class NotesServiceImpl implements INotesService {

    @Resource
    private NotesMapper notesMapper;
    @Override
    public NotesResp getNotesByVideoId(Long videoId) {
        LambdaQueryWrapper<Notes> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notes::getVideoId,videoId)
                .eq(Notes::getDelFlag,"0")
                .eq(Notes::getUserId, SecurityUtils.getUserId());
        Notes notes = notesMapper.selectOne(wrapper);
        if (StringUtils.isNull(notes)){
            throw new ServiceException(MessageUtils.message("course.no.note"));
        }
        NotesResp resp = new NotesResp();
        BeanUtils.copyProperties(notes, resp);
        return resp;
    }

    @Override
    public void addNotes(NotesReq req) {
        Notes notes = new Notes();
        BeanUtils.copyProperties(req, notes);
        notes.setUserId(SecurityUtils.getUserId());
        notes.setCreateBy(SecurityUtils.getUsername());
        notesMapper.insert(notes);
    }

    @Override
    public void updateNotes(NotesReq req) {
        Notes notes = new Notes();
        notes.setContent(req.getContent());
        notes.setId(req.getId());
        notesMapper.updateById(notes);
    }

    @Override
    public void addOrUpdateNotes(NotesReq notesReq) {
        LambdaQueryWrapper<Notes> eq = new LambdaQueryWrapper<Notes>()
                .eq(ObjectUtil.isNotEmpty(notesReq.getId()), Notes::getId, notesReq.getId())
                .eq(Notes::getVideoId, notesReq.getVideoId())
                .eq(Notes::getUserId, SecurityUtils.getUserId());
        Notes notes = notesMapper.selectOne(eq);
        if(ObjectUtil.isNotEmpty(notes)){
            notes.setContent(notesReq.getContent());
            notesMapper.updateById(notes);
            return;
        }
        Notes addNotes = new Notes();
        BeanUtils.copyProperties(notesReq, addNotes);
        addNotes.setUserId(SecurityUtils.getUserId());
        addNotes.setCreateBy(SecurityUtils.getUsername());
        notesMapper.insert(addNotes);
    }

}
