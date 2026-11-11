package com.ceres.system.service;

import com.ceres.system.domain.vo.req.NotesReq;
import com.ceres.system.domain.vo.resp.NotesResp;

public interface INotesService {

    NotesResp getNotesByVideoId(Long videoId);

    void addNotes(NotesReq req);

    void updateNotes(NotesReq req);

    void addOrUpdateNotes(NotesReq notesReq);

}
